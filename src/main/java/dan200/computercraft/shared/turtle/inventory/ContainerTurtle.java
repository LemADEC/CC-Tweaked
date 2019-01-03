/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.inventory;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerListener;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerTurtle extends Container implements IContainerComputer
{
    private static final int PROGRESS_ID_SELECTED_SLOT = 0;

    public final int m_playerInvStartY;
    public final int m_turtleInvStartX;

    protected ITurtleAccess m_turtle;
    private IComputer m_computer;
    private int m_selectedSlot;

    protected ContainerTurtle( Inventory playerInventory, ITurtleAccess turtle, int playerInvStartY, int turtleInvStartX )
    {
        m_playerInvStartY = playerInvStartY;
        m_turtleInvStartX = turtleInvStartX;

        m_turtle = turtle;
        if( !m_turtle.getWorld().isClient )
        {
            m_selectedSlot = m_turtle.getSelectedSlot();
        }
        else
        {
            m_selectedSlot = 0;
        }

        // Turtle inventory
        for( int y = 0; y < 4; y++ )
        {
            for( int x = 0; x < 4; x++ )
            {
                addSlot( new Slot( m_turtle.getInventory(), x + y * 4, turtleInvStartX + 1 + x * 18, playerInvStartY + 1 + y * 18 ) );
            }
        }

        // Player inventory
        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlot( new Slot( playerInventory, x + y * 9 + 9, 8 + x * 18, playerInvStartY + 1 + y * 18 ) );
            }
        }

        // Player hotbar
        for( int x = 0; x < 9; x++ )
        {
            addSlot( new Slot( playerInventory, x, 8 + x * 18, playerInvStartY + 3 * 18 + 5 ) );
        }
    }

    public ContainerTurtle( Inventory playerInventory, ITurtleAccess turtle )
    {
        this( playerInventory, turtle, 134, 175 );
    }

    public ContainerTurtle( Inventory playerInventory, ITurtleAccess turtle, IComputer computer )
    {
        this( playerInventory, turtle );
        m_computer = computer;
    }

    public int getSelectedSlot()
    {
        return m_selectedSlot;
    }

    private void sendStateToPlayer( ContainerListener listener )
    {
        int selectedSlot = m_turtle.getSelectedSlot();
        listener.onContainerPropertyUpdate( this, PROGRESS_ID_SELECTED_SLOT, selectedSlot );
    }

    @Override
    public void addListener( ContainerListener crafting )
    {
        super.addListener( crafting );
        sendStateToPlayer( crafting );
    }

    @Override
    public void sendContentUpdates()
    {
        super.sendContentUpdates();

        int selectedSlot = m_turtle.getSelectedSlot();
        for( ContainerListener listener : listeners )
        {
            if( m_selectedSlot != selectedSlot )
            {
                listener.onContainerPropertyUpdate( this, PROGRESS_ID_SELECTED_SLOT, selectedSlot );
            }
        }
        m_selectedSlot = selectedSlot;
    }

    @Override
    public void setProperty( int id, int value )
    {
        super.setProperty( id, value );
        switch( id )
        {
            case PROGRESS_ID_SELECTED_SLOT:
            {
                m_selectedSlot = value;
                break;
            }
        }
    }

    @Override
    public boolean canUse( @Nonnull PlayerEntity player )
    {
        TileTurtle turtle = ((TurtleBrain) m_turtle).getOwner();
        if( turtle != null )
        {
            return turtle.canPlayerUseInv( player );
        }
        return false;
    }

    @Nonnull
    protected ItemStack tryItemMerge( PlayerEntity player, int slotNum, int firstSlot, int lastSlot, boolean reverse )
    {
        Slot slot = slotList.get( slotNum );
        ItemStack originalStack = ItemStack.EMPTY;
        if( slot != null && slot.hasStack() )
        {
            ItemStack clickedStack = slot.getStack();
            originalStack = clickedStack.copy();
            if( !insertItem( clickedStack, firstSlot, lastSlot, reverse ) )
            {
                return ItemStack.EMPTY;
            }

            if( clickedStack.isEmpty() )
            {
                slot.setStack( ItemStack.EMPTY );
            }
            else
            {
                slot.markDirty();
            }

            if( clickedStack.getAmount() != originalStack.getAmount() )
            {
                slot.onTakeItem( player, clickedStack );
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }
        return originalStack;
    }

    @Nonnull
    @Override
    public ItemStack transferSlot( PlayerEntity player, int slotNum )
    {
        if( slotNum >= 0 && slotNum < 16 )
        {
            return tryItemMerge( player, slotNum, 16, 52, true );
        }
        else if( slotNum >= 16 )
        {
            return tryItemMerge( player, slotNum, 0, 16, false );
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public IComputer getComputer()
    {
        return m_computer;
    }
}
