/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.core.computer.MainThread;
import dan200.computercraft.core.tracking.Tracking;
import dan200.computercraft.shared.command.CommandComputerCraft;
import dan200.computercraft.shared.command.arguments.ArgumentSerializers;
import dan200.computercraft.shared.common.ColourableRecipe;
import dan200.computercraft.shared.common.ContainerHeldItem;
import dan200.computercraft.shared.common.DefaultBundledRedstoneProvider;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.TileCommandComputer;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ContainerComputer;
import dan200.computercraft.shared.computer.inventory.ContainerViewComputer;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.computer.recipe.ComputerUpgradeRecipe;
import dan200.computercraft.shared.media.DefaultMediaProvider;
import dan200.computercraft.shared.media.items.ItemDisk;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.media.items.ItemTreasureDisk;
import dan200.computercraft.shared.media.recipes.DiskRecipe;
import dan200.computercraft.shared.media.recipes.PrintoutRecipe;
import dan200.computercraft.shared.network.NetworkMessage;
import dan200.computercraft.shared.network.client.*;
import dan200.computercraft.shared.network.container.*;
import dan200.computercraft.shared.network.server.ComputerActionServerMessage;
import dan200.computercraft.shared.network.server.ComputerServerMessage;
import dan200.computercraft.shared.network.server.QueueEventServerMessage;
import dan200.computercraft.shared.network.server.RequestComputerMessage;
import dan200.computercraft.shared.peripheral.DefaultPeripheralProvider;
import dan200.computercraft.shared.peripheral.commandblock.CommandBlockPeripheralProvider;
import dan200.computercraft.shared.peripheral.diskdrive.BlockDiskDrive;
import dan200.computercraft.shared.peripheral.diskdrive.ContainerDiskDrive;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.modem.wired.*;
import dan200.computercraft.shared.peripheral.modem.wireless.BlockWirelessModem;
import dan200.computercraft.shared.peripheral.modem.wireless.TileWirelessModem;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessNetwork;
import dan200.computercraft.shared.peripheral.monitor.BlockMonitor;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.peripheral.printer.BlockPrinter;
import dan200.computercraft.shared.peripheral.printer.ContainerPrinter;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.peripheral.speaker.BlockSpeaker;
import dan200.computercraft.shared.peripheral.speaker.TileSpeaker;
import dan200.computercraft.shared.pocket.inventory.ContainerPocketComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.peripherals.PocketModem;
import dan200.computercraft.shared.pocket.peripherals.PocketSpeaker;
import dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe;
import dan200.computercraft.shared.turtle.blocks.BlockTurtle;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.inventory.ContainerTurtle;
import dan200.computercraft.shared.turtle.items.ItemTurtle;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.block.FabricBlockSettings;
import net.fabricmc.fabric.commands.CommandRegistry;
import net.fabricmc.fabric.events.ServerEvent;
import net.fabricmc.fabric.events.TickEvent;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.recipe.RecipeSerializers;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.ModifiableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.io.File;

public abstract class ComputerCraftProxyCommon implements IComputerCraftProxy
{
    @Override
    public void preInit()
    {
        // Creative tab
        ComputerCraft.mainCreativeTab = FabricItemGroupBuilder
            .create( new Identifier( ComputerCraft.MOD_ID, "main" ) )
            .icon( () -> new ItemStack( ComputerCraft.Items.computerNormal ) )
            .build();

        // Registries
        registerBlocks( Registry.BLOCK );
        registerBlockEntities( (ModifiableRegistry<BlockEntityType<?>>) Registry.BLOCK_ENTITY );
        registerItems( Registry.ITEM );

        RecipeSerializers.register( ColourableRecipe.SERIALIZER );
        RecipeSerializers.register( ComputerUpgradeRecipe.SERIALIZER );
        RecipeSerializers.register( PocketComputerUpgradeRecipe.SERIALIZER );
        RecipeSerializers.register( DiskRecipe.SERIALIZER );
        RecipeSerializers.register( PrintoutRecipe.SERIALIZER );

        ArgumentSerializers.register();

        CommandRegistry.INSTANCE.register( false, CommandComputerCraft::register );
    }

