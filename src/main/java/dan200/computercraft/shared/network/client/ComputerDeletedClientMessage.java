/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.client;

import dan200.computercraft.ComputerCraft;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public class ComputerDeletedClientMessage extends ComputerClientMessage
{
    private static final Identifier ID = new Identifier( ComputerCraft.MOD_ID, "computer_deleted" );

    public ComputerDeletedClientMessage( int instanceId )
    {
        super( instanceId );
    }

    public ComputerDeletedClientMessage()
    {
    }

    @Override
    public @Nonnull Identifier getId()
    {
        return ID;
    }
}
