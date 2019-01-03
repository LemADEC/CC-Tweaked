/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.shared.common.BlockGeneric;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public class BlockSpeaker extends BlockGeneric
{
    private static final DirectionProperty FACING = Properties.FACING_HORIZONTAL;

    public BlockSpeaker( Settings settings, BlockEntityType<? extends TileSpeaker> type )
    {
        super( settings, type );
        setDefaultState( getStateFactory().getDefaultState()
            .with( FACING, Direction.NORTH ) );
    }

    @Override
    protected void appendProperties( StateFactory.Builder<Block, BlockState> properties )
    {
        super.appendProperties( properties );
        properties.with( FACING );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerHorizontalFacing().getOpposite() );
    }
}