    @Override
    public void init()
    {
        registerNetwork();
        registerContainers();
        registerProviders();
        registerHandlers();

        // if( Loader.isModLoaded( ModCharset.MODID ) ) IntegrationCharset.register();
    }

    @Override
    public abstract File getWorldDir( World world );

    private void registerBlocks( ModifiableRegistry<Block> registry )
    {
        // Computers
        ComputerCraft.Blocks.computerNormal = registry.register( new Identifier( ComputerCraft.MOD_ID, "computer_normal" ),
            new BlockComputer(
                FabricBlockSettings.of( Material.STONE ).hardness( 2.0f ).build(),
                ComputerFamily.Normal, TileComputer.FACTORY_NORMAL
            ) );

        ComputerCraft.Blocks.computerAdvanced = registry.register( new Identifier( ComputerCraft.MOD_ID, "computer_advanced" ),
            new BlockComputer(
                FabricBlockSettings.of( Material.STONE ).hardness( 2.0f ).build(),
                ComputerFamily.Advanced, TileComputer.FACTORY_ADVANCED
            ) );

        ComputerCraft.Blocks.computerCommand = registry.register( new Identifier( ComputerCraft.MOD_ID, "computer_command" ),
            new BlockComputer(
                FabricBlockSettings.of( Material.STONE ).strength( -1, 6000000.0F ).build(),
                ComputerFamily.Command, TileCommandComputer.FACTORY
            ) );

        // Turtles
        ComputerCraft.Blocks.turtleNormal = registry.register( new Identifier( ComputerCraft.MOD_ID, "turtle_normal" ),
            new BlockTurtle(
                FabricBlockSettings.of( Material.STONE ).hardness( 2.5f ).build(),
                ComputerFamily.Normal, TileTurtle.FACTORY_NORMAL
            ) );

        ComputerCraft.Blocks.turtleAdvanced = registry.register( new Identifier( ComputerCraft.MOD_ID, "turtle_advanced" ),
            new BlockTurtle(
                FabricBlockSettings.of( Material.STONE ).hardness( 2.5f ).build(),
                ComputerFamily.Normal, TileTurtle.FACTORY_ADVANCED
            ) );

        // Peripherals
        ComputerCraft.Blocks.speaker = registry.register( new Identifier( ComputerCraft.MOD_ID, "speaker" ),
            new BlockSpeaker(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TileSpeaker.FACTORY
            ) );

        ComputerCraft.Blocks.diskDrive = registry.register( new Identifier( ComputerCraft.MOD_ID, "disk_drive" ),
            new BlockDiskDrive(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TileDiskDrive.FACTORY
            ) );

        ComputerCraft.Blocks.monitorNormal = registry.register( new Identifier( ComputerCraft.MOD_ID, "monitor_normal" ),
            new BlockMonitor(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TileMonitor.FACTORY_NORMAL
            ) );

        ComputerCraft.Blocks.monitorAdvanced = registry.register( new Identifier( ComputerCraft.MOD_ID, "monitor_advanced" ),
            new BlockMonitor(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TileMonitor.FACTORY_ADVANCED
            ) );

        ComputerCraft.Blocks.printer = registry.register( new Identifier( ComputerCraft.MOD_ID, "printer" ),
            new BlockPrinter(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TilePrinter.FACTORY
            ) );

        ComputerCraft.Blocks.wirelessModemNormal = registry.register( new Identifier( ComputerCraft.MOD_ID, "wireless_modem_normal" ),
            new BlockWirelessModem(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TileWirelessModem.FACTORY_NORMAL
            ) );

        ComputerCraft.Blocks.wirelessModemAdvanced = registry.register( new Identifier( ComputerCraft.MOD_ID, "wireless_modem_advanced" ),
            new BlockWirelessModem(
                FabricBlockSettings.of( Material.STONE ).hardness( 2 ).build(),
                TileWirelessModem.FACTORY_ADVANCED
            ) );

        ComputerCraft.Blocks.wiredModemFull = registry.register( new Identifier( ComputerCraft.MOD_ID, "wired_modem_full" ),
            new BlockWiredModemFull(
                FabricBlockSettings.of( Material.STONE ).hardness( 1.5f ).build(),
                TileWiredModemFull.FACTORY
            ) );

        ComputerCraft.Blocks.cable = registry.register( new Identifier( ComputerCraft.MOD_ID, "cable" ),
            new BlockCable(
                FabricBlockSettings.of( Material.STONE ).hardness( 1.5f ).build(),
                TileCable.FACTORY
            ) );
    }

