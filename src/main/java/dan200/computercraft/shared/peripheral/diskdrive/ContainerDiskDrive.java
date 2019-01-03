/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.diskdrive;

import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerDiskDrive extends Container
{
    private final TileDiskDrive m_diskDrive;

    public ContainerDiskDrive( Inventory playerInventory, TileDiskDrive diskDrive )
    {
        m_diskDrive = diskDrive;
        addSlot( new Slot( m_diskDrive, 0, 8 + 4 * 18, 35 ) );

        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlot( new Slot( playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 ) );
            }
        }

        for( int x = 0; x < 9; x++ )
        {
            addSlot( new Slot( playerInventory, x, 8 + x * 18, 142 ) );
        }
    }

    public TileDiskDrive getDiskDrive()
    {
        return m_diskDrive;
    }

    @Override
    public boolean canUse( @Nonnull PlayerEntity player )
    {
        return m_diskDrive.canPlayerUseInv( player );
    }

    public ItemStack transferSlot( PlayerEntity payer, int slotIndex )
    {
        Slot slot = slotList.get( slotIndex );
        if( slot == null || !slot.hasStack() ) return ItemStack.EMPTY;

        ItemStack slotStack = slot.getStack();
        ItemStack result = slotStack.copy();
        if( slotIndex == 0 )
        {
            // Insert into player inventory
            if( !insertItem( slotStack, 1, 37, true ) ) return ItemStack.EMPTY;
        }
        else
        {
            // Insert into disk inventory
            if( !insertItem( slotStack, 0, 1, false ) ) return ItemStack.EMPTY;
        }

        // Update the slot
        if( slotStack.isEmpty() )
        {
            slot.setStack( ItemStack.EMPTY );
        }
        else
        {
            slot.markDirty();
        }

        if( slotStack.getAmount() == result.getAmount() ) return ItemStack.EMPTY;
        slot.onTakeItem( payer, slotStack );
        return result;
    }
}
