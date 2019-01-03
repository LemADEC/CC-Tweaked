/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.peripherals;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketModem extends AbstractPocketUpgrade
{
    private final boolean m_advanced;

    public PocketModem( boolean advanced )
    {
        super(
            new Identifier( "computercraft", advanced ? "advanced_modem" : "wireless_modem" ),
            advanced
                ? "upgrade.computercraft.wireless_modem_advanced.adjective"
                : "upgrade.computercraft.wireless_modem_normal.adjective",
            advanced
                ? ComputerCraft.Blocks.wirelessModemAdvanced
                : ComputerCraft.Blocks.wirelessModemNormal
        );
        this.m_advanced = advanced;
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral( @Nonnull IPocketAccess access )
    {
        return new PocketModemPeripheral( m_advanced );
    }

    @Override
    public void update( @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral )
    {
        if( peripheral instanceof PocketModemPeripheral )
        {
            PocketModemPeripheral modem = (PocketModemPeripheral) peripheral;

            Entity entity = access.getEntity();
            if( entity != null )
            {
                modem.setLocation( entity.getEntityWorld(), entity.x, entity.y + entity.getEyeHeight(), entity.z );
            }

            ModemState state = modem.getModemState();
            if( state.pollChanged() ) access.setLight( state.isOpen() ? 0xBA0000 : -1 );
        }
    }
}