    private void registerBlockEntities( ModifiableRegistry<BlockEntityType<?>> registry )
    {
        // Computers
        TileComputer.FACTORY_NORMAL.register( registry );
        TileComputer.FACTORY_ADVANCED.register( registry );
        TileCommandComputer.FACTORY.register( registry );

        // Turtles
        TileTurtle.FACTORY_NORMAL.register( registry );
        TileTurtle.FACTORY_ADVANCED.register( registry );

        // Peripherals
        TileSpeaker.FACTORY.register( registry );
        TileDiskDrive.FACTORY.register( registry );
        TilePrinter.FACTORY.register( registry );

        TileMonitor.FACTORY_NORMAL.register( registry );
        TileMonitor.FACTORY_ADVANCED.register( registry );

        TileWirelessModem.FACTORY_NORMAL.register( registry );
        TileWirelessModem.FACTORY_ADVANCED.register( registry );
        TileCable.FACTORY.register( registry );
        TileWiredModemFull.FACTORY.register( registry );
    }

    private static <T extends BlockItem> T register( ModifiableRegistry<Item> registry, T item )
    {
        return registry.register( Registry.BLOCK.getId( item.getBlock() ), item );
    }

    private static Item.Settings defaultItem()
    {
        return new Item.Settings().itemGroup( ComputerCraft.mainCreativeTab );
    }

