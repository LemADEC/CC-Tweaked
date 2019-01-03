/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network;

import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The base interface for any message which will be sent to the client or server.
 *
 * @see dan200.computercraft.shared.network.client
 * @see dan200.computercraft.shared.network.server
 * @see CustomPayloadPacketRegistry
 */
public interface NetworkMessage
{
    /**
     * The unique identifier for this packet type
     *
     * @return This packet type's identifier
     */
    @Nonnull
    Identifier getId();

    /**
     * Write this packet to a buffer.
     *
     * This may be called on any thread, so this should be a pure operation.
     *
     * @param buf The buffer to write data to.
     */
    void toBytes( @Nonnull PacketByteBuf buf );

    /**
     * Read this packet from a buffer.
     *
     * This may be called on any thread, so this should be a pure operation.
     *
     * @param buf The buffer to read data from.
     */
    void fromBytes( @Nonnull PacketByteBuf buf );

    /**
     * Register a packet, and a thread-safe handler for it.
     *
     * @param registry The registry to register this packet handler under
     * @param factory  The factory for this type of packet.
     * @param handler  The handler for this type of packet. Note, this may be called on any thread,
     *                 and so should be thread-safe.
     */
    static <T extends NetworkMessage> void register(
        CustomPayloadPacketRegistry registry,
        Supplier<T> factory,
        BiConsumer<PacketContext, T> handler
    )
    {
        registry.register( factory.get().getId(), ( ctx, buf ) -> {
            T packet = factory.get();
            packet.fromBytes( buf );
            handler.accept( ctx, packet );
        } );
    }

    /**
     * Register packet, and a thread-unsafe handler for it.
     *
     * @param registry The registry to register this packet handler under
     * @param factory  The factory for this type of packet.
     * @param handler  The handler for this type of packet. This will be called on the "main"
     *                 thread (either client or server).
     */
    static <T extends NetworkMessage> void registerMainThread(
        CustomPayloadPacketRegistry registry,
        Supplier<T> factory,
        BiConsumer<PacketContext, T> handler
    )
    {
        registry.register( factory.get().getId(), ( ctx, buf ) -> {
            T packet = factory.get();
            packet.fromBytes( buf );
            if( ctx.getTaskQueue().isMainThread() )
            {
                handler.accept( ctx, packet );
            }
            else
            {
                ctx.getTaskQueue().execute( () -> handler.accept( ctx, packet ) );
            }
        } );
    }
}
