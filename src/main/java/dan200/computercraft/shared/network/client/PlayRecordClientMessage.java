/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.client;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.network.NetworkMessage;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;

/**
 * Starts or stops a record on the client, depending on if {@link #getSoundEvent()} is {@code null}.
 *
 * Used by disk drives to play record items.
 *
 * @see dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive
 */
public class PlayRecordClientMessage implements NetworkMessage
{
    private static final Identifier ID = new Identifier( ComputerCraft.MOD_ID, "play_record" );

    private BlockPos pos;
    private String name;
    private SoundEvent soundEvent;

    public PlayRecordClientMessage( BlockPos pos, SoundEvent event, String name )
    {
        this.pos = pos;
        this.name = name;
        this.soundEvent = event;
    }

    public PlayRecordClientMessage( BlockPos pos )
    {
        this.pos = pos;
    }

    public PlayRecordClientMessage()
    {
    }

    @Override
    public @Nonnull Identifier getId()
    {
        return ID;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public String getName()
    {
        return name;
    }

    public SoundEvent getSoundEvent()
    {
        return soundEvent;
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        buf.writeBlockPos( pos );
        if( soundEvent == null )
        {
            buf.writeBoolean( false );
        }
        else
        {
            buf.writeBoolean( true );
            buf.writeString( name );
            buf.writeInt( Registry.SOUND_EVENT.getRawId( soundEvent ) );
        }
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        pos = buf.readBlockPos();
        if( buf.readBoolean() )
        {
            name = buf.readString( Short.MAX_VALUE );
            soundEvent = Registry.SOUND_EVENT.getInt( buf.readInt() );
        }
    }
}
