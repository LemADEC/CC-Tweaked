/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.Parameters;

import javax.annotation.Nullable;
import java.util.List;

public class BlockComputer extends BlockComputerBase<TileComputer>
{

    public static final EnumProperty<ComputerState> STATE = EnumProperty.create( "state", ComputerState.class );
    public static final DirectionProperty FACING = Properties.FACING_HORIZONTAL;

    public BlockComputer( Settings settings, ComputerFamily family, BlockEntityType<? extends TileComputer> type )
    {
        super( settings, family, type );
        setDefaultState( getDefaultState()
            .with( FACING, Direction.NORTH )
            .with( STATE, ComputerState.OFF )
        );
    }

    @Override
    protected void appendProperties( StateFactory.Builder<Block, BlockState> builder )
    {
        builder.with( FACING, STATE );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerHorizontalFacing().getOpposite() );
    }

    @Override
    @Deprecated
    public List<ItemStack> getDroppedStacks( BlockState block, LootContext.Builder lootBuilder )
    {
        BlockEntity entity = lootBuilder.getNullable( Parameters.BLOCK_ENTITY );
        if( entity instanceof TileComputer )
        {
            TileComputer computer = (TileComputer) entity;
            lootBuilder.putDrop( COMPUTER_DROP, ( lootContext, consumer ) -> {
                consumer.accept( ComputerItemFactory.create( computer ) );
            } );
        }
        return super.getDroppedStacks( block, lootBuilder );
    }

    @Override
    public ItemStack getPickStack( BlockView world, BlockPos pos, BlockState state )
    {
        BlockEntity entity = world.getBlockEntity( pos );
        return entity instanceof TileComputer ? ComputerItemFactory.create( (TileComputer) entity ) : super.getPickStack( world, pos, state );
    }
}
