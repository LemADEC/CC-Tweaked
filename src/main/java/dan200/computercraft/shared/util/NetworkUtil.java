/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.util.PacketByteBuf;

public final class NetworkUtil
{
    private NetworkUtil()
    {
    }

    public static void writeFamily( PacketByteBuf buf, ComputerFamily family )
    {
        buf.writeVarInt( family.ordinal() );
    }

    private static final ComputerFamily[] FAMILIES = ComputerFamily.values();

    public static ComputerFamily readFamily( PacketByteBuf buf )
    {
        int idx = buf.readVarInt();
        return idx >= 0 && idx < FAMILIES.length ? FAMILIES[idx] : ComputerFamily.Normal;
    }
}