    private void registerItems( ModifiableRegistry<Item> registry )
    {
        // Computer
        ComputerCraft.Items.computerNormal = register( registry,
            new ItemComputer( ComputerCraft.Blocks.computerNormal, defaultItem() )
        );

        ComputerCraft.Items.computerAdvanced = register( registry,
            new ItemComputer( ComputerCraft.Blocks.computerAdvanced, defaultItem() )
        );

        ComputerCraft.Items.computerCommand = register( registry,
            new ItemComputer( ComputerCraft.Blocks.computerCommand, defaultItem() )
        );

        // Turtle
        ComputerCraft.Items.turtleNormal = register( registry,
            new ItemTurtle( ComputerCraft.Blocks.turtleNormal, defaultItem() )
        );

        ComputerCraft.Items.turtleAdvanced = register( registry,
            new ItemTurtle( ComputerCraft.Blocks.turtleAdvanced, defaultItem() )
        );

        // Pocket computer
        ComputerCraft.Items.pocketComputerNormal = registry.register( new Identifier( ComputerCraft.MOD_ID, "pocket_computer_normal" ),
            new ItemPocketComputer( defaultItem().stackSize( 1 ), ComputerFamily.Normal )
        );

        ComputerCraft.Items.pocketComputerAdvanced = registry.register( new Identifier( ComputerCraft.MOD_ID, "pocket_computer_advanced" ),
            new ItemPocketComputer( defaultItem().stackSize( 1 ), ComputerFamily.Advanced )
        );

        // Floppy disk
        ComputerCraft.Items.disk = registry.register( new Identifier( ComputerCraft.MOD_ID, "disk" ),
            new ItemDisk( defaultItem().stackSize( 1 ) )
        );

        ComputerCraft.Items.treasureDisk = registry.register( new Identifier( ComputerCraft.MOD_ID, "treasure_disk" ),
            new ItemTreasureDisk( defaultItem().stackSize( 1 ) )
        );

        // Printouts
        ComputerCraft.Items.printedPage = registry.register( new Identifier( ComputerCraft.MOD_ID, "printed_page" ),
            new ItemPrintout( defaultItem().stackSize( 1 ), ItemPrintout.Type.PAGE )
        );

        ComputerCraft.Items.printedPages = registry.register( new Identifier( ComputerCraft.MOD_ID, "printed_pages" ),
            new ItemPrintout( defaultItem().stackSize( 1 ), ItemPrintout.Type.PAGES )
        );

        ComputerCraft.Items.printedBook = registry.register( new Identifier( ComputerCraft.MOD_ID, "printed_book" ),
            new ItemPrintout( defaultItem().stackSize( 1 ), ItemPrintout.Type.BOOK )
        );

        // Peripherals
        register( registry, new BlockItem( ComputerCraft.Blocks.speaker, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.diskDrive, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.printer, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.monitorNormal, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.monitorAdvanced, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.wirelessModemNormal, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.wirelessModemAdvanced, defaultItem() ) );
        register( registry, new BlockItem( ComputerCraft.Blocks.wiredModemFull, defaultItem() ) );

        ComputerCraft.Items.cable = registry.register( new Identifier( ComputerCraft.MOD_ID, "cable" ),
            new ItemBlockCable.Cable( ComputerCraft.Blocks.cable, defaultItem() )
        );
        ComputerCraft.Items.wiredModem = registry.register( new Identifier( ComputerCraft.MOD_ID, "wired_modem" ),
            new ItemBlockCable.WiredModem( ComputerCraft.Blocks.cable, defaultItem() )
        );
    }

    private void registerProviders()
    {
        // Register peripheral providers
        ComputerCraftAPI.registerPeripheralProvider( new DefaultPeripheralProvider() );
        if( ComputerCraft.enableCommandBlock )
        {
            ComputerCraftAPI.registerPeripheralProvider( new CommandBlockPeripheralProvider() );
        }

        // Register bundled power providers
        ComputerCraftAPI.registerBundledRedstoneProvider( new DefaultBundledRedstoneProvider() );

        // Register media providers
        ComputerCraftAPI.registerMediaProvider( new DefaultMediaProvider() );

        // Register pocket upgrades
        ComputerCraftAPI.registerPocketUpgrade( ComputerCraft.PocketUpgrades.wirelessModem = new PocketModem( false ) );
        ComputerCraftAPI.registerPocketUpgrade( ComputerCraft.PocketUpgrades.advancedModem = new PocketModem( true ) );
        ComputerCraftAPI.registerPocketUpgrade( ComputerCraft.PocketUpgrades.pocketSpeaker = new PocketSpeaker() );

        // Register network providers
        // CapabilityWiredElement.register();
    }

    private void registerHandlers()
    {
        TickEvent.SERVER.register( s -> {
            MainThread.executePendingTasks();
            ComputerCraft.serverComputerRegistry.update();
        } );

        ServerEvent.START.register( s -> {
            ComputerCraft.serverComputerRegistry.reset();
            WirelessNetwork.resetNetworks();
            Tracking.reset();
        } );
    }

