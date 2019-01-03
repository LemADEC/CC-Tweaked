/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem.wireless;

import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.peripheral.modem.TileModemBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ViewableWorld;

import javax.annotation.Nullable;

public class BlockWirelessModem extends BlockGeneric
{
    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty ON = BooleanProperty.create( "on" );

    public BlockWirelessModem( Settings settings, BlockEntityType<? extends TileWirelessModem> type )
    {
        super( settings, type );
        setDefaultState( getStateFactory().getDefaultState()
            .with( FACING, Direction.NORTH )
            .with( ON, false ) );
    }

    @Override
    protected void appendProperties( StateFactory.Builder<Block, BlockState> builder )
    {
        builder.with( FACING, ON );
    }

    @Override
    @Deprecated
    public VoxelShape getBoundingShape( BlockState blockState, BlockView blockView, BlockPos blockPos )
    {
        return TileModemBase.SHAPES[blockState.get( FACING ).ordinal()];
    }

    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate( BlockState state, Direction side, BlockState otherState, IWorld world, BlockPos pos, BlockPos otherPos )
    {
        return side == state.get( FACING ) && !state.canPlaceAt( world, pos )
            ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    @Deprecated
    public boolean canPlaceAt( BlockState state, ViewableWorld world, BlockPos pos )
    {
        Direction facing = state.get( FACING );
        BlockPos offsetPos = pos.offset( facing );
        BlockState offsetState = world.getBlockState( offsetPos );
        return Block.isFaceFullCube( offsetState.getCollisionShape( world, offsetPos ), facing.getOpposite() ) && !method_9581( offsetState.getBlock() );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getFacing().getOpposite() );
    }
}
