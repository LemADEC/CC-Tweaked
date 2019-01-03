/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.server;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.NetworkMessage;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A packet, which performs an action on a {@link ServerComputer}.
 *
 * This requires that the sending player is interacting with that computer via a
 * {@link dan200.computercraft.shared.computer.core.IContainerComputer}.
 */
public abstract class ComputerServerMessage implements NetworkMessage
{
    private int instanceId;

    public ComputerServerMessage( int instanceId )
    {
        this.instanceId = instanceId;
    }

    public ComputerServerMessage()
    {
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        buf.writeVarInt( instanceId );
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        instanceId = buf.readVarInt();
    }

    public ServerComputer getComputer( PacketContext context )
    {
        ServerComputer computer = ComputerCraft.serverComputerRegistry.get( instanceId );
        if( computer == null ) return null;

        // Verify the player is interacting with a computer.
        PlayerEntity player = context.getPlayer();
        if( player == null || !computer.isInteracting( player ) ) return null;

        return computer;
    }

    public static <T extends ComputerServerMessage> void register( Supplier<T> factory, BiConsumer<ServerComputer, T> handler )
    {
        NetworkMessage.registerMainThread( CustomPayloadPacketRegistry.SERVER, factory, ( context, packet ) -> {
            ServerComputer computer = packet.getComputer( context );
            if( computer != null ) handler.accept( computer, packet );
        } );
    }
}
