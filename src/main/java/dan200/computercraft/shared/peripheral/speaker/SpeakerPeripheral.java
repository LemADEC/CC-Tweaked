/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.network.packet.PlaySoundIdClientPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static dan200.computercraft.core.apis.ArgumentHelper.optReal;

public class SpeakerPeripheral implements IPeripheral
{
    private final TileSpeaker m_speaker;
    private long m_clock;
    private long m_lastPlayTime;
    private final AtomicInteger m_notesThisTick;

    public SpeakerPeripheral()
    {
        this( null );
    }

    SpeakerPeripheral( TileSpeaker speaker )
    {
        m_clock = 0;
        m_lastPlayTime = 0;
        m_notesThisTick = new AtomicInteger();
        m_speaker = speaker;
    }

    public void update()
    {
        m_clock++;
        m_notesThisTick.set( 0 );
    }

    public World getWorld()
    {
        return m_speaker.getWorld();
    }

    public BlockPos getPos()
    {
        return m_speaker.getPos();
    }

    public boolean madeSound( long ticks )
    {
        return m_clock - m_lastPlayTime <= ticks;
    }

    /* IPeripheral implementation */

    @Override
    public boolean equals( IPeripheral other )
    {
        if( other == this ) return true;
        if( !(other instanceof SpeakerPeripheral) ) return false;
        return m_speaker == ((SpeakerPeripheral) other).m_speaker;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "speaker";
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "playSound", // Plays sound at resourceLocator
            "playNote" // Plays note
        };
    }

    @Override
    public Object[] callMethod( @Nonnull IComputerAccess computerAccess, @Nonnull ILuaContext context, int methodIndex, @Nonnull Object[] args ) throws LuaException
    {
        switch( methodIndex )
        {
            // playSound
            case 0:
            {
                String name = getString( args, 0 );
                float volume = (float) optReal( args, 1, 1.0 );
                float pitch = (float) optReal( args, 2, 1.0 );

                Identifier identifier;
                try
                {
                    identifier = new Identifier( name );
                }
                catch( InvalidIdentifierException e )
                {
                    throw new LuaException( "Malformed sound name '" + name + "' " );
                }

                return new Object[] { playSound( context, identifier, volume, pitch, false ) };
            }

            // playNote
            case 1:
            {
                return playNote( args, context );
            }

            default:
            {
                throw new LuaException( "Method index out of range!" );
            }

        }
    }

    @Nonnull
    private synchronized Object[] playNote( Object[] arguments, ILuaContext context ) throws LuaException
    {
        String name = getString( arguments, 0 );
        float volume = (float) optReal( arguments, 1, 1.0 );
        float pitch = (float) optReal( arguments, 2, 1.0 );

        Instrument instrument = null;
        for( Instrument testInstrument : Instrument.values() )
        {
            if( testInstrument.asString().equalsIgnoreCase( name ) )
            {
                instrument = testInstrument;
                break;
            }
        }

        // Check if the note exists
        if( instrument == null )
        {
            throw new LuaException( "Unnown instrument, \"" + name + "\"!" );
        }

        // If the resource location for note block notes changes, this method call will need to be updated
        boolean success = playSound( context, instrument.getSound().getId(), volume, (float) Math.pow( 2.0, (pitch - 12.0) / 12.0 ), true );

        if( success ) m_notesThisTick.incrementAndGet();
        return new Object[] { success };
    }

    private synchronized boolean playSound( ILuaContext context, Identifier name, float volume, float pitch, boolean isNote ) throws LuaException
    {
        if( m_clock - m_lastPlayTime < TileSpeaker.MIN_TICKS_BETWEEN_SOUNDS &&
            (!isNote || m_clock - m_lastPlayTime != 0 || m_notesThisTick.get() >= ComputerCraft.maxNotesPerTick) )
        {
            // Rate limiting occurs when we've already played a sound within the last tick, or we've
            // played more notes than allowable within the current tick.
            return false;
        }

        World world = getWorld();
        BlockPos pos = getPos();

        context.issueMainThreadTask( () -> {
            MinecraftServer server = world.getServer();
            if( server == null ) return null;

            double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
            float adjVolume = Math.min( volume, 3.0f );
            server.getPlayerManager().sendToAround(
                null, x, y, z, adjVolume > 1.0f ? 16 * adjVolume : 16.0, world.getDimension().getType(),
                new PlaySoundIdClientPacket( name, SoundCategory.RECORD, new Vec3d( x, y, z ), adjVolume, pitch )
            );
            return null;
        } );

        m_lastPlayTime = m_clock;
        return true;
    }
}

