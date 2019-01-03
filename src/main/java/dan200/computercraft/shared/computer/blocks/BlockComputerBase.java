/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.common.BundledRedstoneBlock;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class BlockComputerBase<T extends TileComputerBase> extends BlockGeneric implements BundledRedstoneBlock
{
    public static final Identifier COMPUTER_DROP = new Identifier( ComputerCraft.MOD_ID, "computer" );

    private final ComputerFamily family;

    protected BlockComputerBase( Settings settings, ComputerFamily family, BlockEntityType<? extends T> type )
    {
        super( settings, type );
        this.family = family;
    }

    public ComputerFamily getFamily()
    {
        return family;
    }

    /*
    @Override
    public void onBlockAdded( World world, BlockPos pos, BlockState state )
    {
        super.onBlockAdded( world, pos, state );
        updateInput( world, pos );
    }
    */

    @Override
    @Deprecated
    public final boolean emitsRedstonePower( BlockState state )
    {
        return true;
    }

    @Override
    @Deprecated
    public int getStrongRedstonePower( BlockState state, BlockView world, BlockPos pos, Direction incomingSide )
    {
        BlockEntity entity = world.getBlockEntity( pos );
        if( !(entity instanceof TileComputerBase) ) return 0;

        TileComputerBase computerEntity = (TileComputerBase) entity;
        ServerComputer computer = computerEntity.getServerComputer();
        if( computer == null ) return 0;

        Direction localSide = computerEntity.remapToLocalSide( incomingSide.getOpposite() );
        return computerEntity.isRedstoneBlockedOnSide( localSide ) ? 0 :
            computer.getRedstoneOutput( localSide.getId() );
    }

    @Override
    @Deprecated
    public int getWeakRedstonePower( BlockState state, BlockView world, BlockPos pos, Direction incomingSide )
    {
        return getStrongRedstonePower( state, world, pos, incomingSide );
    }

    @Override
    public boolean getBundledRedstoneConnectivity( World world, BlockPos pos, Direction side )
    {
        BlockEntity entity = world.getBlockEntity( pos );
        if( !(entity instanceof TileComputerBase) ) return false;

        TileComputerBase computerEntity = (TileComputerBase) entity;
        return !computerEntity.isRedstoneBlockedOnSide( computerEntity.remapToLocalSide( side ) );
    }

    @Override
    public int getBundledRedstoneOutput( World world, BlockPos pos, Direction side )
    {
        BlockEntity entity = world.getBlockEntity( pos );
        if( !(entity instanceof TileComputerBase) ) return 0;

        TileComputerBase computerEntity = (TileComputerBase) entity;
        ServerComputer computer = computerEntity.getServerComputer();
        if( computer == null ) return 0;

        Direction localSide = computerEntity.remapToLocalSide( side );
        return computerEntity.isRedstoneBlockedOnSide( localSide ) ? 0 :
            computer.getBundledRedstoneOutput( localSide.getId() );
    }
}
