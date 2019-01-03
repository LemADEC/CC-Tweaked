/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.peripheral.IPeripheralTile;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public abstract class TileModemBase extends TileGeneric implements IPeripheralTile, Tickable
{
    public static final VoxelShape[] SHAPES = new VoxelShape[] {
        VoxelShapes.cube( 0.125, 0.0, 0.125, 0.875, 0.1875, 0.875 ), // Down
        VoxelShapes.cube( 0.125, 0.8125, 0.125, 0.875, 1.0, 0.875 ), // Up
        VoxelShapes.cube( 0.125, 0.125, 0.0, 0.875, 0.875, 0.1875 ), // North
        VoxelShapes.cube( 0.125, 0.125, 0.8125, 0.875, 0.875, 1.0 ), // South
        VoxelShapes.cube( 0.0, 0.125, 0.125, 0.1875, 0.875, 0.875 ), // West
        VoxelShapes.cube( 0.8125, 0.125, 0.125, 1.0, 0.875, 0.875 ), // East
    };

    protected ModemPeripheral m_modem;

    public TileModemBase( BlockEntityType<? extends TileModemBase> type )
    {
        super( type );
        m_modem = createPeripheral();
    }

    protected abstract ModemPeripheral createPeripheral();

    @Override
    public void destroy()
    {
        if( m_modem != null )
        {
            m_modem.destroy();
            m_modem = null;
        }
    }

    @Override
    public void tick()
    {
        if( !getWorld().isClient && m_modem.getModemState().pollChanged() ) updateBlockState();
    }

    protected abstract void updateBlockState();

    protected abstract Direction getDirection();

    @Override
    public IPeripheral getPeripheral( Direction side )
    {
        return side == getDirection() ? m_modem : null;
    }
}
