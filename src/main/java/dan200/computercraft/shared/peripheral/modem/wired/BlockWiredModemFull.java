/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem.wired;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockPeripheralBase;
import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockWiredModemFull extends BlockPeripheralBase
{
    // Statics

    public static class Properties
    {
        public static final PropertyBool MODEM_ON = PropertyBool.create( "modem" );
        public static final PropertyBool PERIPHERAL_ON = PropertyBool.create( "peripheral" );
    }

    // Members

    public BlockWiredModemFull()
    {
        setHardness( 1.5f );
        setTranslationKey( "computercraft:wired_modem_full" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
        setDefaultState( blockState.getBaseState()
            .withProperty( Properties.MODEM_ON, false )
            .withProperty( Properties.PERIPHERAL_ON, false )
        );
    }

    @Override
    protected IBlockState getDefaultBlockState( PeripheralType type, EnumFacing placedSide )
    {
        return getDefaultState();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer( this,
            Properties.MODEM_ON,
            Properties.PERIPHERAL_ON
        );
    }

    @Override
    public int getMetaFromState( IBlockState state )
    {
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState( @Nonnull IBlockState state, IBlockAccess world, BlockPos pos )
    {
        TileEntity te = world.getTileEntity( pos );
        if( te instanceof TileWiredModemFull )
        {
            TileWiredModemFull modem = (TileWiredModemFull) te;
            int anim = modem.getAnim();
            state = state
                .withProperty( Properties.MODEM_ON, (anim & 1) != 0 )
                .withProperty( Properties.PERIPHERAL_ON, (anim & 2) != 0 );
        }

        return state;
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        return PeripheralType.WiredModemFull;
    }

    @Override
    public PeripheralType getPeripheralType( IBlockState state )
    {
        return PeripheralType.WiredModemFull;
    }

    @Override
    public TilePeripheralBase createTile( PeripheralType type )
    {
        return new TileWiredModemFull();
    }
}
