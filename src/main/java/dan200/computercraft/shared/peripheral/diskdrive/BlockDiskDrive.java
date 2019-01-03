/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.diskdrive;

import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.Parameters;

import javax.annotation.Nullable;
import java.util.List;

public class BlockDiskDrive extends BlockGeneric
{
    static final DirectionProperty FACING = Properties.FACING_HORIZONTAL;
    static final EnumProperty<DiskDriveState> STATE = EnumProperty.create( "state", DiskDriveState.class );

    public BlockDiskDrive( Settings settings, BlockEntityType<? extends TileDiskDrive> type )
    {
        super( settings, type );
        setDefaultState( getStateFactory().getDefaultState()
            .with( FACING, Direction.NORTH )
            .with( STATE, DiskDriveState.EMPTY ) );
    }

    @Override
    protected void appendProperties( StateFactory.Builder<Block, BlockState> properties )
    {
        super.appendProperties( properties );
        properties.with( FACING, STATE );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerHorizontalFacing().getOpposite() );
    }

    @Deprecated
    @Override
    public List<ItemStack> getDroppedStacks( BlockState state, LootContext.Builder context )
    {
        BlockEntity entity = context.getNullable( Parameters.BLOCK_ENTITY );
        if( entity instanceof TileDiskDrive )
        {
            InventoryUtil.dropContents( (TileDiskDrive) entity, context );
        }

        return super.getDroppedStacks( state, context );
    }
}
