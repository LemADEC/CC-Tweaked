/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.server.proxy;

import dan200.computercraft.shared.proxy.ComputerCraftProxyCommon;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;

public class ComputerCraftProxyServer extends ComputerCraftProxyCommon
{
    @Override
    public File getWorldDir( World world )
    {
        return world.getServer().getWorld( DimensionType.OVERWORLD ).getSaveHandler().getWorldDir();
    }
}
