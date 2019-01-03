/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2018. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import dan200.computercraft.api.filesystem.IMount;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceType;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class ResourceMountTest
{
    private IMount mount;

    @Before
    public void setup()
    {
        ReloadableResourceManager manager = new ReloadableResourceManagerImpl( ResourceType.DATA );
        manager.reload( Collections.singletonList(
            new DirectoryResourcePack( new File( "src/main/resources" ) )
        ) );

        mount = new ResourceMount( "computercraft", "lua/rom", manager );
    }

    @Test
    public void testList() throws IOException
    {
        List<String> files = new ArrayList<>();
        mount.list( "", files );
        files.sort( Comparator.naturalOrder() );

        assertEquals(
            Arrays.asList( "apis", "autorun", "help", "modules", "programs", "startup.lua" ),
            files
        );
    }

    @Test
    public void testExists() throws IOException
    {
        assertTrue( mount.exists( "" ) );
        assertTrue( mount.exists( "startup.lua" ) );
        assertTrue( mount.exists( "programs/fun/advanced/paint.lua" ) );

        assertFalse( mount.exists( "programs/fun/advance/paint.lua" ) );
        assertFalse( mount.exists( "programs/fun/advanced/paint.lu" ) );
    }

    @Test
    public void testIsDir() throws IOException
    {

        assertTrue( mount.isDirectory( "" ) );
    }

    @Test
    public void testIsFile() throws IOException
    {
        assertFalse( mount.isDirectory( "startup.lua" ) );
    }

    @Test
    public void testSize() throws IOException
    {
        assertNotEquals( mount.getSize( "startup.lua" ), 0 );
    }
}
