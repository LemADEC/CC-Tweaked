/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.TurtleUpgrades;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ItemComputerBase;
import dan200.computercraft.shared.turtle.blocks.BlockTurtle;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTurtle extends ItemComputerBase implements ITurtleItem
{
    private static final String TAG_ID = "computer_id";
    private static final String TAG_RIGHT_UPGRADE = "right_upgrade";
    private static final String TAG_LEFT_UPGRADE = "left_upgrade";
    private static final String TAG_FUEL = "fuel";
    private static final String TAG_COLOUR = "colour";
    private static final String TAG_OVERLAY = "overlay";

    public ItemTurtle( BlockTurtle block, Settings settings )
    {
        super( block, settings );
    }

    public ItemStack create( int id, String label, int colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, int fuelLevel, Identifier overlay )
    {
        // Build the stack
        ItemStack stack = new ItemStack( this );
        if( label != null ) stack.setDisplayName( new StringTextComponent( label ) );
        if( id >= 0 ) stack.getOrCreateTag().putInt( TAG_ID, id );
        if( colour != -1 ) stack.getOrCreateTag().putInt( TAG_COLOUR, colour );
        if( fuelLevel > 0 ) stack.getOrCreateTag().putInt( TAG_FUEL, fuelLevel );
        if( overlay != null ) stack.getOrCreateTag().putString( TAG_OVERLAY, overlay.toString() );

        if( leftUpgrade != null )
        {
            stack.getOrCreateTag().putString( TAG_LEFT_UPGRADE, leftUpgrade.getUpgradeId().toString() );
        }

        if( rightUpgrade != null )
        {
            stack.getOrCreateTag().putString( TAG_RIGHT_UPGRADE, rightUpgrade.getUpgradeId().toString() );
        }

        return stack;
    }

    @Override
    public void addStacksForDisplay( ItemGroup group, DefaultedList<ItemStack> list )
    {
        if( !isInItemGroup( group ) ) return;

        ComputerFamily family = getFamily();

        list.add( create( -1, null, -1, null, null, 0, null ) );
        for( ITurtleUpgrade upgrade : TurtleUpgrades.getVanillaUpgrades() )
        {
            if( !TurtleUpgrades.suitableForFamily( family, upgrade ) ) continue;

            list.add( create( -1, null, -1, upgrade, null, 0, null ) );
        }
    }

    @Override
    protected boolean afterBlockPlaced( BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state )
    {
        boolean changed = super.afterBlockPlaced( pos, world, player, stack, state );

        BlockEntity entity = world.getBlockEntity( pos );
        if( !world.isClient && entity instanceof ITurtleTile )
        {
            ITurtleTile turtle = (ITurtleTile) entity;
            setupTurtleAfterPlacement( stack, turtle );
            changed = true;
        }

        return changed;
    }

    public void setupTurtleAfterPlacement( @Nonnull ItemStack stack, ITurtleTile turtle )
    {
        // Set ID
        int id = getComputerId( stack );
        if( id >= 0 )
        {
            turtle.setComputerId( id );
        }

        // Set Label
        String label = getLabel( stack );
        if( label != null )
        {
            turtle.setLabel( label );
        }

        // Set Upgrades
        for( TurtleSide side : TurtleSide.values() )
        {
            turtle.getAccess().setUpgrade( side, getUpgrade( stack, side ) );
        }

        // Set Fuel level
        int fuelLevel = getFuelLevel( stack );
        turtle.getAccess().setFuelLevel( fuelLevel );

        // Set colour
        int colour = getColour( stack );
        if( colour != -1 )
        {
            turtle.getAccess().setColour( colour );
        }

        // Set overlay
        Identifier overlay = getOverlay( stack );
        if( overlay != null )
        {
            ((TurtleBrain) turtle.getAccess()).setOverlay( overlay );
        }
    }

    @Override
    public TextComponent getTranslatedNameTrimmed( ItemStack stack )
    {
        String baseString = getTranslationKey( stack );
        ITurtleUpgrade left = getUpgrade( stack, TurtleSide.Left );
        ITurtleUpgrade right = getUpgrade( stack, TurtleSide.Right );
        if( left != null && right != null )
        {
            return new TranslatableTextComponent( baseString + ".upgraded_twice",
                new TranslatableTextComponent( right.getUnlocalisedAdjective() ),
                new TranslatableTextComponent( left.getUnlocalisedAdjective() )
            );
        }
        else if( left != null )
        {
            return new TranslatableTextComponent( baseString + ".upgraded",
                new TranslatableTextComponent( left.getUnlocalisedAdjective() )
            );
        }
        else if( right != null )
        {
            return new TranslatableTextComponent( baseString + ".upgraded",
                new TranslatableTextComponent( right.getUnlocalisedAdjective() )
            );
        }
        else
        {
            return new TranslatableTextComponent( baseString );
        }
    }

    @Override
    public ItemStack withFamily( @Nonnull ItemStack stack, @Nonnull ComputerFamily family )
    {
        return TurtleItemFactory.create(
            getComputerId( stack ), getLabel( stack ),
            getColour( stack ), family,
            getUpgrade( stack, TurtleSide.Left ), getUpgrade( stack, TurtleSide.Right ),
            getFuelLevel( stack ), getOverlay( stack )
        );
    }

    @Override
    public ItemStack withColour( ItemStack stack, int colour )
    {
        return TurtleItemFactory.create(
            getComputerId( stack ), getLabel( stack ), colour, getFamily(),
            getUpgrade( stack, TurtleSide.Left ), getUpgrade( stack, TurtleSide.Right ),
            getFuelLevel( stack ), getOverlay( stack )
        );
    }


    @Override
    public int getComputerId( @Nonnull ItemStack stack )
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.containsKey( TAG_ID ) ? tag.getInt( TAG_ID ) : -1;
    }

    @Override
    public ITurtleUpgrade getUpgrade( @Nonnull ItemStack stack, TurtleSide side )
    {
        CompoundTag tag = stack.getTag();
        if( tag == null ) return null;

        String key = side == TurtleSide.Left ? TAG_LEFT_UPGRADE : TAG_RIGHT_UPGRADE;
        return tag.containsKey( key ) ? TurtleUpgrades.get( tag.getString( key ) ) : null;
    }

    @Override
    public int getColour( @Nonnull ItemStack stack )
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.containsKey( TAG_COLOUR ) ? tag.getInt( TAG_COLOUR ) : -1;
    }

    @Override
    public Identifier getOverlay( @Nonnull ItemStack stack )
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.containsKey( TAG_OVERLAY ) ? new Identifier( tag.getString( TAG_OVERLAY ) ) : null;
    }

    @Override
    public int getFuelLevel( @Nonnull ItemStack stack )
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.containsKey( TAG_FUEL ) ? tag.getInt( TAG_FUEL ) : 0;
    }
}
