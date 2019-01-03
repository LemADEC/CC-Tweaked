/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.BundledRedstone;
import dan200.computercraft.shared.Peripherals;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.RedstoneUtil;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class TileComputerBase extends TileGeneric implements IComputerTile, Tickable
{
    private static final String TAG_ID = "computer_id";
    private static final String TAG_LABEL = "computer_label";
    private static final String TAG_INSTANCE = "instance_id";

    private int m_instanceId = -1;
    private int m_computerId = -1;
    protected String m_label = null;
    private boolean m_on = false;
    boolean m_startOn = false;
    private boolean m_fresh = false;

    private final ComputerFamily family;

    public TileComputerBase( BlockEntityType<? extends TileGeneric> type, ComputerFamily family )
    {
        super( type );
        this.family = family;
    }

    protected void unload()
    {
        if( m_instanceId >= 0 )
        {
            if( !getWorld().isClient ) ComputerCraft.serverComputerRegistry.remove( m_instanceId );
            m_instanceId = -1;
        }
    }

    @Override
    public void destroy()
    {
        unload();
        for( Direction dir : DirectionUtil.FACINGS )
        {
            RedstoneUtil.propagateRedstoneOutput( getWorld(), getPos(), dir );
        }
    }

    @Override
    public void invalidate()
    {
        unload();
        super.invalidate();
    }

    public abstract void openGUI( PlayerEntity player );

    protected boolean canNameWithTag( PlayerEntity player )
    {
        return false;
    }

    protected boolean onDefaultComputerInteract( PlayerEntity player )
    {
        if( !getWorld().isClient && isUsable( player, false ) )
        {
            createServerComputer().turnOn();
            openGUI( player );
        }
        return true;
    }

    @Override
    public boolean onActivate( PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ )
    {
        ItemStack item = player.getStackInHand( hand );
        if( !item.isEmpty() && item.getItem() == Items.NAME_TAG && canNameWithTag( player ) )
        {
            // Label to rename computer
            if( !getWorld().isClient && item.hasDisplayName() )
            {
                setLabel( item.getDisplayName().getText() );
                item.subtractAmount( 1 );
            }
            return true;
        }
        else if( !player.isSneaking() )
        {
            // Regular right click to activate computer
            return onDefaultComputerInteract( player );
        }
        return false;
    }

    @Override
    public void onNeighbourChange( @Nonnull BlockPos neighbour )
    {
        updateSideInput( neighbour );
    }

    @Override
    public void onNeighbourTileEntityChange( @Nonnull BlockPos neighbour )
    {
        updateSideInput( neighbour );
    }

    @Override
    public void tick()
    {
        if( getWorld().isClient ) return;
        ServerComputer computer = createServerComputer();
        if( computer == null ) return;

        // If the computer isn't on and should be, then turn it on
        if( m_startOn || (m_fresh && m_on) )
        {
            computer.turnOn();
            m_startOn = false;
        }

        computer.keepAlive();

        m_fresh = false;
        m_computerId = computer.getId();
        m_label = computer.getLabel();
        m_on = computer.isOn();

        // Update the block state if needed. We don't fire a block update intentionally,
        // as this only really is needed on the client side.
        updateBlockState( computer.getState() );

        if( computer.hasOutputChanged() ) updateOutput();
    }

    protected abstract void updateBlockState( ComputerState newState );

    @Nonnull
    @Override
    public CompoundTag toTag( CompoundTag nbt )
    {
        // Save id, label and power state
        if( m_computerId >= 0 ) nbt.putInt( TAG_ID, m_computerId );
        if( m_label != null ) nbt.putString( TAG_LABEL, m_label );
        nbt.putBoolean( "on", m_on );

        return super.toTag( nbt );
    }

    @Override
    public void fromTag( CompoundTag nbt )
    {
        super.fromTag( nbt );

        // Load ID, label and power state
        m_computerId = nbt.containsKey( TAG_ID ) ? nbt.getInt( TAG_ID ) : -1;
        m_label = nbt.containsKey( TAG_LABEL ) ? nbt.getString( TAG_LABEL ) : null;
        m_on = m_startOn = nbt.getBoolean( "on" );
    }

    protected boolean isPeripheralBlockedOnSide( Direction localSide )
    {
        return false;
    }

    protected boolean isRedstoneBlockedOnSide( Direction localSide )
    {
        return false;
    }

    protected abstract Direction getDirection();

    protected Direction remapToLocalSide( Direction globalSide )
    {
        return DirectionUtil.toLocal( getDirection(), globalSide );
    }

    private void updateSideInput( ServerComputer computer, Direction dir, BlockPos offset )
    {
        Direction offsetSide = dir.getOpposite();
        Direction localDir = remapToLocalSide( dir );
        if( !isRedstoneBlockedOnSide( localDir ) )
        {
            computer.setRedstoneInput( localDir.getId(), getWorld().getEmittedRedstonePower( offset, offsetSide.getOpposite() ) );
            computer.setBundledRedstoneInput( localDir.getId(), BundledRedstone.getOutput( getWorld(), offset, offsetSide ) );
        }
        if( !isPeripheralBlockedOnSide( localDir ) )
        {
            computer.setPeripheral( localDir.getId(), Peripherals.getPeripheral( getWorld(), offset, offsetSide ) );
        }
    }

    public void updateAllInputs()
    {
        if( getWorld() == null || getWorld().isClient ) return;

        // Update all sides
        ServerComputer computer = getServerComputer();
        if( computer == null ) return;

        BlockPos pos = computer.getPosition();
        for( Direction dir : DirectionUtil.FACINGS )
        {
            updateSideInput( computer, dir, pos.offset( dir ) );
        }
    }

    private void updateSideInput( BlockPos neighbour )
    {
        if( getWorld() == null || getWorld().isClient ) return;

        ServerComputer computer = getServerComputer();
        if( computer == null ) return;

        // Find the appropriate side and update.
        BlockPos pos = computer.getPosition();
        for( Direction dir : DirectionUtil.FACINGS )
        {
            BlockPos offset = pos.offset( dir );
            if( offset.equals( neighbour ) )
            {
                updateSideInput( computer, dir, offset );
                break;
            }
        }
    }

    public void updateOutput()
    {
        // Update redstone
        updateBlock();
        for( Direction dir : DirectionUtil.FACINGS )
        {
            RedstoneUtil.propagateRedstoneOutput( getWorld(), getPos(), dir );
        }
    }

    protected abstract ServerComputer createComputer( int instanceID, int id );

    public abstract ComputerProxy createProxy();

    @Override
    public int getComputerId()
    {
        return m_computerId;
    }

    @Override
    public String getLabel()
    {
        return m_label;
    }

    @Override
    public void setComputerId( int id )
    {
        if( !getWorld().isClient && m_computerId != id )
        {
            m_computerId = id;
            ServerComputer computer = getServerComputer();
            if( computer != null )
            {
                computer.setID( m_computerId );
            }
            markDirty();
        }
    }

    @Override
    public void setLabel( String label )
    {
        if( !getWorld().isClient && !Objects.equals( this.m_label, label ) )
        {
            m_label = label;
            ServerComputer computer = getServerComputer();
            if( computer != null ) computer.setLabel( label );
            markDirty();
        }
    }

    @Override
    public ComputerFamily getFamily()
    {
        return family;
    }

    public ServerComputer createServerComputer()
    {
        if( getWorld().isClient ) return null;

        boolean changed = false;
        if( m_instanceId < 0 )
        {
            m_instanceId = ComputerCraft.serverComputerRegistry.getUnusedInstanceID();
            changed = true;
        }

        if( !ComputerCraft.serverComputerRegistry.contains( m_instanceId ) )
        {
            ServerComputer computer = createComputer( m_instanceId, m_computerId );
            ComputerCraft.serverComputerRegistry.add( m_instanceId, computer );
            m_fresh = true;
            changed = true;
        }

        if( changed )
        {
            updateBlock();
            updateAllInputs();
        }
        return ComputerCraft.serverComputerRegistry.get( m_instanceId );
    }

    public ServerComputer getServerComputer()
    {
        return !getWorld().isClient ? ComputerCraft.serverComputerRegistry.get( m_instanceId ) : null;
    }

    public ClientComputer createClientComputer()
    {
        if( !getWorld().isClient || m_instanceId < 0 ) return null;

        ClientComputer computer = ComputerCraft.clientComputerRegistry.get( m_instanceId );
        if( computer == null )
        {
            ComputerCraft.clientComputerRegistry.add( m_instanceId, computer = new ClientComputer( m_instanceId ) );
        }
        return computer;
    }

    public ClientComputer getClientComputer()
    {
        return getWorld().isClient ? ComputerCraft.clientComputerRegistry.get( m_instanceId ) : null;
    }

    // Networking stuff

    @Override
    protected CompoundTag writeDescription( CompoundTag nbt )
    {
        // The client needs to know about the computer ID and label in order to provide pick-block
        // functionality
        if( m_computerId >= 0 ) nbt.putInt( TAG_ID, m_computerId );
        if( m_label != null ) nbt.putString( TAG_LABEL, m_label );
        nbt.putInt( TAG_INSTANCE, createServerComputer().getInstanceID() );

        return super.writeDescription( nbt );
    }

    @Override
    protected void readDescription( CompoundTag nbt )
    {
        m_computerId = nbt.containsKey( TAG_ID ) ? nbt.getInt( TAG_ID ) : -1;
        m_label = nbt.containsKey( TAG_LABEL ) ? nbt.getString( TAG_LABEL ) : null;
        m_instanceId = nbt.containsKey( TAG_INSTANCE ) ? nbt.getInt( TAG_INSTANCE ) : -1;

        super.readDescription( nbt );
    }

    protected void transferStateFrom( TileComputerBase copy )
    {
        if( copy.m_computerId != m_computerId || copy.m_instanceId != m_instanceId )
        {
            unload();
            m_instanceId = copy.m_instanceId;
            m_computerId = copy.m_computerId;
            m_label = copy.m_label;
            m_on = copy.m_on;
            m_startOn = copy.m_startOn;
            updateBlock();
        }
        copy.m_instanceId = -1;
    }
}
