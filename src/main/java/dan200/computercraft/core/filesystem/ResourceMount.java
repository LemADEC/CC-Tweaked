/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import dan200.computercraft.api.filesystem.IMount;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceMount implements IMount
{
    private static final byte[] TEMP_BUFFER = new byte[8192];

    private class FileEntry
    {
        final Identifier identifier;
        Map<String, FileEntry> children;
        long size = -1;

        FileEntry( Identifier identifier )
        {
            this.identifier = identifier;
        }

        boolean isDirectory()
        {
            return children != null;
        }

        void list( List<String> contents )
        {
            if( children != null ) contents.addAll( children.keySet() );
        }
    }

    private final String namespace;
    private final String subPath;
    private final ReloadableResourceManager manager;

    @Nullable
    private FileEntry root;

    public ResourceMount( String namespace, String subPath, ReloadableResourceManager manager )
    {
        this.namespace = namespace;
        this.subPath = subPath;
        this.manager = manager;

        this.manager.addListener( new Listener( this ) );
    }

    private void load()
    {
        boolean hasAny = false;
        FileEntry newRoot = new FileEntry( new Identifier( namespace, subPath ) );
        for( Identifier file : manager.findResources( subPath, s -> true ) )
        {
            if( !file.getNamespace().equals( namespace ) ) continue;

            String localPath = FileSystem.toLocal( file.getPath(), subPath );
            create( newRoot, localPath );
            hasAny = true;
        }

        root = hasAny ? newRoot : null;
    }

    private FileEntry get( String path )
    {
        FileEntry lastEntry = root;
        int lastIndex = 0;

        while( lastEntry != null && lastIndex < path.length() )
        {
            int nextIndex = path.indexOf( '/', lastIndex );
            if( nextIndex < 0 ) nextIndex = path.length();

            lastEntry = lastEntry.children == null ? null : lastEntry.children.get( path.substring( lastIndex, nextIndex ) );
            lastIndex = nextIndex + 1;
        }

        return lastEntry;
    }

    private void create( FileEntry lastEntry, String path )
    {
        int lastIndex = 0;
        while( lastIndex < path.length() )
        {
            int nextIndex = path.indexOf( '/', lastIndex );
            if( nextIndex < 0 ) nextIndex = path.length();

            String part = path.substring( lastIndex, nextIndex );
            if( lastEntry.children == null ) lastEntry.children = new HashMap<>();

            FileEntry nextEntry = lastEntry.children.get( part );
            if( nextEntry == null )
            {
                lastEntry.children.put( part, nextEntry = new FileEntry( new Identifier( namespace, subPath + "/" + path ) ) );
            }

            lastEntry = nextEntry;
            lastIndex = nextIndex + 1;
        }
    }

    @Override
    public boolean exists( @Nonnull String path )
    {
        return get( path ) != null;
    }

    @Override
    public boolean isDirectory( @Nonnull String path )
    {
        FileEntry file = get( path );
        return file != null && file.isDirectory();
    }

    @Override
    public void list( @Nonnull String path, @Nonnull List<String> contents ) throws IOException
    {
        FileEntry file = get( path );
        if( file == null || !file.isDirectory() ) throw new IOException( "/" + path + ": Not a directory" );

        file.list( contents );
    }

    @Override
    public long getSize( @Nonnull String path ) throws IOException
    {
        FileEntry file = get( path );
        if( file != null )
        {
            if( file.size != -1 ) return file.size;
            if( file.isDirectory() ) return file.size = 0;

            try
            {
                Resource resource = manager.getResource( file.identifier );
                if( resource != null )
                {
                    InputStream s = resource.getInputStream();
                    int total = 0, read = 0;
                    do
                    {
                        total += read;
                        read = s.read( TEMP_BUFFER );
                    } while( read > 0 );

                    return file.size = total;
                }
            }
            catch( IOException e )
            {
                e.printStackTrace();
                return file.size = 0;
            }
        }

        throw new IOException( "/" + path + ": No such file" );
    }

    @Nonnull
    @Override
    @Deprecated
    public InputStream openForRead( @Nonnull String path ) throws IOException
    {
        FileEntry file = get( path );
        if( file != null && !file.isDirectory() )
        {
            Resource entry = manager.getResource( file.identifier );
            if( entry != null ) return entry.getInputStream();
        }

        throw new IOException( "/" + path + ": No such file" );
    }

    /**
     * A {@link ResourceReloadListener} which refers to the {@link ResourceMount} weakly.
     *
     * While people should really be keeping a permanent reference to this, some people construct it every
     * method call, so let's make this as small as possible.
     */
    static class Listener implements ResourceReloadListener
    {
        private final WeakReference<ResourceMount> ref;

        Listener( ResourceMount mount )
        {
            this.ref = new WeakReference<>( mount );
        }

        @Override
        public void onResourceReload( ResourceManager manager )
        {
            ResourceMount mount = ref.get();
            if( mount != null ) mount.load();
        }
    }
}
