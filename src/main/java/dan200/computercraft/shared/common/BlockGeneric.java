/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class BlockGeneric extends Block implements BlockEntityProvider
{
    private final BlockEntityType<? extends TileGeneric> type;

    public BlockGeneric( Settings settings, BlockEntityType<? extends TileGeneric> type )
    {
        super( settings );
        this.type = type;
    }

    @Deprecated
    @Override
    public void onBlockRemoved( BlockState block, World world, BlockPos pos, BlockState replace, boolean bool )
    {
        if( block.getBlock() == replace.getBlock() ) return;

        BlockEntity entity = world.getBlockEntity( pos );
        super.onBlockRemoved( block, world, pos, replace, bool );
        world.removeBlockEntity( pos );
        if( entity instanceof TileGeneric ) ((TileGeneric) entity).destroy();
    }

    @Deprecated
    @Override
    public boolean onBlockAction( BlockState state, World world, BlockPos pos, int action, int arg )
    {
        super.onBlockAction( state, world, pos, action, arg );
        BlockEntity entity = world.getBlockEntity( pos );
        return entity != null && entity.method_11004( action, arg );
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity( BlockView blockView )
    {
        return type.instantiate();
    }

    @Override
    @Deprecated
    public boolean activate( BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ )
    {
        BlockEntity entity = world.getBlockEntity( pos );
        if( !(entity instanceof TileGeneric) )
        {
            return super.activate( state, world, pos, player, hand, side, hitX, hitY, hitZ );
        }

        return ((TileGeneric) entity).onActivate( player, hand, side, hitX, hitY, hitZ );
    }

    /*
    @Override
    public final void onNeighborChange( BlockView world, BlockPos pos, BlockPos neighbour )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric) tile;
            generic.onNeighbourTileEntityChange( neighbour );
        }
    }
    */

    @Override
    @Deprecated
    public void neighborUpdate( BlockState state, World world, BlockPos pos, Block neighbourBlock, BlockPos neighbourPos )
    {
        super.neighborUpdate( state, world, pos, neighbourBlock, neighbourPos );
        BlockEntity tile = world.getBlockEntity( pos );
        if( tile instanceof TileGeneric ) ((TileGeneric) tile).onNeighbourChange( neighbourPos );
    }
}
