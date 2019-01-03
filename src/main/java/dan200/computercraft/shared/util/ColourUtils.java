/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DyeColor;

import javax.annotation.Nonnull;

import static dan200.computercraft.shared.util.NBTUtil.TAG_ANY_NUMERIC;

public final class ColourUtils
{
    public static DyeColor getStackColour( ItemStack stack )
    {
        Item item = stack.getItem();
        if( item instanceof DyeItem ) return ((DyeItem) item).getColor();

        return null;
    }

    public static int getHexColour( @Nonnull CompoundTag tag )
    {
        if( tag.containsKey( "colourIndex", TAG_ANY_NUMERIC ) )
        {
            return Colour.VALUES[tag.getInt( "colourIndex" ) & 0xF].getHex();
        }
        else if( tag.containsKey( "colour", TAG_ANY_NUMERIC ) )
        {
            return tag.getInt( "colour" );
        }
        else if( tag.containsKey( "color", TAG_ANY_NUMERIC ) )
        {
            return tag.getInt( "color" );
        }
        else
        {
            return -1;
        }
    }

    public static Colour getColour( @Nonnull CompoundTag tag )
    {
        if( tag.containsKey( "colourIndex", TAG_ANY_NUMERIC ) )
        {
            return Colour.fromInt( tag.getInt( "colourIndex" ) & 0xF );
        }
        else
        {
            return null;
        }
    }
}
