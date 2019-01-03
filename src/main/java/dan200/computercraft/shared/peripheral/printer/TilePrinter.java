/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.printer;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.peripheral.IPeripheralTile;
import dan200.computercraft.shared.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.TextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

public class TilePrinter extends TileGeneric implements SidedInventory, DefaultInventory, IPeripheralTile
{
    public static final NamedBlockEntityType<TilePrinter> FACTORY = NamedBlockEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "printer" ),
        TilePrinter::new
    );

    private static final int[] bottomSlots = { 7, 8, 9, 10, 11, 12 };
    private static final int[] topSlots = { 1, 2, 3, 4, 5, 6 };
    private static final int[] sideSlots = { 0 };

    private final DefaultedList<ItemStack> m_inventory = DefaultedList.create( 13, ItemStack.EMPTY );
    private final ItemStorage m_itemHandlerAll = ItemStorage.wrap( this );
    // private IItemHandlerModifiable[] m_itemHandlerSides;

    private final Terminal m_page = new Terminal( ItemPrintout.LINE_MAX_LENGTH, ItemPrintout.LINES_PER_PAGE );
    private String m_pageTitle = "";
    private boolean m_printing = false;

    public TilePrinter( BlockEntityType<? extends TilePrinter> factory )
    {
        super( factory );
    }

    @Override
    public boolean onActivate( PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ )
    {
        if( player.isSneaking() ) return false;

        if( !getWorld().isClient ) ComputerCraft.openPrinterGUI( player, this );
        return true;
    }

    @Override
    public void fromTag( CompoundTag nbt )
    {
        super.fromTag( nbt );

        // Read page
        synchronized( m_page )
        {
            m_printing = nbt.getBoolean( "printing" );
            m_pageTitle = nbt.getString( "pageTitle" );
            m_page.readFromNBT( nbt );
        }

        // Read inventory
        synchronized( m_inventory )
        {
            ListTag nbttaglist = nbt.getList( "Items", NBTUtil.TAG_COMPOUND );
            for( int i = 0; i < nbttaglist.size(); i++ )
            {
                CompoundTag itemTag = nbttaglist.getCompoundTag( i );
                int j = itemTag.getByte( "Slot" ) & 0xff;
                if( j >= 0 && j < m_inventory.size() ) m_inventory.set( j, ItemStack.fromTag( itemTag ) );
            }
        }
    }

    @Nonnull
    @Override
    public CompoundTag toTag( CompoundTag nbt )
    {
        // Write page
        synchronized( m_page )
        {
            nbt.putBoolean( "printing", m_printing );
            nbt.putString( "pageTitle", m_pageTitle );
            m_page.writeToNBT( nbt );
        }

        // Write inventory
        synchronized( m_inventory )
        {
            ListTag itemList = new ListTag();
            for( int i = 0; i < m_inventory.size(); i++ )
            {
                ItemStack stack = m_inventory.get( i );
                if( !stack.isEmpty() )
                {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putByte( "Slot", (byte) i );
                    stack.toTag( itemTag );
                    itemList.add( itemTag );
                }
            }
            nbt.put( "Items", itemList );
        }

        return super.toTag( nbt );
    }

    @Override
    public final void readDescription( @Nonnull CompoundTag nbt )
    {
        super.readDescription( nbt );
        updateBlock();
    }

    public boolean isPrinting()
    {
        return m_printing;
    }

    // Inventory implementation
    @Override
    public int getInvSize()
    {
        return m_inventory.size();
    }

    @Override
    public void clearInv()
    {
        synchronized( m_inventory )
        {
            Collections.fill( m_inventory, ItemStack.EMPTY );
            markDirty();
        }
    }

    @Override
    public boolean isInvEmpty()
    {
        for( ItemStack stack : m_inventory )
        {
            if( !stack.isEmpty() ) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getInvStack( int i )
    {
        return m_inventory.get( i );
    }

    @Override
    public void setInvStack( int i, @Nonnull ItemStack stack )
    {
        synchronized( m_inventory )
        {
            m_inventory.set( i, stack );
            markDirty();
        }
    }

    @Nonnull
    @Override
    public TextComponent getName()
    {
        return getCachedState().getBlock().getTextComponent();
    }

    @Override
    public boolean canPlayerUseInv( PlayerEntity playerEntity )
    {
        return isUsable( playerEntity, false );
    }

    // ISidedInventory implementation

    @Nonnull
    @Override
    public int[] getInvAvailableSlots( @Nonnull Direction side )
    {
        switch( side )
        {
            case DOWN:
                return bottomSlots; // Bottom (Out tray)
            case UP:
                return topSlots; // Top (In tray)
            default:
                return sideSlots; // Sides (Ink)
        }
    }


    @Override
    public boolean canInsertInvStack( int slot, @Nonnull ItemStack itemstack, @Nullable Direction face )
    {
        return true; // TODO: Add some actual validation here
    }

    @Override
    public boolean canExtractInvStack( int slot, @Nonnull ItemStack itemstack, @Nonnull Direction face )
    {
        return true;
    }

    // IPeripheralTile implementation

    @Override
    public IPeripheral getPeripheral( Direction side )
    {
        return new PrinterPeripheral( this );
    }

    public Terminal getCurrentPage()
    {
        return m_printing ? m_page : null;
    }

    public boolean startNewPage()
    {
        synchronized( m_inventory )
        {
            if( !canInputPage() ) return false;
            if( m_printing && !outputPage() ) return false;
            return inputPage();
        }
    }

    public boolean endCurrentPage()
    {
        synchronized( m_inventory )
        {
            if( m_printing && outputPage() )
            {
                return true;
            }
        }
        return false;
    }

    public int getInkLevel()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory.get( 0 );
            return isInk( inkStack ) ? inkStack.getAmount() : 0;
        }
    }

    public int getPaperLevel()
    {
        int count = 0;
        synchronized( m_inventory )
        {
            for( int i = 1; i < 7; i++ )
            {
                ItemStack paperStack = m_inventory.get( i );
                if( !paperStack.isEmpty() && isPaper( paperStack ) )
                {
                    count += paperStack.getAmount();
                }
            }
        }
        return count;
    }

    public void setPageTitle( String title )
    {
        if( m_printing )
        {
            m_pageTitle = title;
        }
    }

    private boolean isInk( @Nonnull ItemStack stack )
    {
        return stack.getItem() instanceof DyeItem;
    }

    private boolean isPaper( @Nonnull ItemStack stack )
    {
        Item item = stack.getItem();
        return item == Items.PAPER
            || (item instanceof ItemPrintout && ((ItemPrintout) item).getType() == ItemPrintout.Type.PAGE);
    }

    private boolean canInputPage()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory.get( 0 );
            return !inkStack.isEmpty() && isInk( inkStack ) && getPaperLevel() > 0;
        }
    }

    private boolean inputPage()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory.get( 0 );
            if( !isInk( inkStack ) ) return false;

            for( int i = 1; i < 7; i++ )
            {
                ItemStack paperStack = m_inventory.get( i );
                if( !paperStack.isEmpty() && isPaper( paperStack ) )
                {
                    // Setup the new page
                    DyeColor dye = ColourUtils.getStackColour( inkStack );
                    m_page.setTextColour( dye != null ? 15 - dye.getId() : 15 );

                    m_page.clear();
                    if( paperStack.getItem() instanceof ItemPrintout )
                    {
                        m_pageTitle = ItemPrintout.getTitle( paperStack );
                        String[] text = ItemPrintout.getText( paperStack );
                        String[] textColour = ItemPrintout.getColours( paperStack );
                        for( int y = 0; y < m_page.getHeight(); y++ )
                        {
                            m_page.setLine( y, text[y], textColour[y], "" );
                        }
                    }
                    else
                    {
                        m_pageTitle = "";
                    }
                    m_page.setCursorPos( 0, 0 );

                    // Decrement ink
                    inkStack.subtractAmount( 1 );
                    if( inkStack.isEmpty() ) m_inventory.set( 0, ItemStack.EMPTY );

                    // Decrement paper
                    paperStack.subtractAmount( 1 );
                    if( paperStack.isEmpty() ) m_inventory.set( i, ItemStack.EMPTY );

                    markDirty();
                    m_printing = true;
                    return true;
                }
            }
            return false;
        }
    }

    private boolean outputPage()
    {
        synchronized( m_page )
        {
            int height = m_page.getHeight();
            String[] lines = new String[height];
            String[] colours = new String[height];
            for( int i = 0; i < height; i++ )
            {
                lines[i] = m_page.getLine( i ).toString();
                colours[i] = m_page.getTextColourLine( i ).toString();
            }

            ItemStack stack = ItemPrintout.createSingleFromTitleAndText( m_pageTitle, lines, colours );
            synchronized( m_inventory )
            {
                ItemStack remainder = InventoryUtil.storeItems( stack, m_itemHandlerAll, 7, 6, 7 );
                if( remainder.isEmpty() )
                {
                    m_printing = false;
                    return true;
                }
            }
            return false;
        }
    }

    private void ejectContents()
    {
        synchronized( m_inventory )
        {
            for( int i = 0; i < 13; i++ )
            {
                ItemStack stack = m_inventory.get( i );
                if( !stack.isEmpty() )
                {
                    // Remove the stack from the inventory
                    setInvStack( i, ItemStack.EMPTY );

                    // Spawn the item in the world
                    BlockPos pos = getPos();
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.75;
                    double z = pos.getZ() + 0.5;
                    ItemEntity entityitem = new ItemEntity( getWorld(), x, y, z, stack );
                    entityitem.velocityX = getWorld().random.nextFloat() * 0.2 - 0.1;
                    entityitem.velocityY = getWorld().random.nextFloat() * 0.2 - 0.1;
                    entityitem.velocityZ = getWorld().random.nextFloat() * 0.2 - 0.1;
                    getWorld().spawnEntity( entityitem );
                }
            }
        }
    }

    @Override
    public void markDirty()
    {
        if( !world.isClient ) updateBlockState();
        super.markDirty();

        BlockState state = getCachedState();
        world.updateListeners( getPos(), state, state, 3 );
    }

    private void updateBlockState()
    {
        boolean top = false, bottom = false;
        synchronized( m_inventory )
        {
            for( int i = 1; i < 7; i++ )
            {
                ItemStack stack = m_inventory.get( i );
                if( !stack.isEmpty() && isPaper( stack ) )
                {
                    top = true;
                    break;
                }
            }
            for( int i = 7; i < 13; i++ )
            {
                ItemStack stack = m_inventory.get( i );
                if( !stack.isEmpty() && isPaper( stack ) )
                {
                    bottom = true;
                    break;
                }
            }
        }

        updateBlockState( top, bottom );
    }

    private void updateBlockState( boolean top, boolean bottom )
    {
        BlockState state = getCachedState();
        if( state.get( BlockPrinter.TOP ) == top & state.get( BlockPrinter.BOTTOM ) == bottom ) return;

        getWorld().setBlockState( getPos(), state.with( BlockPrinter.TOP, top ).with( BlockPrinter.BOTTOM, bottom ) );
    }

    /*
    @Override
    public boolean hasCapability( @Nonnull Capability<?> capability, @Nullable Direction facing )
    {
        return capability == ITEM_HANDLER_CAPABILITY || super.hasCapability( capability, facing );
    }

    @Nullable
    @Override
    public <T> T getCapability( @Nonnull Capability<T> capability, @Nullable Direction facing )
    {
        if( capability == ITEM_HANDLER_CAPABILITY )
        {
            if( facing == null )
            {
                return ITEM_HANDLER_CAPABILITY.cast( m_itemHandlerAll );
            }
            else
            {
                IItemHandlerModifiable[] handlers = m_itemHandlerSides;
                if( handlers == null ) handlers = m_itemHandlerSides = new IItemHandlerModifiable[6];

                int i = facing.ordinal();
                IItemHandlerModifiable handler = handlers[i];
                if( handler == null ) handler = handlers[i] = new SidedInvWrapper( this, facing );

                return ITEM_HANDLER_CAPABILITY.cast( handler );
            }
        }
        return super.getCapability( capability, facing );
    }
    */
}
