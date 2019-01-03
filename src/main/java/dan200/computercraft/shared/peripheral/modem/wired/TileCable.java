/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem.wired;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.network.wired.IWiredElement;
import dan200.computercraft.api.network.wired.IWiredNode;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.command.CommandCopy;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.peripheral.modem.TileModemBase;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.NamedBlockEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class TileCable extends TileModemBase
{
    public static final NamedBlockEntityType<TileCable> FACTORY = NamedBlockEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "cable" ),
        TileCable::new
    );

    private static class CableElement extends WiredModemElement
    {
        private final TileCable m_entity;

        private CableElement( TileCable entity )
        {
            m_entity = entity;
        }

        @Nonnull
        @Override
        public World getWorld()
        {
            return m_entity.getWorld();
        }

        @Nonnull
        @Override
        public Vec3d getPosition()
        {
            BlockPos pos = m_entity.getPos();
            return new Vec3d( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
        }

        @Override
        protected void attachPeripheral( String name, IPeripheral peripheral )
        {
            ((WiredModemPeripheral) m_entity.m_modem).attachPeripheral( name, peripheral );
        }

        @Override
        protected void detachPeripheral( String name )
        {
            ((WiredModemPeripheral) m_entity.m_modem).detachPeripheral( name );
        }
    }

    private boolean m_peripheralAccessAllowed;
    private WiredModemLocalPeripheral m_peripheral = new WiredModemLocalPeripheral();

    private boolean m_destroyed = false;

    private boolean m_hasDirection = false;
    private Direction m_direction = Direction.NORTH;
    private boolean m_connectionsFormed = false;

    private WiredModemElement m_cable;
    private IWiredNode m_node;

    public TileCable( BlockEntityType<? extends TileCable> type )
    {
        super( type );
    }

    @Override
    protected ModemPeripheral createPeripheral()
    {
        m_cable = new CableElement( this );
        m_node = m_cable.getNode();
        return new WiredModemPeripheral( new ModemState(), m_cable )
        {
            @Nonnull
            @Override
            protected WiredModemLocalPeripheral getLocalPeripheral()
            {
                return m_peripheral;
            }

            @Nonnull
            @Override
            public Vec3d getPosition()
            {
                BlockPos pos = getPos().offset( m_direction );
                return new Vec3d( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
            }
        };
    }

    private void remove()
    {
        if( world == null || !world.isClient )
        {
            m_node.remove();
            m_connectionsFormed = false;
        }
    }

    @Override
    public void destroy()
    {
        if( !m_destroyed )
        {
            m_destroyed = true;
            remove();
        }
        super.destroy();
    }

    /*
    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        remove();
    }
    */

    @Override
    public void invalidate()
    {
        super.invalidate();
        remove();
    }

    /*
    @Override
    public void onLoad()
    {
        super.onLoad();
        updateDirection();
    }
    */

    @Override
    public void resetBlock()
    {
        super.resetBlock();
        m_hasDirection = false;
        // TODO: Update in markDirty
    }

    private void updateDirection()
    {
        if( !m_hasDirection )
        {
            m_hasDirection = true;
            m_direction = getDirection();
        }
    }

    @Override
    public Direction getDirection()
    {
        BlockState state = getCachedState();
        CableModemVariant modem = state.get( BlockCable.MODEM );
        return modem != CableModemVariant.None ? modem.getFacing() : Direction.NORTH;
    }

    /*
    @Override
    public void onNeighbourChange()
    {
        // TODO: Update connection state?
        if( !world.isClient && peripheralAccessAllowed )
        {
            if( peripheral.attach( world, getPos(), dir ) ) updateConnectedPeripherals();
        }
    }
    */

    @Override
    public void onNeighbourTileEntityChange( @Nonnull BlockPos neighbour )
    {
        // TODO: Update connection state?

        super.onNeighbourTileEntityChange( neighbour );
        if( !world.isClient && m_peripheralAccessAllowed )
        {
            Direction facing = getDirection();
            if( getPos().offset( facing ).equals( neighbour ) )
            {
                if( m_peripheral.attach( world, getPos(), facing ) ) updateConnectedPeripherals();
            }
        }
    }

    @Override
    public boolean onActivate( PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ )
    {
        if( !canAttachPeripheral() || player.isSneaking() ) return false;

        if( getWorld().isClient ) return true;

        String oldName = m_peripheral.getConnectedName();
        togglePeripheralAccess();
        String newName = m_peripheral.getConnectedName();
        if( !Objects.equal( newName, oldName ) )
        {
            if( oldName != null )
            {
                player.addChatMessage( new TranslatableTextComponent( "gui.computercraft.wired_modem.peripheral_disconnected",
                    CommandCopy.createCopyText( oldName ) ), false );
            }
            if( newName != null )
            {
                player.addChatMessage( new TranslatableTextComponent( "gui.computercraft.wired_modem.peripheral_connected",
                    CommandCopy.createCopyText( newName ) ), false );
            }
        }

        return true;
    }

    @Override
    public void fromTag( CompoundTag nbt )
    {
        super.fromTag( nbt );
        m_peripheralAccessAllowed = nbt.getBoolean( "peripheral_access" );
        m_peripheral.fromTag( nbt, "" );
    }

    @Nonnull
    @Override
    public CompoundTag toTag( CompoundTag nbt )
    {
        nbt.putBoolean( "peripheral_access", m_peripheralAccessAllowed );
        m_peripheral.toTag( nbt, "" );
        return super.toTag( nbt );
    }

    @Override
    protected void updateBlockState()
    {
        BlockState state = getCachedState();
        CableModemVariant oldVariant = state.get( BlockCable.MODEM );
        CableModemVariant newVariant = CableModemVariant
            .from( oldVariant.getFacing(), m_modem.getModemState().isOpen(), m_peripheralAccessAllowed );

        if( oldVariant != newVariant )
        {
            world.setBlockState( getPos(), state.with( BlockCable.MODEM, newVariant ) );
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        updateDirection();

        if( !getWorld().isClient && !m_connectionsFormed )
        {
            m_connectionsFormed = true;

            connectionsChanged();
            if( m_peripheralAccessAllowed )
            {
                m_peripheral.attach( world, pos, getDirection() );
                updateConnectedPeripherals();
            }
        }
    }

    public void connectionsChanged()
    {
        if( getWorld().isClient ) return;

        BlockState state = getCachedState();
        World world = getWorld();
        BlockPos current = getPos();
        for( Direction facing : DirectionUtil.FACINGS )
        {
            BlockPos offset = current.offset( facing );
            if( !world.isBlockLoaded( offset ) ) continue;

            IWiredElement element = ComputerCraftAPI.getWiredElementAt( world, offset, facing.getOpposite() );
            if( element == null ) continue;

            if( BlockCable.canConnectIn( state, facing ) )
            {
                // If we can connect to it then do so
                m_node.connectTo( element.getNode() );
            }
            else if( m_node.getNetwork() == element.getNode().getNetwork() )
            {
                // Otherwise if we're on the same network then attempt to void it.
                m_node.disconnectFrom( element.getNode() );
            }
        }
    }

    public void modemChanged()
    {
        if( getWorld().isClient ) return;

        // If we can no longer attach peripherals, then detach any
        // which may have existed
        if( !canAttachPeripheral() && m_peripheralAccessAllowed )
        {
            m_peripheralAccessAllowed = false;
            m_peripheral.detach();
            m_node.updatePeripherals( Collections.emptyMap() );
            markDirty();
            updateBlockState();
        }
    }

    // private stuff
    private void togglePeripheralAccess()
    {
        if( !m_peripheralAccessAllowed )
        {
            m_peripheral.attach( world, getPos(), getDirection() );
            if( !m_peripheral.hasPeripheral() ) return;

            m_peripheralAccessAllowed = true;
            m_node.updatePeripherals( m_peripheral.toMap() );
        }
        else
        {
            m_peripheral.detach();

            m_peripheralAccessAllowed = false;
            m_node.updatePeripherals( Collections.emptyMap() );
        }

        updateBlockState();
    }

    private void updateConnectedPeripherals()
    {
        Map<String, IPeripheral> peripherals = m_peripheral.toMap();
        if( peripherals.isEmpty() )
        {
            // If there are no peripherals then disable access and update the display state.
            m_peripheralAccessAllowed = false;
            updateBlockState();
        }

        m_node.updatePeripherals( peripherals );
    }

    // IWiredElement capability
    /*
    @Override
    public boolean hasCapability( @Nonnull Capability<?> capability, @Nullable Direction facing )
    {
        if( capability == CapabilityWiredElement.CAPABILITY ) return BlockCable.canConnectIn( getBlockState(), facing );
        return super.hasCapability( capability, facing );
    }

    @Nullable
    @Override
    public <T> T getCapability( @Nonnull Capability<T> capability, @Nullable Direction facing )
    {
        if( capability == CapabilityWiredElement.CAPABILITY )
        {
            return BlockCable.canConnectIn( getBlockState(), facing ) ? CapabilityWiredElement.CAPABILITY.cast( cable ) : null;
        }

        return super.getCapability( capability, facing );
    }
    */

    public IWiredElement getElement( Direction facing )
    {
        return BlockCable.canConnectIn( getCachedState(), facing ) ? m_cable : null;
    }

    // IPeripheralTile

    @Override
    public IPeripheral getPeripheral( Direction side )
    {
        return hasModem() ? super.getPeripheral( side ) : null;
    }

    public boolean hasCable()
    {
        return getCachedState().get( BlockCable.CABLE );
    }

    public boolean hasModem()
    {
        return getCachedState().get( BlockCable.MODEM ) != CableModemVariant.None;
    }

    boolean canAttachPeripheral()
    {
        return hasCable() && hasModem();
    }
}
