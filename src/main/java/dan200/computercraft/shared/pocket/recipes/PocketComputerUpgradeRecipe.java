/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.recipes;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.PocketUpgrades;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeSerializers;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PocketComputerUpgradeRecipe extends AbstractRecipe
{
    private PocketComputerUpgradeRecipe( Identifier identifier )
    {
        super( identifier );
    }

    @Override
    public boolean fits( int x, int y )
    {
        return x >= 1 && y >= 2;
    }

    @Nonnull
    @Override
    public ItemStack getOutput()
    {
        return PocketComputerItemFactory.create( -1, null, -1, ComputerFamily.Normal, null );
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
        // Scan the grid for a pocket computer
        ItemStack computer = ItemStack.EMPTY;
        int computerX = -1;
        int computerY = -1;
        computer:
        for( int y = 0; y < inventory.getInvWidth(); y++ )
        {
            for( int x = 0; x < inventory.getInvHeight(); x++ )
            {
                ItemStack item = inventory.getInvStack( x + y * inventory.getInvWidth() );
                if( !item.isEmpty() && item.getItem() instanceof ItemPocketComputer )
                {
                    computer = item;
                    computerX = x;
                    computerY = y;
                    break computer;
                }
            }
        }

        if( computer.isEmpty() )
        {
            return ItemStack.EMPTY;
        }

        ItemPocketComputer itemComputer = (ItemPocketComputer) computer.getItem();
        if( ItemPocketComputer.getUpgrade( computer ) != null ) return ItemStack.EMPTY;

        // Check for upgrades around the item
        IPocketUpgrade upgrade = null;
        for( int y = 0; y < inventory.getInvHeight(); y++ )
        {
            for( int x = 0; x < inventory.getInvWidth(); x++ )
            {
                ItemStack item = inventory.getInvStack( x + y * inventory.getInvWidth() );
                if( x == computerX && y == computerY )
                {
                    continue;
                }
                else if( x == computerX && y == computerY - 1 )
                {
                    upgrade = PocketUpgrades.get( item );
                    if( upgrade == null ) return ItemStack.EMPTY;
                }
                else if( !item.isEmpty() )
                {
                    return ItemStack.EMPTY;
                }
            }
        }

        if( upgrade == null ) return ItemStack.EMPTY;

        // Construct the new stack
        ComputerFamily family = itemComputer.getFamily();
        int computerID = itemComputer.getComputerId( computer );
        String label = itemComputer.getLabel( computer );
        int colour = itemComputer.getColour( computer );
        return PocketComputerItemFactory.create( computerID, label, colour, family, upgrade );
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    public static final RecipeSerializer<PocketComputerUpgradeRecipe> SERIALIZER = new RecipeSerializers.Dummy<>(
        ComputerCraft.MOD_ID + ":pocket_computer_upgrade",
        PocketComputerUpgradeRecipe::new
    );
}
