/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.recipes;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.media.items.ItemDisk;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.ColourTracker;
import dan200.computercraft.shared.util.ColourUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeSerializers;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DiskRecipe extends AbstractRecipe
{
    private final Ingredient paper = Ingredient.ofItems( Items.PAPER );
    private final Ingredient redstone = Ingredient.ofItems( Items.REDSTONE );

    public DiskRecipe( Identifier id )
    {
        super( id );
    }

    @Override
    public boolean matches( @Nonnull Inventory inv, @Nonnull World world )
    {
        boolean paperFound = false;
        boolean redstoneFound = false;

        for( int i = 0; i < inv.getInvSize(); i++ )
        {
            ItemStack stack = inv.getInvStack( i );

            if( !stack.isEmpty() )
            {
                if( paper.matches( stack ) )
                {
                    if( paperFound ) return false;
                    paperFound = true;
                }
                else if( redstone.matches( stack ) )
                {
                    if( redstoneFound ) return false;
                    redstoneFound = true;
                }
                else if( ColourUtils.getStackColour( stack ) != null )
                {
                    return false;
                }
            }
        }

        return redstoneFound && paperFound;
    }

    @Nonnull
    @Override
    public ItemStack craft( @Nonnull Inventory inv )
    {
        ColourTracker tracker = new ColourTracker();

        for( int i = 0; i < inv.getInvSize(); i++ )
        {
            ItemStack stack = inv.getInvStack( i );

            if( stack.isEmpty() ) continue;

            if( !paper.matches( stack ) && !redstone.matches( stack ) )
            {
                DyeColor dye = ColourUtils.getStackColour( stack );
                if( dye == null ) continue;

                Colour colour = Colour.VALUES[dye.getId()];
                tracker.addColour( colour.getR(), colour.getG(), colour.getB() );
            }
        }

        return ItemDisk.createFromIDAndColour( -1, null, tracker.hasColour() ? tracker.getColour() : Colour.Blue.getHex() );
    }

    @Override
    public boolean fits( int x, int y )
    {
        return x >= 2 && y >= 2;
    }

    @Nonnull
    @Override
    public ItemStack getOutput()
    {
        return ItemDisk.createFromIDAndColour( -1, null, Colour.Blue.getHex() );
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    public static final RecipeSerializer<DiskRecipe> SERIALIZER = new RecipeSerializers.Dummy<>(
        ComputerCraft.MOD_ID + ":disk", DiskRecipe::new
    );
}
