/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.server;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.network.NetworkMessage;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;

public class RequestComputerMessage implements NetworkMessage
{
    private static final Identifier ID = new Identifier( ComputerCraft.MOD_ID, "request_computer" );

    private int instance;

    public RequestComputerMessage( int instance )
    {
        this.instance = instance;
    }

    public RequestComputerMessage()
    {
    }

    @Nonnull
    @Override
    public Identifier getId()
    {
        return ID;
    }

    public int getInstance()
    {
        return instance;
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        buf.writeVarInt( instance );
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        instance = buf.readVarInt();
    }
}
