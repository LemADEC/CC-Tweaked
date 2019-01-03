/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.peripheral.IPeripheralTile;
import dan200.computercraft.shared.util.NamedBlockEntityType;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

public class TileSpeaker extends TileGeneric implements Tickable, IPeripheralTile
{
    public static final int MIN_TICKS_BETWEEN_SOUNDS = 1;

    public static final NamedBlockEntityType<TileSpeaker> FACTORY = NamedBlockEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "speaker" ),
        TileSpeaker::new
    );

    private final SpeakerPeripheral m_peripheral;

    public TileSpeaker( BlockEntityType<? extends TileSpeaker> type )
    {
        super( type );
        m_peripheral = new SpeakerPeripheral( this );
    }

    @Override
    public void tick()
    {
        m_peripheral.update();
    }

    @Override
    public IPeripheral getPeripheral( Direction side )
    {
        return m_peripheral;
    }
}
    
