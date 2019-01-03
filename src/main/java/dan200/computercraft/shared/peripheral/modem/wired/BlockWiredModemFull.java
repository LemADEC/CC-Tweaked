/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem.wired;

import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.common.TileGeneric;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;

public class BlockWiredModemFull extends BlockGeneric
{
    public static final BooleanProperty MODEM_ON = BooleanProperty.create( "modem" );
    public static final BooleanProperty PERIPHERAL_ON = BooleanProperty.create( "peripheral" );

    public BlockWiredModemFull( Settings settings, BlockEntityType<? extends TileGeneric> type )
    {
        super( settings, type );
        setDefaultState( getStateFactory().getDefaultState()
            .with( MODEM_ON, false )
            .with( PERIPHERAL_ON, false )
        );
    }

    @Override
    protected void appendProperties( StateFactory.Builder<Block, BlockState> builder )
    {
        builder.with( MODEM_ON, PERIPHERAL_ON );
    }
}
