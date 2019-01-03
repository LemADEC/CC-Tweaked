/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.peripherals;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.IPocketAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketSpeaker extends AbstractPocketUpgrade
{
    public PocketSpeaker()
    {
        super(
            new Identifier( "computercraft", "speaker" ),
            "upgrade.computercraft.speaker.adjective",
            ComputerCraft.Blocks.speaker
        );
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral( @Nonnull IPocketAccess access )
    {
        return new PocketSpeakerPeripheral();
    }

    @Override
    public void update( @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral )
    {
        if( peripheral instanceof PocketSpeakerPeripheral )
        {
            PocketSpeakerPeripheral speaker = (PocketSpeakerPeripheral) peripheral;

            Entity entity = access.getEntity();
            if( entity != null )
            {
                speaker.setLocation( entity.getEntityWorld(), entity.x, entity.y + entity.getEyeHeight(), entity.z );
            }
            speaker.update();
            access.setLight( speaker.madeSound( 20 ) ? 0x3320fc : -1 );
        }
    }
}
