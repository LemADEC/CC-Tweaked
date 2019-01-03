/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem.wireless;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.peripheral.modem.TileModemBase;
import dan200.computercraft.shared.util.NamedBlockEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileWirelessModem extends TileModemBase
{
    public static final NamedBlockEntityType<TileWirelessModem> FACTORY_NORMAL = NamedBlockEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "wireless_modem_normal" ),
        f -> new TileWirelessModem( f, false )
    );

    public static final NamedBlockEntityType<TileWirelessModem> FACTORY_ADVANCED = NamedBlockEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "wireless_modem_advanced" ),
        f -> new TileWirelessModem( f, true )
    );

    private static class Peripheral extends WirelessModemPeripheral
    {
        private final TileWirelessModem m_entity;

        Peripheral( TileWirelessModem entity )
        {
            super( new ModemState(), entity.advanced );
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
            BlockPos pos = m_entity.getPos().offset( m_entity.m_direction );
            return new Vec3d( pos.getX(), pos.getY(), pos.getZ() );
        }

        @Override
        public boolean equals( IPeripheral other )
        {
            return this == other || (other instanceof Peripheral && m_entity == ((Peripheral) other).m_entity);
        }
    }

    private final boolean advanced;

    private boolean m_directionCorrect = false;
    private Direction m_direction = Direction.DOWN;

    public TileWirelessModem( BlockEntityType<? extends TileModemBase> type, boolean advanced )
    {
        super( type );
        this.advanced = advanced;
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
    public void markDirty()
    {
        super.markDirty();
        if( world != null )
        {
            m_directionCorrect = true;
            m_direction = getDirection();
        }
        else
        {
            m_directionCorrect = false;
        }
    }

    @Override
    public void resetBlock()
    {
        super.resetBlock();
        m_directionCorrect = false;
    }

    @Override
    public void tick()
    {
        super.tick();
        if( !m_directionCorrect )
        {
            m_directionCorrect = true;
            m_direction = getDirection();
        }
    }

    @Override
    public Direction getDirection()
    {
        return getCachedState().get( BlockWirelessModem.FACING );
    }

    @Override
    protected void updateBlockState()
    {
        boolean on = m_modem.getModemState().isOpen();
        BlockState state = getCachedState();
        if( state.get( BlockWirelessModem.ON ) != on )
        {
            getWorld().setBlockState( getPos(), state.with( BlockWirelessModem.ON, on ) );
        }
    }

    @Override
    protected ModemPeripheral createPeripheral()
    {
        return new Peripheral( this );
    }
}
