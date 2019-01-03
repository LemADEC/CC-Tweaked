/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.recipes;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeSerializers;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PrintoutRecipe implements Recipe
{
    private final Ingredient paper = Ingredient.ofItems( Items.PAPER );
    private final Ingredient leather = Ingredient.ofItems( Items.LEATHER );
    private final Ingredient string = Ingredient.ofItems( Items.STRING );

    private final Identifier id;

    public PrintoutRecipe( Identifier id )
    {
        this.id = id;
    }

    @Override
    public boolean fits( int x, int y )
    {
        return x >= 3 && y >= 3;
    }

    @Nonnull
    @Override
    public ItemStack getOutput()
    {
        return ItemPrintout.createMultipleFromTitleAndText( null, null, null );
    }

    @Override
    public boolean matches( @Nonnull Inventory inventory, @Nonnull World world )
    {
        return !craft( inventory ).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack craft( @Nonnull Inventory inventory )
    {
        // See if we match the recipe, and extract the input disk ID and dye colour
        int numPages = 0;
        int numPrintouts = 0;
        ItemStack[] printouts = null;
        boolean stringFound = false;
        boolean leatherFound = false;
        boolean printoutFound = false;
        for( int y = 0; y < inventory.getInvWidth(); y++ )
        {
            for( int x = 0; x < inventory.getInvHeight(); x++ )
            {
                ItemStack stack = inventory.getInvStack( x + y * inventory.getInvWidth() );
                if( !stack.isEmpty() )
                {
                    if( stack.getItem() instanceof ItemPrintout && ((ItemPrintout) stack.getItem()).getType() != ItemPrintout.Type.BOOK )
                    {
                        if( printouts == null )
                        {
                            printouts = new ItemStack[9];
                        }
                        printouts[numPrintouts] = stack;
                        numPages = numPages + ItemPrintout.getPageCount( stack );
                        numPrintouts++;
                        printoutFound = true;
                    }
                    else if( paper.matches( stack ) )
                    {
                        if( printouts == null )
                        {
                            printouts = new ItemStack[9];
                        }
                        printouts[numPrintouts] = stack;
                        numPages++;
                        numPrintouts++;
                    }
                    else if( string.matches( stack ) && !stringFound )
                    {
                        stringFound = true;
                    }
                    else if( leather.matches( stack ) && !leatherFound )
                    {
                        leatherFound = true;
                    }
                    else
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // Build some pages with what was passed in
        if( numPages <= ItemPrintout.MAX_PAGES && stringFound && printoutFound && numPrintouts >= (leatherFound ? 1 : 2) )
        {
            String[] text = new String[numPages * ItemPrintout.LINES_PER_PAGE];
            String[] colours = new String[numPages * ItemPrintout.LINES_PER_PAGE];
            int line = 0;

            for( int printout = 0; printout < numPrintouts; printout++ )
            {
                ItemStack stack = printouts[printout];
                if( stack.getItem() instanceof ItemPrintout )
                {
                    // Add a printout
                    String[] pageText = ItemPrintout.getText( printouts[printout] );
                    String[] pageColours = ItemPrintout.getColours( printouts[printout] );
                    for( int pageLine = 0; pageLine < pageText.length; pageLine++ )
                    {
                        text[line] = pageText[pageLine];
                        colours[line] = pageColours[pageLine];
                        line++;
                    }
                }
                else
                {
                    // Add a blank page
                    for( int pageLine = 0; pageLine < ItemPrintout.LINES_PER_PAGE; pageLine++ )
                    {
                        text[line] = "";
                        colours[line] = "";
                        line++;
                    }
                }
            }

            String title = null;
            if( printouts[0].getItem() instanceof ItemPrintout )
            {
                title = ItemPrintout.getTitle( printouts[0] );
            }

            if( leatherFound )
            {
                return ItemPrintout.createBookFromTitleAndText( title, text, colours );
            }
            else
            {
                return ItemPrintout.createMultipleFromTitleAndText( title, text, colours );
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isIgnoredInRecipeBook()
    {
        return true;
    }

    @Override
    public Identifier getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    public static final RecipeSerializer<?> SERIALIZER = new RecipeSerializers.Dummy<>(
        ComputerCraft.MOD_ID + ":printout", PrintoutRecipe::new
    );
}
