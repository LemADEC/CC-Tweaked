/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.server;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.util.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queue an event on a {@link dan200.computercraft.shared.computer.core.ServerComputer}.
 *
 * @see dan200.computercraft.shared.computer.core.ClientComputer#queueEvent(String)
 * @see dan200.computercraft.shared.computer.core.ServerComputer#queueEvent(String)
 */
public class QueueEventServerMessage extends ComputerServerMessage
{
    private static final Identifier ID = new Identifier( ComputerCraft.MOD_ID, "queue_event" );

    private String event;
    private Object[] args;

    public QueueEventServerMessage( int instanceId, @Nonnull String event, @Nullable Object[] args )
    {
        super( instanceId );
        this.event = event;
        this.args = args;
    }

    public QueueEventServerMessage()
    {
    }

    @Override
    public @Nonnull Identifier getId()
    {
        return ID;
    }

    @Nonnull
    public String getEvent()
    {
        return event;
    }

    @Nullable
    public Object[] getArgs()
    {
        return args;
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        super.toBytes( buf );
        buf.writeString( event );
        buf.writeCompoundTag( args == null ? null : NBTUtil.encodeObjects( args ) );
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        super.fromBytes( buf );
        event = buf.readString( Short.MAX_VALUE );

        CompoundTag args = buf.readCompoundTag();
        this.args = args == null ? null : NBTUtil.decodeObjects( args );
    }
}
