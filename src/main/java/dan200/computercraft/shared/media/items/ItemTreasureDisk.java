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
import dan200.computercraft.core.filesystem.SubMount;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class ItemTreasureDisk extends Item implements IMedia
{
    private static final String TAG_TITLE = "title";
    private static final String TAG_COLOUR = "colour";
    private static final String TAG_SUB_PATH = "sub_path";

    public ItemTreasureDisk( Settings settings )
    {
        super( settings );
    }

    /*
    @Override
    public boolean doesSneakBypassUse( @Nonnull ItemStack stack, BlockView world, BlockPos pos, PlayerEntity player )
    {
        return true;
    }
    */

    @Override
    public void buildTooltip( ItemStack stack, @Nullable World world, List<TextComponent> list, TooltipOptions tooltipOptions )
    {
        String label = getTitle( stack );
        if( label != null && label.length() > 0 ) list.add( new StringTextComponent( label ) );
    }

    @Override
    public String getLabel( @Nonnull ItemStack stack )
    {
        return getTitle( stack );
    }

    @Override
    public IMount createDataMount( @Nonnull ItemStack stack, @Nonnull World world )
    {
        IMount rootTreasure = getTreasureMount();
        String subPath = getSubPath( stack );
        try
        {
            if( rootTreasure.exists( subPath ) )
            {
                return new SubMount( rootTreasure, subPath );
            }
            else if( rootTreasure.exists( "deprecated/" + subPath ) )
            {
                return new SubMount( rootTreasure, "deprecated/" + subPath );
            }
            else
            {
                return null;
            }
        }
        catch( IOException e )
        {
            return null;
        }
    }

    public static ItemStack create( String subPath, int colourIndex )
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString( TAG_SUB_PATH, subPath );

        int slash = subPath.indexOf( "/" );
        if( slash >= 0 )
        {
            String author = subPath.substring( 0, slash );
            String title = subPath.substring( slash + 1 );
            nbt.putString( TAG_TITLE, "\"" + title + "\" by " + author );
        }
        else
        {
            nbt.putString( TAG_TITLE, "untitled" );
        }
        nbt.putInt( TAG_COLOUR, Colour.values()[colourIndex].getHex() );

        ItemStack result = new ItemStack( ComputerCraft.Items.treasureDisk );
        result.setTag( nbt );
        return result;
    }

    private static IMount getTreasureMount()
    {
        return ComputerCraftAPI.createResourceMount( "computercraft", "lua/treasure" );
    }

    private String getTitle( @Nonnull ItemStack stack )
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.containsKey( TAG_TITLE ) ? nbt.getString( TAG_TITLE ) : "'alongtimeago' by dan200";
    }

    private String getSubPath( @Nonnull ItemStack stack )
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.containsKey( TAG_SUB_PATH ) ? nbt.getString( TAG_SUB_PATH ) : "dan200/alongtimeago";
    }

    public static int getColour( @Nonnull ItemStack stack )
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.containsKey( TAG_COLOUR ) ? nbt.getInt( TAG_COLOUR ) : Colour.Blue.getHex();
    }
}
