/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.printer;

import net.minecraft.container.Container;
import net.minecraft.container.ContainerListener;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerPrinter extends Container
{
    private static final int PROPERTY_PRINTING = 0;

    private TilePrinter m_printer;
    private boolean m_lastPrinting;

    public ContainerPrinter( Inventory playerInventory, TilePrinter printer )
    {
        m_printer = printer;
        m_lastPrinting = false;

        // Ink slot
        addSlot( new Slot( printer, 0, 13, 35 ) );

        // In-tray
        for( int x = 0; x < 6; x++ ) addSlot( new Slot( printer, x + 1, 61 + x * 18, 22 ) );

        // Out-tray
        for( int x = 0; x < 6; x++ ) addSlot( new Slot( printer, x + 7, 61 + x * 18, 49 ) );

        // Player inv
        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlot( new Slot( playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 ) );
            }
        }

        // Player hotbar
        for( int x = 0; x < 9; x++ )
        {
            addSlot( new Slot( playerInventory, x, 8 + x * 18, 142 ) );
        }
    }

    public boolean isPrinting()
    {
        return m_lastPrinting;
    }

    public TilePrinter getPrinter()
    {
        return m_printer;
    }

    @Override
    public void addListener( ContainerListener listener )
    {
        super.addListener( listener );
        listener.onContainerPropertyUpdate( this, PROPERTY_PRINTING, m_printer.isPrinting() ? 1 : 0 );
    }

    @Override
    public void sendContentUpdates()
    {
        super.sendContentUpdates();

        if( !m_printer.getWorld().isClient )
        {
            // Push the printing state to the client if needed.
            boolean printing = m_printer.isPrinting();
            if( printing != m_lastPrinting )
            {
                for( ContainerListener listener : listeners )
                {
                    listener.onContainerPropertyUpdate( this, PROPERTY_PRINTING, printing ? 1 : 0 );
                }
                m_lastPrinting = printing;
            }
        }
    }

    @Override
    public void setProperty( int property, int value )
    {
        super.setProperty( property, value );
        if( m_printer.getWorld().isClient )
        {
            if( property == PROPERTY_PRINTING ) m_lastPrinting = value != 0;
        }
    }

    @Override
    public boolean canUse( @Nonnull PlayerEntity player )
    {
        return m_printer.canPlayerUseInv( player );
    }


    @Nonnull
    @Override
    public ItemStack transferSlot( PlayerEntity par1EntityPlayer, int slotIndex )
    {
        Slot slot = slotList.get( slotIndex );
        if( slot == null || !slot.hasStack() ) return ItemStack.EMPTY;

        ItemStack slotStack = slot.getStack();
        ItemStack result = slotStack.copy();
        if( slotIndex < 13 )
        {
            // Transfer from printer to inventory
            if( !insertItem( slotStack, 13, 49, true ) ) return ItemStack.EMPTY;
        }
        else
        {
            // Transfer from inventory to printer
            if( slotStack.getItem() instanceof DyeItem )
            {
                // Dyes go in the first slot
                if( !insertItem( slotStack, 0, 1, false ) ) return ItemStack.EMPTY;
            }
            else
            {
                // Paper goes into 1 to 12.
                if( !insertItem( slotStack, 1, 13, false ) ) return ItemStack.EMPTY;
            }
        }

        if( slotStack.isEmpty() )
        {
            slot.setStack( ItemStack.EMPTY );
        }
        else
        {
            slot.markDirty();
        }

        if( slotStack.getAmount() == result.getAmount() ) return ItemStack.EMPTY;

        slot.onTakeItem( par1EntityPlayer, slotStack );
        return result;
    }
}
