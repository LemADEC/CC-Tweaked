/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.recipe;

import checkers.nullness.quals.NonNull;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Represents a recipe which converts a computer from one form into another.
 */
public abstract class ComputerConvertRecipe extends ShapedRecipe
{
    private final String group;

    public ComputerConvertRecipe( Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result )
    {
        super( identifier, group, width, height, ingredients, result );
        this.group = group;
    }

    @Nonnull
    protected abstract ItemStack convert( @NonNull IComputerItem item, @Nonnull ItemStack stack );

    @Override
    public boolean matches( @Nonnull Inventory inventory, @Nonnull World world )
    {
        if( !super.matches( inventory, world ) ) return false;

        for( int i = 0; i < inventory.getInvSize(); i++ )
        {
            if( inventory.getInvStack( i ).getItem() instanceof IComputerItem )
            {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    @Override
    public ItemStack craft( @Nonnull Inventory inventory )
    {
        for( int i = 0; i < inventory.getInvSize(); i++ )
        {
            ItemStack stack = inventory.getInvStack( i );
            if( stack.getItem() instanceof IComputerItem )
            {
                return convert( (IComputerItem) stack.getItem(), stack );
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public String getGroup()
    {
        return group;
    }
}