    private void registerNetwork()
    {
        // Server messages

        ComputerServerMessage.register( ComputerActionServerMessage::new, ( computer, packet ) -> {
            switch( packet.getAction() )
            {
                case TURN_ON:
                    computer.turnOn();
                    break;
                case REBOOT:
                    computer.reboot();
                    break;
                case SHUTDOWN:
                    computer.shutdown();
                    break;
            }
        } );

        ComputerServerMessage.register( QueueEventServerMessage::new, ( computer, packet ) ->
            computer.queueEvent( packet.getEvent(), packet.getArgs() ) );

        NetworkMessage.registerMainThread( CustomPayloadPacketRegistry.SERVER, RequestComputerMessage::new, ( context, packet ) -> {
            ServerComputer computer = ComputerCraft.serverComputerRegistry.get( packet.getInstance() );
            if( computer != null ) computer.sendComputerState( context.getPlayer() );
        } );

        // Client messages

        NetworkMessage.registerMainThread( CustomPayloadPacketRegistry.CLIENT, PlayRecordClientMessage::new, ( computer, packet ) ->
            playRecordClient( packet.getPos(), packet.getSoundEvent(), packet.getName() ) );

        ComputerClientMessage.register( ComputerDataClientMessage::new, ( computer, packet ) ->
            computer.setState( packet.getState(), packet.getUserData() ) );

        ComputerClientMessage.register( ComputerTerminalClientMessage::new, ( computer, packet ) ->
            computer.readDescription( packet.getTag() ) );

        NetworkMessage.registerMainThread( CustomPayloadPacketRegistry.CLIENT, ComputerDeletedClientMessage::new, ( context, packet ) ->
            ComputerCraft.clientComputerRegistry.remove( packet.getInstanceId() ) );

        NetworkMessage.registerMainThread( CustomPayloadPacketRegistry.CLIENT, ChatTableClientMessage::new, ( context, packet ) ->
            showTableClient( packet.getTable() ) );
    }

    private void registerContainers()
    {
        ContainerType.register( BlockEntityContainerType::computer, ( packet, player ) ->
            new ContainerComputer( (TileComputer) packet.getBlockEntity( player ) ) );
        ContainerType.register( BlockEntityContainerType::turtle, ( packet, player ) -> {
            TileTurtle turtle = (TileTurtle) packet.getBlockEntity( player );
            return new ContainerTurtle( player.inventory, turtle.getAccess(), turtle.getServerComputer() );
        } );
        ContainerType.register( BlockEntityContainerType::diskDrive, ( packet, player ) ->
            new ContainerDiskDrive( player.inventory, (TileDiskDrive) packet.getBlockEntity( player ) ) );
        ContainerType.register( BlockEntityContainerType::printer, ( packet, player ) ->
            new ContainerPrinter( player.inventory, (TilePrinter) packet.getBlockEntity( player ) ) );

        ContainerType.register( PocketComputerContainerType::new, ( packet, player ) -> new ContainerPocketComputer( player, packet.hand ) );
        ContainerType.register( PrintoutContainerType::new, ( packet, player ) -> new ContainerHeldItem( player, packet.hand ) );
        ContainerType.register( ViewComputerContainerType::new, ( packet, player ) -> new ContainerViewComputer( ComputerCraft.serverComputerRegistry.get( packet.instanceId ) ) );
    }

    /*
    public class ForgeHandlers implements IGuiHandler
    {
        @SubscribeEvent
        public void onConnectionOpened( FMLNetworkEvent.ClientConnectedToServerEvent event )
        {
            ComputerCraft.clientComputerRegistry.reset();
        }

        @SubscribeEvent
        public void onConnectionClosed( FMLNetworkEvent.ClientDisconnectionFromServerEvent event )
        {
            ComputerCraft.clientComputerRegistry.reset();
        }

        @SubscribeEvent
        public void onConfigChanged( ConfigChangedEvent.OnConfigChangedEvent event )
        {
            if( event.getModID().equals( ComputerCraft.MOD_ID ) )
            {
                ComputerCraft.syncConfig();
            }
        }

        @SubscribeEvent
        public void onContainerOpen( PlayerContainerEvent.Open event )
        {
            // If we're opening a computer container then broadcast the terminal state
            Container container = event.getContainer();
            if( container instanceof IContainerComputer )
            {
                IComputer computer = ((IContainerComputer) container).getComputer();
                if( computer instanceof ServerComputer )
                {
                    ((ServerComputer) computer).sendTerminalState( event.getPlayerEntity() );
                }
            }
        }
    */
}
