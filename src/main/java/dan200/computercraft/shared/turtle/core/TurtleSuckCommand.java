/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.event.TurtleEvent;
import dan200.computercraft.api.turtle.event.TurtleInventoryEvent;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.ItemStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class TurtleSuckCommand implements ITurtleCommand
{
    private final InteractDirection m_direction;
    private final int m_quantity;

    public TurtleSuckCommand( InteractDirection direction, int quantity )
    {
        m_direction = direction;
        m_quantity = quantity;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Sucking nothing is easy
        if( m_quantity == 0 )
        {
            turtle.playAnimation( TurtleAnimation.Wait );
            return TurtleCommandResult.success();
        }

        // Get world direction from direction
        Direction direction = m_direction.toWorldDir( turtle );

        // Get inventory for thing in front
        World world = turtle.getWorld();
        BlockPos oldPosition = turtle.getPosition();
        BlockPos newPosition = oldPosition.offset( direction );
        Direction side = direction.getOpposite();

        Inventory inventory = InventoryUtil.getInventory( world, newPosition, side );

        // Fire the event, exiting if it is cancelled.
        TurtlePlayer player = TurtlePlaceCommand.createPlayer( turtle, oldPosition, direction );
        TurtleInventoryEvent.Suck event = new TurtleInventoryEvent.Suck( turtle, player, world, newPosition, inventory );
        if( TurtleEvent.post( event ) )
        {
            return TurtleCommandResult.failure( event.getFailureMessage() );
        }

        if( inventory != null )
        {
            ItemStorage storage = ItemStorage.wrap( inventory, side );
            // Take from inventory of thing in front
            ItemStack stack = InventoryUtil.takeItems( m_quantity, storage );
            if( !stack.isEmpty() )
            {
                // Try to place into the turtle
                ItemStack remainder = InventoryUtil.storeItems( stack, ItemStorage.wrap( turtle.getInventory() ), turtle.getSelectedSlot() );
                if( !remainder.isEmpty() )
                {
                    // Put the remainder back in the inventory
                    InventoryUtil.storeItems( remainder, storage );
                }

                // Return true if we consumed anything
                if( remainder != stack )
                {
                    turtle.playAnimation( TurtleAnimation.Wait );
                    return TurtleCommandResult.success();
                }
                else
                {
                    return TurtleCommandResult.failure( "No space for items" );
                }
            }
            return TurtleCommandResult.failure( "No items to take" );
        }
        else
        {
            // Suck up loose items off the ground
            BoundingBox aabb = new BoundingBox(
                newPosition.getX(), newPosition.getY(), newPosition.getZ(),
                newPosition.getX() + 1.0, newPosition.getY() + 1.0, newPosition.getZ() + 1.0
            );
            List<Entity> list = world.getEntities( (Entity) null, aabb, EntityPredicates.VALID_ENTITY );
            if( list.size() > 0 )
            {
                boolean foundItems = false;
                boolean storedItems = false;
                for( Entity entity : list )
                {
                    if( entity instanceof ItemEntity && !entity.isValid() )
                    {
                        // Suck up the item
                        foundItems = true;
                        ItemEntity entityItem = (ItemEntity) entity;
                        ItemStack stack = entityItem.getStack().copy();
                        ItemStack storeStack;
                        ItemStack leaveStack;
                        if( stack.getAmount() > m_quantity )
                        {
                            storeStack = stack.split( m_quantity );
                            leaveStack = stack;
                        }
                        else
                        {
                            storeStack = stack;
                            leaveStack = ItemStack.EMPTY;
                        }
                        ItemStack remainder = InventoryUtil.storeItems( storeStack, ItemStorage.wrap( turtle.getInventory() ), turtle.getSelectedSlot() );
                        if( remainder != storeStack )
                        {
                            storedItems = true;
                            if( remainder.isEmpty() && leaveStack.isEmpty() )
                            {
                                entityItem.invalidate();
                            }
                            else if( remainder.isEmpty() )
                            {
                                entityItem.setStack( leaveStack );
                            }
                            else if( leaveStack.isEmpty() )
                            {
                                entityItem.setStack( remainder );
                            }
                            else
                            {
                                leaveStack.addAmount( remainder.getAmount() );
                                entityItem.setStack( leaveStack );
                            }
                            break;
                        }
                    }
                }

                if( foundItems )
                {
                    if( storedItems )
                    {
                        // Play fx
                        world.fireWorldEvent( 1000, oldPosition, 0 ); // BLOCK_DISPENSER_DISPENSE
                        turtle.playAnimation( TurtleAnimation.Wait );
                        return TurtleCommandResult.success();
                    }
                    else
                    {
                        return TurtleCommandResult.failure( "No space for items" );
                    }
                }
            }
            return TurtleCommandResult.failure( "No items to take" );
        }
    }
}
