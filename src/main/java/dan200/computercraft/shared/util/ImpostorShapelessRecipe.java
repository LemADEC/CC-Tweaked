/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ImpostorShapelessRecipe extends ShapelessRecipe
{
    public ImpostorShapelessRecipe( @Nonnull Identifier id, @Nonnull String group, @Nonnull ItemStack result, DefaultedList<Ingredient> ingredients )
    {
        super( id, group, result, ingredients );
    }

    public ImpostorShapelessRecipe( @Nonnull Identifier id, @Nonnull String group, @Nonnull ItemStack result, ItemStack[] ingredients )
    {
        super( id, group, result, convert( ingredients ) );
    }

    private static DefaultedList<Ingredient> convert( ItemStack[] items )
    {
        DefaultedList<Ingredient> ingredients = DefaultedList.create( items.length, Ingredient.EMPTY );
        for( int i = 0; i < items.length; i++ ) ingredients.set( i, Ingredient.ofStacks( items[i] ) );
        return ingredients;
    }

    @Override
    public boolean matches( Inventory inv, World world )
    {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack craft( Inventory inventory )
    {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return super.getSerializer(); // TODO: Implement me!
    }
}
