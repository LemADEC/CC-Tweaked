/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.common.IColouredItem;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemDisk extends Item implements IMedia, IColouredItem
{
    private static final String TAG_COLOUR = "color";
    private static final String TAG_ID = "disk_id";

    public ItemDisk( Settings settings )
    {
        super( settings );
    }

    @Nonnull
    public static ItemStack createFromIDAndColour( int id, String label, int colour )
    {
        ItemStack stack = new ItemStack( ComputerCraft.Items.disk );
        setDiskID( stack, id );
        ComputerCraft.Items.disk.setLabel( stack, label );
        setColourDirect( stack, colour );
        return stack;
    }

    @Override
    public void addStacksForDisplay( @Nonnull ItemGroup tabs, @Nonnull DefaultedList<ItemStack> list )
    {
        if( !isInItemGroup( tabs ) ) return;
        for( int colour = 0; colour < 16; colour++ )
        {
            list.add( createFromIDAndColour( -1, null, Colour.VALUES[colour].getHex() ) );
        }
    }

    @Override
    public void buildTooltip( ItemStack stack, @Nullable World world, List<TextComponent> list, TooltipOptions options )
    {
        if( options.isAdvanced() )
        {
            int id = getDiskID( stack );
            if( id >= 0 )
            {
                list.add( new TranslatableTextComponent( "gui.computercraft.tooltip.disk_id", id )
                    .applyFormat( TextFormat.GRAY ) );
            }
        }
    }

    @Override
    public String getLabel( @Nonnull ItemStack stack )
    {
        return stack.hasDisplayName() ? stack.getDisplayName().getString() : null;
    }

    @Override
    public boolean setLabel( @Nonnull ItemStack stack, String label )
    {
        if( label != null )
        {
            stack.setDisplayName( new StringTextComponent( label ) );
        }
        else
        {
            stack.removeDisplayName();
        }
        return true;
    }

    @Override
    public IMount createDataMount( @Nonnull ItemStack stack, @Nonnull World world )
    {
        int diskID = getDiskID( stack );
        if( diskID < 0 )
        {
            diskID = ComputerCraftAPI.createUniqueNumberedSaveDir( world, "computer/disk" );
            setDiskID( stack, diskID );
        }
        return ComputerCraftAPI.createSaveDirMount( world, "computer/disk/" + diskID, ComputerCraft.floppySpaceLimit );
    }

    public static int getDiskID( @Nonnull ItemStack stack )
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.containsKey( TAG_ID ) ? nbt.getInt( TAG_ID ) : -1;
    }

    private static void setDiskID( @Nonnull ItemStack stack, int id )
    {
        if( id >= 0 ) stack.getOrCreateTag().putInt( TAG_ID, id );
    }

    @Override
    public int getColour( @Nonnull ItemStack stack )
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.containsKey( TAG_COLOUR ) ? nbt.getInt( TAG_COLOUR ) : Colour.White.getHex();
    }

    @Override
    public ItemStack withColour( ItemStack stack, int colour )
    {
        ItemStack copy = stack.copy();
        setColourDirect( copy, colour );
        return copy;
    }

    private static void setColourDirect( ItemStack stack, int colour )
    {
        if( colour == -1 )
        {
            CompoundTag tag = stack.getTag();
            if( tag != null ) tag.remove( TAG_COLOUR );
        }
        else
        {
            stack.getOrCreateTag().putInt( TAG_COLOUR, colour );
        }
    }
}
