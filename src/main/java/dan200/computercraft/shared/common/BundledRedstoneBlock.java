/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface BundledRedstoneBlock
{
    boolean getBundledRedstoneConnectivity( World world, BlockPos pos, Direction side );

    int getBundledRedstoneOutput( World world, BlockPos pos, Direction side );
}
