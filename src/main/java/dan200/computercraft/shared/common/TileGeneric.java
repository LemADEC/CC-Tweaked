/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import net.fabricmc.fabric.block.entity.ClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class TileGeneric extends BlockEntity implements ClientSerializable
{
    public TileGeneric( BlockEntityType<? extends TileGeneric> type )
    {
        super( type );
    }

    public void destroy()
    {
    }

    public final void updateBlock()
    {
        World world = getWorld();
        if( world != null )
        {
            markDirty();
            BlockPos pos = getPos();
            BlockState state = getCachedState();
            world.scheduleBlockRender( pos );
            world.updateListeners( pos, state, state, 3 );
        }
    }

    public boolean onActivate( PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ )
    {
        return false;
    }

    public void onNeighbourChange( @Nonnull BlockPos neighbour )
    {
    }

    public void onNeighbourTileEntityChange( @Nonnull BlockPos neighbour )
    {
    }

    protected double getInteractRange( PlayerEntity player )
    {
        return 8.0;
    }

    public boolean isUsable( PlayerEntity player, boolean ignoreRange )
    {
        if( player == null || !player.isValid() || getWorld().getBlockEntity( getPos() ) != this ) return false;
        if( ignoreRange ) return true;

        double range = getInteractRange( player );
        BlockPos pos = getPos();
        return player.getEntityWorld() == getWorld()
            && player.squaredDistanceTo( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 ) <= range * range;
    }

    protected CompoundTag writeDescription( CompoundTag nbt )
    {
        return nbt;
    }

    protected void readDescription( CompoundTag nbt )
    {
    }

    @Override
    public final void fromClientTag( CompoundTag nbt )
    {
        readDescription( nbt );
    }

    @Override
    public final CompoundTag toClientTag( CompoundTag nbt )
    {
        return writeDescription( nbt );
    }
}
