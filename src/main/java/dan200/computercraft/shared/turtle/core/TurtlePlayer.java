/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.event.FakePlayer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.ItemStorage;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.command.arguments.EntityAnchorArgumentType;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Villager;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TurtlePlayer extends FakePlayer
{
    public final static GameProfile DEFAULT_PROFILE = new GameProfile(
        UUID.fromString( "0d0c4ca0-4ff1-11e4-916c-0800200c9a66" ),
        "[ComputerCraft]"
    );

    private TurtlePlayer( ITurtleAccess turtle )
    {
        super( (ServerWorld) turtle.getWorld(), getProfile( turtle.getOwningPlayer() ), new ServerPlayerInteractionManager( turtle.getWorld() ) );
        setState( turtle );
    }

    private static GameProfile getProfile( @Nullable GameProfile profile )
    {
        return profile != null && profile.isComplete() ? profile : DEFAULT_PROFILE;
    }

    private void setState( ITurtleAccess turtle )
    {
        BlockPos position = turtle.getPosition();
        x = position.getX() + 0.5;
        y = position.getY() + 0.5;
        z = position.getZ() + 0.5;

        yaw = turtle.getDirection().asRotation();
        pitch = 0.0f;

        inventory.clearInv();
    }

    public static TurtlePlayer get( ITurtleAccess access )
    {
        if( !(access instanceof TurtleBrain) ) return new TurtlePlayer( access );

        TurtleBrain brain = (TurtleBrain) access;
        TurtlePlayer player = brain.m_cachedPlayer;
        if( player == null || player.getGameProfile() != getProfile( access.getOwningPlayer() )
            || player.getEntityWorld() != access.getWorld() )
        {
            player = brain.m_cachedPlayer = new TurtlePlayer( brain );
        }
        else
        {
            player.setState( access );
        }

        return player;
    }

    public void loadInventory( @Nonnull ItemStack currentStack )
    {
        // Load up the fake inventory
        inventory.selectedSlot = 0;
        inventory.setInvStack( 0, currentStack );
    }

    public ItemStack unloadInventory( ITurtleAccess turtle )
    {
        // Get the item we placed with
        ItemStack results = inventory.getInvStack( 0 );
        inventory.setInvStack( 0, ItemStack.EMPTY );

        // Store (or drop) anything else we found
        BlockPos dropPosition = turtle.getPosition();
        Direction dropDirection = turtle.getDirection().getOpposite();
        ItemStorage storage = ItemStorage.wrap( turtle.getInventory() );
        for( int i = 0; i < inventory.getInvSize(); ++i )
        {
            ItemStack stack = inventory.getInvStack( i );
            if( !stack.isEmpty() )
            {
                ItemStack remainder = InventoryUtil.storeItems( stack, storage, turtle.getSelectedSlot() );
                if( !remainder.isEmpty() )
                {
                    WorldUtil.dropItemStack( remainder, turtle.getWorld(), dropPosition, dropDirection );
                }
                inventory.setInvStack( i, ItemStack.EMPTY );
            }
        }
        inventory.markDirty();
        return results;
    }

    @Override
    public void method_6000()
    {
    }

    @Override
    public void method_6044()
    {
    }

    @Override
    public @Nullable Entity changeDimension( DimensionType dimensionType_1 )
    {
        return null;
    }

    @Nonnull
    @Override
    public SleepResult trySleep( @Nonnull BlockPos bedLocation )
    {
        return SleepResult.INVALID_ATTEMPT;
    }

    @Override
    public boolean startRiding( Entity entity_1, boolean boolean_1 )
    {
        return false;
    }

    @Override
    public void stopRiding()
    {
    }

    @Override
    public void openSignEditor( SignBlockEntity block )
    {
    }

    @Override
    public void openContainer( ContainerProvider container )
    {
    }

    @Override
    public void openInventory( Inventory inventory )
    {
    }

    @Override
    public void openVillagerGui( Villager villager )
    {
    }

    @Override
    public void openHorseInventory( HorseBaseEntity entity, Inventory inventory )
    {
    }

    @Override
    public void openBookEditor( ItemStack itemStack, Hand hand )
    {
    }

    @Override
    public void openCommandBlock( CommandBlockBlockEntity entity )
    {
    }

    @Override
    public void onContainerRegistered( Container container_1, DefaultedList<ItemStack> defaultedList_1 )
    {
    }

    @Override
    public void onContainerPropertyUpdate( Container container_1, int int_1, int int_2 )
    {
    }

    @Override
    public void onContainerInvRegistered( Container container_1, Inventory inventory_1 )
    {
    }

    @Override
    public void closeGui()
    {
    }

    @Override
    public void onContainerSlotUpdate( Container container_1, int int_1, ItemStack itemStack_1 )
    {
    }

    @Override
    public void method_14241()
    {
    }

    @Override
    public void addChatMessage( TextComponent textComponent_1, boolean boolean_1 )
    {
    }

    @Override
    protected void method_6040()
    {
    }

    @Override
    public void lookAt( EntityAnchorArgumentType.EntityAnchor entityAnchorArgumentType$EntityAnchor_1, Vec3d vec3d_1 )
    {
    }

    // TODO: Finish this off.
}
