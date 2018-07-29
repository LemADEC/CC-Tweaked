/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class ItemPeripheral extends ItemPeripheralBase
{
    public ItemPeripheral( Block block )
    {
        super( block );
        setTranslationKey( "computercraft:peripheral" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    @Nonnull
    public ItemStack create( PeripheralType type, String label, int quantity )
    {
        ItemStack stack;
        switch( type )
        {
            case Printer:
            {
                stack = new ItemStack( this, quantity, 3 );
                break;
            }

            default:
            {
                // Ignore types we can't handle
                return ItemStack.EMPTY;
            }
        }
        if( label != null )
        {
            stack.setStackDisplayName( label );
        }
        return stack;
    }

    @Override
    public void getSubItems( @Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> list )
    {
        if( !isInCreativeTab( tabs ) ) return;
        list.add( PeripheralItemFactory.create( PeripheralType.Printer, null, 1 ) );
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        switch( damage )
        {
            default:
            case 3:
            {
                return PeripheralType.Printer;
            }
        }
    }
}
