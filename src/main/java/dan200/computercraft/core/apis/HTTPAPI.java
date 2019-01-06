/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.http.CheckUrl;
import dan200.computercraft.core.apis.http.HTTPRequestException;
import dan200.computercraft.core.apis.http.request.HttpRequest;
import dan200.computercraft.core.apis.http.websocket.Websocket;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dan200.computercraft.core.apis.ArgumentHelper.*;
import static dan200.computercraft.core.apis.TableHelper.*;

public class HTTPAPI implements ILuaAPI
{
    private final IAPIEnvironment m_apiEnvironment;

    private final Set<Closeable> tasks = Collections.newSetFromMap( new ConcurrentHashMap<>() );

    public HTTPAPI( IAPIEnvironment environment )
    {
        m_apiEnvironment = environment;
    }

    @Override
    public String[] getNames()
    {
        return new String[] {
            "http"
        };
    }

    @Override
    public void shutdown()
    {
        for( Closeable task : tasks )
        {
            try
            {
                task.close();
            }
            catch( IOException ignored )
            {
            }
        }
        tasks.clear();
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "request",
            "checkURL",
            "websocket",
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0: // request
            {
                String address, postString, requestMethod;
                Map<Object, Object> headerTable;
                boolean binary, redirect;

                if( args.length >= 1 && args[0] instanceof Map )
                {
                    Map<?, ?> options = (Map) args[0];
                    address = getStringField( options, "url" );
                    postString = optStringField( options, "body", null );
                    headerTable = optTableField( options, "headers", Collections.emptyMap() );
                    binary = optBooleanField( options, "binary", false );
                    requestMethod = optStringField( options, "method", null );
                    redirect = optBooleanField( options, "redirect", true );

                }
                else
                {
                    // Get URL and post information
                    address = getString( args, 0 );
                    postString = optString( args, 1, null );
                    headerTable = optTable( args, 2, Collections.emptyMap() );
                    binary = optBoolean( args, 3, false );
                    requestMethod = null;
                    redirect = true;
                }

                HttpHeaders headers = getHeaders( headerTable );


                HttpMethod httpMethod;
                if( requestMethod == null )
                {
                    httpMethod = postString == null ? HttpMethod.GET : HttpMethod.POST;
                }
                else
                {
                    httpMethod = HttpMethod.valueOf( requestMethod.toUpperCase( Locale.ROOT ) );
                    if( httpMethod == null || requestMethod.equalsIgnoreCase( "CONNECT" ) )
                    {
                        throw new LuaException( "Unsupported HTTP method" );
                    }
                }
                // Make the request
                try
                {
                    URI uri = HttpRequest.checkUri( address );
                    HttpRequest connector = new HttpRequest( m_apiEnvironment, this, address, postString, headers, binary, redirect );
                    tasks.add( connector );
                    connector.request( uri, httpMethod );

                    return new Object[] { true };
                }
                catch( HTTPRequestException e )
                {
                    return new Object[] { false, e.getMessage() };
                }
            }
            case 1: // checkURL
            {
                String address = getString( args, 0 );

                // Check URL
                try
                {
                    URI uri = HttpRequest.checkUri( address );
                    CheckUrl check = new CheckUrl( m_apiEnvironment, this, address, uri );
                    tasks.add( check );
                    check.run();

                    return new Object[] { true };
                }
                catch( HTTPRequestException e )
                {
                    return new Object[] { false, e.getMessage() };
                }
            }
            case 2: // websocket
            {
                String address = getString( args, 0 );
                Map<Object, Object> headerTbl = optTable( args, 1, Collections.emptyMap() );

                if( !ComputerCraft.http_websocket_enable )
                {
                    throw new LuaException( "Websocket connections are disabled" );
                }

                HttpHeaders headers = getHeaders( headerTbl );

                try
                {
                    URI uri = Websocket.checkUri( address );
                    Websocket connector = new Websocket( m_apiEnvironment, this, uri, address, headers );
                    tasks.add( connector );
                    connector.connect();

                    return new Object[] { true };
                }
                catch( HTTPRequestException e )
                {
                    return new Object[] { false, e.getMessage() };
                }
            }
            default:
            {
                return null;
            }
        }
    }

    public void removeCloseable( Closeable closeable )
    {
        tasks.remove( closeable );
    }

    @Nonnull
    private static HttpHeaders getHeaders( @Nonnull Map<?, ?> headerTable ) throws LuaException
    {
        HttpHeaders headers = new DefaultHttpHeaders();
        for( Object key : headerTable.keySet() )
        {
            Object value = headerTable.get( key );
            if( key instanceof String && value instanceof String )
            {
                try
                {
                    headers.add( (String) key, value );
                }
                catch( IllegalArgumentException e )
                {
                    throw new LuaException( e.getMessage() );
                }
            }
        }
        return headers;
    }
}
