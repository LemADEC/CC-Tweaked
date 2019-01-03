/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface DefaultInventory extends Inventory
{
    @Override
    default ItemStack takeInvStack( int slot, int count )
    {
        ItemStack stack = getInvStack( slot ).split( count );
        if( !stack.isEmpty() ) markDirty( slot );
        return stack;
    }

    @Override
    default ItemStack removeInvStack( int slot )
    {
        ItemStack stack = getInvStack( slot );
        if( !stack.isEmpty() ) setInvStack( slot, ItemStack.EMPTY );
        return stack;
    }

    @Override
    default int getInvMaxStackAmount()
    {
        return 64;
    }

    @Override
    default void onInvOpen( PlayerEntity playerEntity )
    {
    }

    @Override
    default void onInvClose( PlayerEntity playerEntity )
    {
    }

    @Override
    default boolean isValidInvStack( int i, ItemStack itemStack )
    {
        return true;
    }

    @Override
    default int getInvProperty( int key )
    {
        return 0;
    }

    @Override
    default void setInvProperty( int key, int value )
    {
    }

    @Override
    default int getInvPropertyCount()
    {
        return 0;
    }

    default void markDirty( int slot )
    {
        markDirty();
    }
}
