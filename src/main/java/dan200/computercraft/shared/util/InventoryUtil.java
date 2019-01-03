/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import dan200.computercraft.ComputerCraft;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.loot.context.LootContext;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public class InventoryUtil
{
    private static final Identifier CONTENTS_DROP = new Identifier( ComputerCraft.MOD_ID, "contents" );

    // Methods for comparing things:

    public static boolean areItemsEqual( @Nonnull ItemStack a, @Nonnull ItemStack b )
    {
        return a == b || ItemStack.areEqual( a, b );
    }

    public static boolean areItemsStackable( @Nonnull ItemStack a, @Nonnull ItemStack b )
    {
        return a == b || (a.getItem() == b.getItem() && ItemStack.areTagsEqual( a, b ));
    }

    @Nonnull
    public static ItemStack copyItem( @Nonnull ItemStack a )
    {
        return a.copy();
    }

    // Methods for finding inventories:

    public static Inventory getInventory( World world, BlockPos pos, Direction side )
    {
        // Look for tile with inventory
        int y = pos.getY();
        if( y >= 0 && y < world.getHeight() )
        {
            BlockEntity tileEntity = world.getBlockEntity( pos );
            if( tileEntity != null )
            {
                //                IItemHandler itemHandler = tileEntity.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side );
                //                if( itemHandler != null )
                //                {
                //                    return itemHandler;
                //                }
                //                else if( side != null && tileEntity instanceof ISidedInventory )
                //                {
                //                    return new SidedInvWrapper( (ISidedInventory) tileEntity, side );
                //                }
                //                else if( tileEntity instanceof Inventory )
                //                {
                return (Inventory) tileEntity;
                //                }
            }
        }

        // Look for entity with inventory
        Vec3d vecStart = new Vec3d(
            pos.getX() + 0.5 + 0.6 * side.getOffsetX(),
            pos.getY() + 0.5 + 0.6 * side.getOffsetY(),
            pos.getZ() + 0.5 + 0.6 * side.getOffsetZ()
        );
        Direction dir = side.getOpposite();
        Vec3d vecDir = new Vec3d(
            dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ()
        );
        Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities( world, vecStart, vecDir, 1.1 );
        if( hit != null )
        {
            Entity entity = hit.getKey();
            if( entity instanceof Inventory )
            {
                return (Inventory) entity;
            }
        }
        return null;
    }

    public static ItemStorage getStorage( World world, BlockPos pos, Direction side )
    {
        Inventory inventory = getInventory( world, pos, side );
        return inventory == null ? null : ItemStorage.wrap( inventory, side );
    }

    // Methods for placing into inventories:

    @Nonnull
    public static ItemStack storeItems( @Nonnull ItemStack itemstack, ItemStorage inventory, int begin )
    {
        return storeItems( itemstack, inventory, 0, inventory.size(), begin );
    }

    @Nonnull
    public static ItemStack storeItems( @Nonnull ItemStack itemstack, ItemStorage inventory )
    {
        return storeItems( itemstack, inventory, 0, inventory.size(), 0 );
    }

    @Nonnull
    public static ItemStack storeItems( @Nonnull ItemStack stack, ItemStorage inventory, int start, int range, int begin )
    {
        if( stack.isEmpty() ) return ItemStack.EMPTY;

        // Inspect the slots in order and try to find empty or stackable slots
        ItemStack remainder = stack.copy();
        for( int i = 0; i < range; i++ )
        {
            int slot = start + ((i + (begin - start)) % range);
            if( remainder.isEmpty() ) break;
            remainder = inventory.store( slot, remainder, false );
        }
        return areItemsEqual( stack, remainder ) ? stack : remainder;
    }

    // Methods for taking out of inventories

    @Nonnull
    public static ItemStack takeItems( int count, ItemStorage inventory, int begin )
    {
        return takeItems( count, inventory, 0, inventory.size(), begin );
    }

    @Nonnull
    public static ItemStack takeItems( int count, ItemStorage inventory )
    {
        return takeItems( count, inventory, 0, inventory.size(), 0 );
    }

    @Nonnull
    public static ItemStack takeItems( int count, ItemStorage inventory, int start, int range, int begin )
    {
        // Combine multiple stacks from inventory into one if necessary
        ItemStack partialStack = ItemStack.EMPTY;
        for( int i = 0; i < range; i++ )
        {
            int slot = start + ((i + (begin - start)) % range);

            if( count <= 0 ) break;

            ItemStack extracted = inventory.take( slot, count, partialStack, false );
            if( !extracted.isEmpty() )
            {
                count -= extracted.getAmount();
                if( partialStack.isEmpty() )
                {
                    // If we've extracted for this first time, then limit the count to the maximum stack size.
                    partialStack = extracted;
                    count = Math.min( count, extracted.getMaxAmount() );
                }
                else
                {
                    partialStack.addAmount( extracted.getAmount() );
                }
            }
        }

        return partialStack;
    }

    public static void dropContents( Inventory inventory, LootContext.Builder builder )
    {
        builder.putDrop( CONTENTS_DROP, ( ctx, consumer ) -> {
            for( int i = 0; i < inventory.getInvSize(); i++ ) consumer.accept( inventory.getInvStack( i ) );
        } );
    }
}
