/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.core;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

public enum ComputerState implements StringRepresentable
{
    OFF( "off" ),
    ON( "on" ),
    BLINKING( "blinking" );

    private static final ComputerState[] VALUES = ComputerState.values();

    // TODO: Move to dan200.computercraft.shared.computer.core in the future. We can't do it now
    //  as Plethora depends on it.

    private String m_name;

    ComputerState( String name )
    {
        m_name = name;
    }

    @Nonnull
    @Override
    public String asString()
    {
        return m_name;
    }

    @Override
    public String toString()
    {
        return m_name;
    }

    public static ComputerState valueOf( int ordinal )
    {
        return ordinal < 0 || ordinal >= VALUES.length ? ComputerState.OFF : VALUES[ordinal];
    }
}

