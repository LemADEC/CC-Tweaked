/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.client.proxy.CCTurtleProxyClient;
import dan200.computercraft.client.proxy.ComputerCraftProxyClient;
import dan200.computercraft.core.apis.AddressPredicate;
import dan200.computercraft.core.filesystem.ResourceMount;
import dan200.computercraft.server.proxy.CCTurtleProxyServer;
import dan200.computercraft.server.proxy.ComputerCraftProxyServer;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ClientComputerRegistry;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.media.items.ItemDisk;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.media.items.ItemTreasureDisk;
import dan200.computercraft.shared.network.NetworkMessage;
import dan200.computercraft.shared.network.client.PlayRecordClientMessage;
import dan200.computercraft.shared.network.container.BlockEntityContainerType;
import dan200.computercraft.shared.network.container.PocketComputerContainerType;
import dan200.computercraft.shared.network.container.PrintoutContainerType;
import dan200.computercraft.shared.network.container.ViewComputerContainerType;
import dan200.computercraft.shared.peripheral.diskdrive.BlockDiskDrive;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.modem.wired.BlockCable;
import dan200.computercraft.shared.peripheral.modem.wired.BlockWiredModemFull;
import dan200.computercraft.shared.peripheral.modem.wired.ItemBlockCable;
import dan200.computercraft.shared.peripheral.modem.wireless.BlockWirelessModem;
import dan200.computercraft.shared.peripheral.monitor.BlockMonitor;
import dan200.computercraft.shared.peripheral.printer.BlockPrinter;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.peripheral.speaker.BlockSpeaker;
import dan200.computercraft.shared.pocket.inventory.ContainerPocketComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.peripherals.PocketModem;
import dan200.computercraft.shared.pocket.peripherals.PocketSpeaker;
import dan200.computercraft.shared.proxy.ICCTurtleProxy;
import dan200.computercraft.shared.proxy.IComputerCraftProxy;
import dan200.computercraft.shared.turtle.blocks.BlockTurtle;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.items.ItemTurtle;
import dan200.computercraft.shared.turtle.upgrades.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadServerPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ComputerCraft implements ModInitializer
{
    public static final String MOD_ID = "computercraft";
    public static final int DATAFIXER_VERSION = 0;

    // Configuration options
    private static final String[] DEFAULT_HTTP_WHITELIST = new String[] { "*" };
    private static final String[] DEFAULT_HTTP_BLACKLIST = new String[] {
        "127.0.0.0/8",
        "10.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "fd00::/8",
    };

    public static boolean http_enable = true;
    public static boolean http_websocket_enable = true;
    public static AddressPredicate http_whitelist = new AddressPredicate( DEFAULT_HTTP_WHITELIST );
    public static AddressPredicate http_blacklist = new AddressPredicate( DEFAULT_HTTP_BLACKLIST );
    public static boolean disable_lua51_features = false;
    public static String default_computer_settings = "";
    public static boolean debug_enable = false;
    public static int computer_threads = 1;
    public static boolean logPeripheralErrors = false;

    public static boolean enableCommandBlock = false;
    public static boolean turtlesNeedFuel = true;
    public static int turtleFuelLimit = 20000;
    public static int advancedTurtleFuelLimit = 100000;
    public static boolean turtlesObeyBlockProtection = true;
    public static boolean turtlesCanPush = true;
    // public static EnumSet<TurtleAction> turtleDisabledActions = EnumSet.noneOf( TurtleAction.class );

    public static final int terminalWidth_computer = 51;
    public static final int terminalHeight_computer = 19;

    public static final int terminalWidth_turtle = 39;
    public static final int terminalHeight_turtle = 13;

    public static final int terminalWidth_pocketComputer = 26;
    public static final int terminalHeight_pocketComputer = 20;

    public static int modem_range = 64;
    public static int modem_highAltitudeRange = 384;
    public static int modem_rangeDuringStorm = 64;
    public static int modem_highAltitudeRangeDuringStorm = 384;

    public static int computerSpaceLimit = 1000 * 1000;
    public static int floppySpaceLimit = 125 * 1000;
    public static int maximumFilesOpen = 128;

    public static int maxNotesPerTick = 8;

    // Blocks and Items
    public static class Blocks
    {
        public static BlockComputer computerNormal;
        public static BlockComputer computerAdvanced;
        public static BlockComputer computerCommand;

        public static BlockTurtle turtleNormal;
        public static BlockTurtle turtleAdvanced;

        public static BlockSpeaker speaker;
        public static BlockDiskDrive diskDrive;
        public static BlockPrinter printer;

        public static BlockMonitor monitorNormal;
        public static BlockMonitor monitorAdvanced;

        public static BlockWirelessModem wirelessModemNormal;
        public static BlockWirelessModem wirelessModemAdvanced;

        public static BlockWiredModemFull wiredModemFull;
        public static BlockCable cable;
    }

    public static class Items
    {
        public static ItemComputer computerNormal;
        public static ItemComputer computerAdvanced;
        public static ItemComputer computerCommand;

        public static ItemPocketComputer pocketComputerNormal;
        public static ItemPocketComputer pocketComputerAdvanced;

        public static ItemTurtle turtleNormal;
        public static ItemTurtle turtleAdvanced;

        public static ItemDisk disk;
        public static ItemTreasureDisk treasureDisk;

        public static ItemPrintout printedPage;
        public static ItemPrintout printedPages;
        public static ItemPrintout printedBook;

        public static ItemBlockCable.Cable cable;
        public static ItemBlockCable.WiredModem wiredModem;
    }

    public static class Upgrades
    {
        public static TurtleModem wirelessModemNormal;
        public static TurtleModem wirelessModemAdvanced;
        public static TurtleSpeaker speaker;

        public static TurtleCraftingTable craftingTable;
        public static TurtleSword diamondSword;
        public static TurtleShovel diamondShovel;
        public static TurtleTool diamondPickaxe;
        public static TurtleAxe diamondAxe;
        public static TurtleHoe diamondHoe;
    }

    public static class PocketUpgrades
    {
        public static PocketModem wirelessModem;
        public static PocketModem advancedModem;
        public static PocketSpeaker pocketSpeaker;
    }

    /*
    public static class Config
    {
        public static Configuration config;

        public static Property http_enable;
        public static Property http_websocket_enable;
        public static Property http_whitelist;
        public static Property http_blacklist;
        public static Property disable_lua51_features;
        public static Property default_computer_settings;
        public static Property debug_enable;
        public static Property computer_threads;
        public static Property logPeripheralErrors;

        public static Property enableCommandBlock;
        public static Property turtlesNeedFuel;
        public static Property turtleFuelLimit;
        public static Property advancedTurtleFuelLimit;
        public static Property turtlesObeyBlockProtection;
        public static Property turtlesCanPush;
        public static Property turtleDisabledActions;

        public static Property modem_range;
        public static Property modem_highAltitudeRange;
        public static Property modem_rangeDuringStorm;
        public static Property modem_highAltitudeRangeDuringStorm;

        public static Property computerSpaceLimit;
        public static Property floppySpaceLimit;
        public static Property maximumFilesOpen;
        public static Property maxNotesPerTick;
    }
    */

    // Registries
    public static ClientComputerRegistry clientComputerRegistry = new ClientComputerRegistry();
    public static ServerComputerRegistry serverComputerRegistry = new ServerComputerRegistry();

    // Creative
    public static ItemGroup mainCreativeTab;

    // Logging
    public static Logger log = LogManager.getLogger( MOD_ID );

    // Implementation
    public static ComputerCraft instance;
    public static IComputerCraftProxy proxy;
    public static ICCTurtleProxy turtleProxy;

    public ComputerCraft()
    {
        instance = this;
    }

    @Override
    public void onInitialize()
    {
        if( FabricLoader.INSTANCE.getEnvironmentHandler().getEnvironmentType() == EnvType.CLIENT )
        {
            proxy = new ComputerCraftProxyClient();
            turtleProxy = new CCTurtleProxyClient();
        }
        else
        {
            proxy = new ComputerCraftProxyServer();
            turtleProxy = new CCTurtleProxyServer();
        }

        proxy.preInit();
        turtleProxy.preInit();
        proxy.init();
        turtleProxy.init();
    }

    /*
    @Mod.EventHandler
    public void onServerStopped( FMLServerStoppedEvent event )
    {
        if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
        {
            ComputerCraft.serverComputerRegistry.reset();
            WirelessNetwork.resetNetworks();
            Tracking.reset();
        }
    }
    */

    public static void playRecord( SoundEvent record, String recordInfo, World world, BlockPos pos )
    {
        PlayRecordClientMessage packet = record == null
            ? new PlayRecordClientMessage( pos )
            : new PlayRecordClientMessage( pos, record, recordInfo );

        ComputerCraft.sendToAllAround( packet, world, new Vec3d( pos ), 64 );
    }

    public static void openDiskDriveGUI( PlayerEntity player, TileDiskDrive drive )
    {
        BlockEntityContainerType.diskDrive( drive.getPos() ).open( player );
    }

    public static void openComputerGUI( PlayerEntity player, TileComputer computer )
    {
        // Send an initial update of the terminal state
        ServerComputer server = computer.getServerComputer();
        if( server != null ) server.sendTerminalState( player );

        // And open the container
        BlockEntityContainerType.computer( computer.getPos() ).open( player );
    }

    public static void openPrinterGUI( PlayerEntity player, TilePrinter printer )
    {
        BlockEntityContainerType.printer( printer.getPos() ).open( player );
    }

    public static void openTurtleGUI( PlayerEntity player, TileTurtle turtle )
    {
        // Send an initial update of the terminal state
        ServerComputer server = turtle.getServerComputer();
        if( server != null ) server.sendTerminalState( player );

        BlockEntityContainerType.turtle( turtle.getPos() ).open( player );
    }

    public static void openPrintoutGUI( PlayerEntity player, Hand hand )
    {
        ItemStack stack = player.getStackInHand( hand );
        Item item = stack.getItem();
        if( !(item instanceof ItemPrintout) ) return;

        new PrintoutContainerType( hand ).open( player );
    }

    public static void openPocketComputerGUI( PlayerEntity player, Hand hand )
    {
        ItemStack stack = player.getStackInHand( hand );
        Item item = stack.getItem();
        if( !(item instanceof ItemPocketComputer) ) return;

        ContainerPocketComputer container = new ContainerPocketComputer( player, hand );
        IComputer computer = container.getComputer();
        if( !(computer instanceof ServerComputer) ) return;

        // Send an initial update of the terminal state
        ((ServerComputer) computer).sendTerminalState( player );

        new PocketComputerContainerType( hand ).open( player );
    }

    public static void openComputerGUI( PlayerEntity player, ServerComputer computer )
    {
        // Send an initial update of the terminal state
        computer.sendTerminalState( player );

        // And open the container
        new ViewComputerContainerType( computer ).open( player );
    }

    public static File getBaseDir()
    {
        return FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().getFile( "." );
    }

    public static File getResourcePackDir()
    {
        // TODO: Use data packs instead
        return new File( getBaseDir(), "resourcepacks" );
    }

    public static File getWorldDir( World world )
    {
        return proxy.getWorldDir( world );
    }

    private static PacketByteBuf encode( NetworkMessage packet )
    {
        PacketByteBuf buffer = new PacketByteBuf( Unpooled.buffer() );
        packet.toBytes( buffer );
        return buffer;
    }

    public static void sendToPlayer( PlayerEntity player, NetworkMessage packet )
    {
        ((ServerPlayerEntity) player).networkHandler.sendPacket(
            new CustomPayloadClientPacket( packet.getId(), encode( packet ) )
        );
    }

    public static void sendToAllPlayers( NetworkMessage packet )
    {
        FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().getPlayerManager().sendToAll(
            new CustomPayloadClientPacket( packet.getId(), encode( packet ) )
        );
    }

    public static void sendToServer( NetworkMessage packet )
    {
        MinecraftClient.getInstance().player.networkHandler.sendPacket(
            new CustomPayloadServerPacket( packet.getId(), encode( packet ) )
        );
    }

    public static void sendToAllAround( NetworkMessage packet, World world, Vec3d pos, double range )
    {
        FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().getPlayerManager().sendToAround(
            null, pos.x, pos.y, pos.z, range, world.getDimension().getType(),
            new CustomPayloadClientPacket( packet.getId(), encode( packet ) )
        );
    }

    public static boolean isPlayerOpped( PlayerEntity player )
    {
        MinecraftServer server = player.getServer();
        if( server != null )
        {
            return server.getPlayerManager().isOperator( player.getGameProfile() );
        }
        return false;
    }

    public static boolean isBlockEnterable( World world, BlockPos pos, PlayerEntity player )
    {
        MinecraftServer server = player.getServer();
        if( server != null && !world.isClient )
        {
            if( server.isSpawnProtected( world, pos, player ) )
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isBlockEditable( World world, BlockPos pos, PlayerEntity player )
    {
        MinecraftServer server = player.getServer();
        if( server != null && !world.isClient )
        {
            if( server.isSpawnProtected( world, pos, player ) )
            {
                return false;
            }
        }

        return true;
    }

    static IMount createResourceMount( String domain, String subPath )
    {
        ReloadableResourceManager manager = FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().getDataManager();
        ResourceMount mount = new ResourceMount( domain, subPath, manager );
        return mount.exists( "" ) ? mount : null;
    }

    public static InputStream getResourceFile( String domain, String subPath )
    {
        ReloadableResourceManager manager = FabricLoader.INSTANCE.getEnvironmentHandler().getServerInstance().getDataManager();
        try
        {
            Resource resource = manager.getResource( new Identifier( domain, subPath ) );
            return resource == null ? null : resource.getInputStream();
        }
        catch( IOException ignored )
        {
            return null;
        }
    }
}
