/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.container;

import net.fabricmc.fabric.api.client.gui.GuiFactory;
import net.fabricmc.fabric.api.client.gui.GuiProviderRegistry;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface ContainerType<T extends Container>
{
    @Nonnull
    Identifier getId();

    void toBytes( PacketByteBuf buf );

    void fromBytes( PacketByteBuf buf );

    default void open( PlayerEntity player )
    {
        ContainerProviderRegistry.INSTANCE.openContainer( getId(), player, this::toBytes );
    }

    static <C extends Container, T extends ContainerType<C>> void register( Supplier<T> containerType, BiFunction<T, PlayerEntity, C> factory )
    {
        ContainerProviderRegistry.INSTANCE.registerFactory( containerType.get().getId(), ( id, player, packet ) -> {
            T type = containerType.get();
            type.fromBytes( packet );
            return factory.apply( type, player );
        } );
    }

    static <C extends Container, T extends ContainerType<C>> void registerGui( Supplier<T> containerType, BiFunction<T, PlayerEntity, ContainerGui> factory )
    {
        GuiProviderRegistry.INSTANCE.registerFactory( containerType.get().getId(), ( id, player, packet ) -> {
            T type = containerType.get();
            type.fromBytes( packet );
            return factory.apply( type, player );
        } );
    }

    static <C extends Container, T extends ContainerType<C>> void registerGui( Supplier<T> containerType, GuiFactory<C> factory )
    {
        GuiProviderRegistry.INSTANCE.registerFactory( containerType.get().getId(), factory );
    }
}
