/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileSpeaker extends TileGeneric implements ITickable, IPeripheralTile
{
    // Statics
    public static final int MIN_TICKS_BETWEEN_SOUNDS = 1;

    // Members
    private final SpeakerPeripheral m_peripheral;

    public TileSpeaker()
    {
        super();
        m_peripheral = new SpeakerPeripheral( this );
    }

    @Override
    public synchronized void update()
    {
        m_peripheral.update();
    }

    // IPeripheralTile implementation

    @Override
    public PeripheralType getPeripheralType()
    {
        return PeripheralType.Speaker;
    }

    @Override
    public IPeripheral getPeripheral( EnumFacing side )
    {
        return m_peripheral;
    }

    @Override
    public String getLabel()
    {
        return null;
    }

    @Override
    public EnumFacing getDirection()
    {
        return getBlockState().getValue( BlockSpeaker.FACING );
    }

    @Override
    public void setDirection( EnumFacing dir )
    {
        if( dir.getAxis() != EnumFacing.Axis.Y )
        {
            setBlockState( getBlockState().withProperty( BlockSpeaker.FACING, dir ) );
        }
    }
}
