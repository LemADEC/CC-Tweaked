/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import javax.annotation.Nonnull;

public class ContainerHeldItem extends Container
{
    private final ItemStack stack;
    private final Hand hand;

    public ContainerHeldItem( PlayerEntity player, Hand hand )
    {
        this.hand = hand;
        stack = InventoryUtil.copyItem( player.getStackInHand( hand ) );
    }

    @Nonnull
    public ItemStack getStack()
    {
        return stack;
    }

    @Override
    public boolean canUse( @Nonnull PlayerEntity player )
    {
        if( player == null || !player.isValid() ) return false;

        ItemStack stack = player.getStackInHand( hand );
        return stack == this.stack || (!stack.isEmpty() && !this.stack.isEmpty() && stack.getItem() == this.stack.getItem());
    }
}
