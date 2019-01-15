/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.shared.common.BlockDirectional;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class BlockComputerBase extends BlockDirectional
{
    public BlockComputerBase( Material material )
    {
        super( material );
    }

    @Override
    public void onBlockAdded( World world, BlockPos pos, IBlockState state )
    {
        super.onBlockAdded( world, pos, state );
        updateInput( world, pos );
    }

    @Override
    public void setDirection( World world, BlockPos pos, EnumFacing dir )
    {
        super.setDirection( world, pos, dir );
        updateInput( world, pos );
    }

    protected abstract IBlockState getDefaultBlockState( ComputerFamily family, EnumFacing placedSide );

    protected abstract ComputerFamily getFamily( int damage );

    protected abstract ComputerFamily getFamily( IBlockState state );

    protected abstract TileComputerBase createTile( ComputerFamily family );

    @Nonnull
    @Override
    @Deprecated
    public final IBlockState getStateForPlacement( World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int damage, EntityLivingBase placer )
    {
        return getDefaultBlockState( getFamily( damage ), DirectionUtil.fromEntityRot( placer ) );
    }

    @Override
    public final TileComputerBase createTile( IBlockState state )
    {
        return createTile( getFamily( state ) );
    }

    @Override
    public final TileComputerBase createTile( int damage )
    {
        return createTile( getFamily( damage ) );
    }

    public final ComputerFamily getFamily( IBlockAccess world, BlockPos pos )
    {
        return getFamily( world.getBlockState( pos ) );
    }

    protected void updateInput( IBlockAccess world, BlockPos pos )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile instanceof TileComputerBase )
        {
            TileComputerBase computer = (TileComputerBase) tile;
            computer.updateInput();
        }
    }
}
