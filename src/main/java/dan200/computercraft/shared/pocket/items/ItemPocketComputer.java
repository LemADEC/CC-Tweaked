/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.items;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.PocketUpgrades;
import dan200.computercraft.shared.common.IColouredItem;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.pocket.apis.PocketAPI;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemPocketComputer extends Item implements IComputerItem, IMedia, IColouredItem
{
    private static final String TAG_ID = "computer_id";
    private static final String TAG_COLOUR = "colour";

    private static final String TAG_UPGRADE = "upgrade";
    private static final String TAG_UPGRADE_INFO = "upgrade_info";
    private static final String TAG_LIGHT = "modem_light";

    private static final String TAG_INSTANCE = "instance_id";
    private static final String TAG_SESSION = "session_id";

    private final ComputerFamily family;

    public ItemPocketComputer( Settings settings, ComputerFamily family )
    {
        super( settings );
        addProperty( STATE, STATE_GETTER );
        this.family = family;
    }

    public ItemStack create( int id, String label, int colour, IPocketUpgrade upgrade )
    {
        ItemStack result = new ItemStack( this );
        if( id >= 0 ) result.getOrCreateTag().putInt( TAG_ID, id );
        if( label != null ) result.setDisplayName( new StringTextComponent( label ) );
        if( upgrade != null ) result.getOrCreateTag().putString( TAG_UPGRADE, upgrade.getUpgradeID().toString() );
        if( colour != -1 ) result.getOrCreateTag().putInt( TAG_COLOUR, colour );
        return result;
    }

    @Override
    public void addStacksForDisplay( ItemGroup group, DefaultedList<ItemStack> stacks )
    {
        if( !isInItemGroup( group ) ) return;
        stacks.add( create( -1, null, -1, null ) );
        for( IPocketUpgrade upgrade : PocketUpgrades.getVanillaUpgrades() )
        {
            stacks.add( create( -1, null, -1, upgrade ) );
        }
    }

    @Override
    public void onUpdate( ItemStack stack, World world, Entity entity, int slotNum, boolean selected )
    {
        if( !world.isClient )
        {
            // Server side
            Inventory inventory = (entity instanceof PlayerEntity) ? ((PlayerEntity) entity).inventory : null;
            PocketServerComputer computer = createServerComputer( world, inventory, entity, stack );
            if( computer != null )
            {
                IPocketUpgrade upgrade = getUpgrade( stack );

                // Ping computer
                computer.keepAlive();
                computer.setWorld( world );
                computer.updateValues( entity, stack, upgrade );

                // Sync ID
                int id = computer.getId();
                if( id != getComputerId( stack ) )
                {
                    setComputerID( stack, id );
                    if( inventory != null )
                    {
                        inventory.markDirty();
                    }
                }

                // Sync label
                String label = computer.getLabel();
                if( !Objects.equal( label, getLabel( stack ) ) )
                {
                    setLabel( stack, label );
                    if( inventory != null )
                    {
                        inventory.markDirty();
                    }
                }

                // Update pocket upgrade
                if( upgrade != null )
                {
                    upgrade.update( computer, computer.getPeripheral( 2 ) );
                }
            }
        }
    }

    @Nonnull
    @Override
    public TypedActionResult<ItemStack> use( World world, PlayerEntity player, @Nonnull Hand hand )
    {
        ItemStack stack = player.getStackInHand( hand );
        if( !world.isClient )
        {
            PocketServerComputer computer = createServerComputer( world, player.inventory, player, stack );

            boolean stop = false;
            if( computer != null )
            {
                computer.turnOn();

                IPocketUpgrade upgrade = getUpgrade( stack );
                if( upgrade != null )
                {
                    computer.updateValues( player, stack, upgrade );
                    stop = upgrade.onRightClick( world, computer, computer.getPeripheral( 2 ) );
                }
            }

            if( !stop ) ComputerCraft.openPocketComputerGUI( player, hand );
        }
        return new TypedActionResult<>( ActionResult.SUCCESS, stack );
    }

    @Nonnull
    @Override
    public TextComponent getTranslatedNameTrimmed( @Nonnull ItemStack stack )
    {
        String baseString = getTranslationKey( stack );
        IPocketUpgrade upgrade = getUpgrade( stack );
        if( upgrade != null )
        {
            return new TranslatableTextComponent( baseString + ".upgraded",
                new TranslatableTextComponent( upgrade.getUnlocalisedAdjective() )
            );
        }
        else
        {
            return super.getTranslatedNameTrimmed( stack );
        }
    }

    @Override
    public void buildTooltip( ItemStack stack, @Nullable World world, List<TextComponent> list, TooltipOptions options )
    {
        if( options.isAdvanced() )
        {
            int id = getComputerId( stack );
            if( id >= 0 )
            {
                list.add( new TranslatableTextComponent( "gui.computercraft.tooltip.computer_id", id )
                    .applyFormat( TextFormat.GRAY ) );
            }
        }
    }

    public PocketServerComputer createServerComputer( final World world, Inventory inventory, Entity entity, @Nonnull ItemStack stack )
    {
        if( world.isClient )
        {
            return null;
        }

        PocketServerComputer computer;
        int instanceID = getInstanceID( stack );
        int sessionID = getSessionID( stack );
        int correctSessionID = ComputerCraft.serverComputerRegistry.getSessionID();

        if( instanceID >= 0 && sessionID == correctSessionID &&
            ComputerCraft.serverComputerRegistry.contains( instanceID ) )
        {
            computer = (PocketServerComputer) ComputerCraft.serverComputerRegistry.get( instanceID );
        }
        else
        {
            if( instanceID < 0 || sessionID != correctSessionID )
            {
                instanceID = ComputerCraft.serverComputerRegistry.getUnusedInstanceID();
                setInstanceID( stack, instanceID );
                setSessionID( stack, correctSessionID );
            }
            int computerID = getComputerId( stack );
            if( computerID < 0 )
            {
                computerID = ComputerCraftAPI.createUniqueNumberedSaveDir( world, "computer" );
                setComputerID( stack, computerID );
            }
            computer = new PocketServerComputer(
                world,
                computerID,
                getLabel( stack ),
                instanceID,
                getFamily()
            );
            computer.updateValues( entity, stack, getUpgrade( stack ) );
            computer.addAPI( new PocketAPI( computer ) );
            ComputerCraft.serverComputerRegistry.add( instanceID, computer );
            if( inventory != null )
            {
                inventory.markDirty();
            }
        }
        computer.setWorld( world );
        return computer;
    }

    public static ServerComputer getServerComputer( @Nonnull ItemStack stack )
    {
        int instanceID = getInstanceID( stack );
        if( instanceID >= 0 )
        {
            return ComputerCraft.serverComputerRegistry.get( instanceID );
        }
        return null;
    }

    public static ClientComputer createClientComputer( @Nonnull ItemStack stack )
    {
        int instanceID = getInstanceID( stack );
        if( instanceID >= 0 )
        {
            if( !ComputerCraft.clientComputerRegistry.contains( instanceID ) )
            {
                ComputerCraft.clientComputerRegistry.add( instanceID, new ClientComputer( instanceID ) );
            }
            return ComputerCraft.clientComputerRegistry.get( instanceID );
        }
        return null;
    }

    private static ClientComputer getClientComputer( @Nonnull ItemStack stack )
    {
        int instanceID = getInstanceID( stack );
        if( instanceID >= 0 )
        {
            return ComputerCraft.clientComputerRegistry.get( instanceID );
        }
        return null;
    }

    // IComputerItem implementation

    @Override
    public int getComputerId( @Nonnull ItemStack stack )
    {
        CompoundTag compound = stack.getTag();
        return compound != null && compound.containsKey( TAG_ID ) ? compound.getInt( TAG_ID ) : -1;
    }

    private void setComputerID( @Nonnull ItemStack stack, int computerID )
    {
        stack.getOrCreateTag().putInt( TAG_ID, computerID );
    }

    @Override
    public String getLabel( @Nonnull ItemStack stack )
    {
        return stack.hasDisplayName() ? stack.getDisplayName().getString() : null;
    }

    @Override
    public ComputerFamily getFamily()
    {
        return family;
    }

    @Override
    public ItemStack withFamily( @Nonnull ItemStack stack, @Nonnull ComputerFamily family )
    {
        return PocketComputerItemFactory.create(
            getComputerId( stack ), getLabel( stack ), getColour( stack ),
            family, getUpgrade( stack )
        );
    }

    // IMedia

    @Override
    public boolean setLabel( @Nonnull ItemStack stack, String label )
    {
        if( label != null )
        {
            stack.setDisplayName( new StringTextComponent( label ) );
        }
        else
        {
            stack.removeDisplayName();
        }
        return true;
    }

    @Override
    public IMount createDataMount( @Nonnull ItemStack stack, @Nonnull World world )
    {
        int id = getComputerId( stack );
        if( id >= 0 )
        {
            return ComputerCraftAPI.createSaveDirMount( world, "computer/" + id, ComputerCraft.computerSpaceLimit );
        }
        return null;
    }

    private static int getInstanceID( @Nonnull ItemStack stack )
    {
        CompoundTag compound = stack.getTag();
        return compound != null && compound.containsKey( TAG_INSTANCE ) ? compound.getInt( TAG_INSTANCE ) : -1;
    }

    private static void setInstanceID( @Nonnull ItemStack stack, int instanceID )
    {
        stack.getOrCreateTag().putInt( TAG_INSTANCE, instanceID );
    }

    private static int getSessionID( @Nonnull ItemStack stack )
    {
        CompoundTag compound = stack.getTag();
        return compound != null && compound.containsKey( TAG_SESSION ) ? compound.getInt( TAG_SESSION ) : -1;
    }

    private static void setSessionID( @Nonnull ItemStack stack, int sessionID )
    {
        stack.getOrCreateTag().putInt( TAG_SESSION, sessionID );
    }

    @Environment( EnvType.CLIENT )
    public static ComputerState getState( @Nonnull ItemStack stack )
    {
        ClientComputer computer = getClientComputer( stack );
        if( computer != null && computer.isOn() )
        {
            return computer.isCursorDisplayed() ? ComputerState.BLINKING : ComputerState.ON;
        }
        return ComputerState.OFF;
    }

    @Environment( EnvType.CLIENT )
    public static int getLightState( @Nonnull ItemStack stack )
    {
        ClientComputer computer = getClientComputer( stack );
        if( computer != null && computer.isOn() )
        {
            CompoundTag computerNBT = computer.getUserData();
            if( computerNBT != null && computerNBT.containsKey( TAG_LIGHT ) )
            {
                return computerNBT.getInt( TAG_LIGHT );
            }
        }
        return -1;
    }

    public static IPocketUpgrade getUpgrade( @Nonnull ItemStack stack )
    {
        CompoundTag compound = stack.getTag();
        return compound != null && compound.containsKey( TAG_UPGRADE )
            ? PocketUpgrades.get( compound.getString( TAG_UPGRADE ) ) : null;

    }

    public static void setUpgrade( @Nonnull ItemStack stack, IPocketUpgrade upgrade )
    {
        CompoundTag compound = stack.getOrCreateTag();

        if( upgrade == null )
        {
            compound.remove( TAG_UPGRADE );
        }
        else
        {
            compound.putString( TAG_UPGRADE, upgrade.getUpgradeID().toString() );
        }

        compound.remove( TAG_UPGRADE_INFO );
    }

    public static CompoundTag getUpgradeInfo( @Nonnull ItemStack stack )
    {
        return stack.getOrCreateSubCompoundTag( TAG_UPGRADE_INFO );
    }

    @Override
    public int getColour( ItemStack stack )
    {
        return getColourDirect( stack );
    }

    public static int getColourDirect( ItemStack stack )
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.containsKey( TAG_COLOUR ) ? tag.getInt( TAG_COLOUR ) : -1;
    }

    @Override
    public ItemStack withColour( ItemStack stack, int colour )
    {
        ItemStack copy = stack.copy();
        setColourDirect( copy, colour );
        return copy;
    }

    public static void setColourDirect( ItemStack stack, int colour )
    {
        if( colour == -1 )
        {
            CompoundTag tag = stack.getTag();
            if( tag != null ) tag.remove( TAG_COLOUR );
        }
        else
        {
            stack.getOrCreateTag().putInt( TAG_COLOUR, colour );
        }
    }

    private static final Identifier STATE = new Identifier( ComputerCraft.MOD_ID, "computer_state" );
    private static final ItemPropertyGetter STATE_GETTER = ( stack, world, player ) -> {
        if( !(stack.getItem() instanceof ItemPocketComputer) ) return 0;

        ClientComputer computer = getClientComputer( stack );
        return computer != null && computer.isOn() ? (computer.isCursorDisplayed() ? 2 : 1) : 0;
    };
}
