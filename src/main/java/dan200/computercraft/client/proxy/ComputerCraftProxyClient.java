/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.ClientTableFormatter;
import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.gui.*;
import dan200.computercraft.client.render.TileEntityMonitorRenderer;
import dan200.computercraft.shared.command.text.TableBuilder;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.inventory.ContainerViewComputer;
import dan200.computercraft.shared.network.container.*;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.proxy.ComputerCraftProxyCommon;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.inventory.ContainerTurtle;
import dan200.computercraft.shared.util.Colour;
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.fabricmc.fabric.client.render.BlockEntityRendererRegistry;
import net.fabricmc.fabric.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;

public class ComputerCraftProxyClient extends ComputerCraftProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();
        registerHandlers();
        registerContainers();

        // Register any client-specific commands
        // ClientCommandHandler.instance.registerCommand( CommandCopy.INSTANCE );
    }

    @Override
    public void init()
    {
        super.init();
        ColorProviderRegistry.ITEM.register(
            ( stack, layer ) -> layer == 0 ? 0xFFFFFF : ComputerCraft.Items.disk.getColour( stack ),
            ComputerCraft.Items.disk
        );

        ColorProviderRegistry.ITEM.register( ( stack, layer ) -> {
            switch( layer )
            {
                default:
                    return 0xFFFFFF;
                case 1:
                {
                    // Frame colour
                    int colour = ItemPocketComputer.getColourDirect( stack );
                    return colour == -1 ? 0xFFFFFF : colour;
                }
                case 2:
                {
                    // Light colour
                    int colour = ItemPocketComputer.getLightState( stack );
                    return colour == -1 ? Colour.Black.getHex() : colour;
                }
            }
        }, ComputerCraft.Items.pocketComputerNormal, ComputerCraft.Items.pocketComputerAdvanced );

        // Setup renderers
        // ClientRegistry.bindTileEntitySpecialRenderer( TileCable.class, new TileEntityCableRenderer() );
        BlockEntityRendererRegistry.INSTANCE.register( TileMonitor.class, new TileEntityMonitorRenderer() );
    }

    @Override
    public File getWorldDir( World world )
    {
        return world.getSaveHandler().getWorldDir();
    }

    private void registerHandlers()
    {
        ClientTickEvent.CLIENT.register( s -> {
            FrameInfo.instance().onTick();
            ComputerCraft.clientComputerRegistry.update();
        } );
    }

    private void registerContainers()
    {
        ContainerType.registerGui( BlockEntityContainerType::computer, ( packet, player ) ->
            new GuiComputer( (TileComputer) packet.getBlockEntity( player ) ) );
        ContainerType.registerGui( BlockEntityContainerType::diskDrive, GuiDiskDrive::new );
        ContainerType.registerGui( BlockEntityContainerType::printer, GuiPrinter::new );
        ContainerType.registerGui( BlockEntityContainerType::turtle, ( packet, player ) -> {
            TileTurtle turtle = (TileTurtle) packet.getBlockEntity( player );
            return new GuiTurtle( turtle, new ContainerTurtle( player.inventory, turtle.getAccess(), turtle.getClientComputer() ) );
        } );

        ContainerType.registerGui( PocketComputerContainerType::new, GuiPocketComputer::new );
        ContainerType.registerGui( PrintoutContainerType::new, GuiPrintout::new );
        ContainerType.registerGui( ViewComputerContainerType::new, ( packet, player ) -> {
            ClientComputer computer = ComputerCraft.clientComputerRegistry.get( packet.instanceId );
            if( computer == null )
            {
                ComputerCraft.clientComputerRegistry.add( packet.instanceId, computer = new ClientComputer( packet.instanceId ) );
            }

            ContainerViewComputer container = new ContainerViewComputer( computer );
            return new GuiComputer( container, packet.family, computer, packet.width, packet.height );
        } );
    }

    @Override
    public void playRecordClient( BlockPos pos, SoundEvent record, String info )
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.world.playRecord( pos, record );
        if( info != null ) mc.hudInGame.setRecordPlayingOverlay( info );
    }

    @Override
    public void showTableClient( TableBuilder table )
    {
        ClientTableFormatter.INSTANCE.display( table );
    }

    /*
    public class ForgeHandlers
    {
        @SubscribeEvent
        public void onWorldUnload( WorldEvent.Unload event )
        {
            if( event.getWorld().isClient )
            {
                ClientMonitor.destroyAll();
            }
        }
    }
    */
}
