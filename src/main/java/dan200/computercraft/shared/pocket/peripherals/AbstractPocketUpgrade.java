/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.peripherals;

import dan200.computercraft.api.pocket.IPocketUpgrade;
import net.minecraft.item.ItemProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public abstract class AbstractPocketUpgrade implements IPocketUpgrade
{
    private final Identifier identifier;
    private final String adjective;
    private final ItemStack stack;

    protected AbstractPocketUpgrade( Identifier identifier, String adjective, ItemStack stack )
    {
        this.identifier = identifier;
        this.adjective = adjective;
        this.stack = stack;
    }

    protected AbstractPocketUpgrade( Identifier identifier, String adjective, ItemProvider item )
    {
        this( identifier, adjective, new ItemStack( item ) );
    }

    @Nonnull
    @Override
    public final Identifier getUpgradeID()
    {
        return identifier;
    }

    @Nonnull
    @Override
    public final String getUnlocalisedAdjective()
    {
        return adjective;
    }

    @Nonnull
    @Override
    public final ItemStack getCraftingItem()
    {
        return stack;
    }
}
