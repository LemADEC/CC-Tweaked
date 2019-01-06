/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.http.request;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.core.apis.handles.ArrayByteChannel;
import dan200.computercraft.core.apis.handles.BinaryReadableHandle;
import dan200.computercraft.core.apis.handles.EncodedReadableHandle;
import dan200.computercraft.core.apis.http.HTTPRequestException;
import dan200.computercraft.core.apis.http.NetworkUtils;
import dan200.computercraft.core.tracking.TrackingField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static dan200.computercraft.core.apis.http.request.HttpRequest.getHeaderSize;

public final class HttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> implements Closeable
{
    /**
     * Same as {@link io.netty.handler.codec.MessageAggregator}.
     */
    private static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;

    private static final byte[] EMPTY_BYTES = new byte[0];

    private final HttpRequest context;
    private boolean closed = false;

    private final URI uri;
    private final HttpMethod method;

    private Charset responseCharset;
    private final HttpHeaders responseHeaders = new DefaultHttpHeaders();
    private HttpResponseStatus responseStatus;
    private CompositeByteBuf responseBody;

    HttpRequestHandler( HttpRequest context, URI uri, HttpMethod method )
    {
        this.context = context;

        this.uri = uri;
        this.method = method;
    }

    @Override
    public void channelActive( ChannelHandlerContext ctx ) throws Exception
    {
        if( context.checkClosed() ) return;

        ByteBuf body = context.body();
        body.resetReaderIndex().retain();

        String requestUri = uri.getRawPath();
        if( uri.getRawQuery() != null ) requestUri += "?" + uri.getRawQuery();

        FullHttpRequest request = new DefaultFullHttpRequest( HttpVersion.HTTP_1_1, HttpMethod.GET, requestUri, body );
        request.setMethod( method );
        request.headers().set( HttpHeaderNames.ACCEPT_CHARSET, "UTF-8" );
        request.headers().set( HttpHeaderNames.USER_AGENT, context.environment().getComputerEnvironment().getHostString() );
        request.headers().set( context.headers() );

        // We force some headers to be always applied
        request.headers().set( HttpHeaderNames.HOST, uri.getHost() );
        request.headers().set( HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE );

        ctx.channel().writeAndFlush( request );

        super.channelActive( ctx );
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx ) throws Exception
    {
        if( !closed ) context.failure( "Could not connect" );
        super.channelInactive( ctx );
    }

    @Override
    public void channelRead0( ChannelHandlerContext ctx, HttpObject message )
    {
        if( closed || context.checkClosed() ) return;

        if( message instanceof HttpResponse )
        {
            HttpResponse response = (HttpResponse) message;

            if( context.redirects.get() > 0 )
            {
                URI redirect = getRedirect( response.status(), response.headers() );
                if( redirect != null && !uri.equals( redirect ) && context.redirects.getAndDecrement() > 0 )
                {
                    // If we have a redirect, and don't end up at the same place, then follow it.

                    // We mark ourselves as disposed first though, to avoid firing events when the channel
                    // becomes inactive or disposed.
                    closed = true;
                    ctx.close();

                    try
                    {
                        HttpRequest.checkUri( redirect );
                    }
                    catch( HTTPRequestException e )
                    {
                        // If we cannot visit this uri, then fail.
                        context.failure( e.getMessage() );
                        return;
                    }

                    context.request( redirect, response.status().code() == 303 ? HttpMethod.GET : method );
                    return;
                }
            }

            responseCharset = HttpUtil.getCharset( response, StandardCharsets.UTF_8 );
            responseStatus = response.status();
            responseHeaders.add( response.headers() );
        }

        if( message instanceof HttpContent )
        {
            HttpContent content = (HttpContent) message;

            if( responseBody == null )
            {
                responseBody = ctx.alloc().compositeBuffer( DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS );
            }

            ByteBuf partial = content.content();
            if( partial.isReadable() ) responseBody.addComponent( true, partial.retain() );

            if( message instanceof LastHttpContent )
            {
                LastHttpContent last = (LastHttpContent) message;
                responseHeaders.add( last.trailingHeaders() );

                // Set the content length, if not already given.
                if( responseHeaders.contains( HttpHeaderNames.CONTENT_LENGTH ) )
                {
                    responseHeaders.set( HttpHeaderNames.CONTENT_LENGTH, responseBody.readableBytes() );
                }

                ctx.close();
                sendResponse();
            }
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause )
    {
        if( ComputerCraft.logPeripheralErrors ) ComputerCraft.log.error( "Error handling HTTP response", cause );
        context.failure( cause );
    }

    private void sendResponse()
    {
        // Read the ByteBuf into a channel.
        CompositeByteBuf body = responseBody;
        byte[] bytes = body == null ? EMPTY_BYTES : NetworkUtils.toBytes( body );

        // Decode the headers
        HttpResponseStatus status = responseStatus;
        Map<String, String> headers = new HashMap<>();
        for( Map.Entry<String, String> header : responseHeaders )
        {
            String existing = headers.get( header.getKey() );
            headers.put( header.getKey(), existing == null ? header.getValue() : existing + "," + header.getValue() );
        }

        // Fire off a stats event
        context.environment().addTrackingChange( TrackingField.HTTP_DOWNLOAD, getHeaderSize( responseHeaders ) + bytes.length );

        // Prepare to queue an event
        ArrayByteChannel contents = new ArrayByteChannel( bytes );
        final ILuaObject reader = context.isBinary()
            ? new BinaryReadableHandle( contents )
            : new EncodedReadableHandle( EncodedReadableHandle.open( contents, responseCharset ) );
        ILuaObject stream = new HttpResponseHandle( reader, status.code(), headers );

        if( status.code() >= 200 && status.code() < 400 )
        {
            context.success( stream );
        }
        else
        {
            context.failure( status.reasonPhrase(), stream );
        }
    }

    /**
     * Determine the redirect from this response
     */
    private URI getRedirect( HttpResponseStatus status, HttpHeaders headers )
    {
        int code = status.code();
        if( code < 300 || code > 307 || code == 304 || code == 306 ) return null;

        String location = headers.get( HttpHeaderNames.LOCATION );
        if( location == null ) return null;

        try
        {
            return uri.resolve( new URI( URLDecoder.decode( location, "UTF-8" ) ) );
        }
        catch( UnsupportedEncodingException | IllegalArgumentException | URISyntaxException e )
        {
            return null;
        }
    }

    @Override
    public void close()
    {
        closed = true;
        if( responseBody != null )
        {
            responseBody.release();
            responseBody = null;
        }
    }
}
