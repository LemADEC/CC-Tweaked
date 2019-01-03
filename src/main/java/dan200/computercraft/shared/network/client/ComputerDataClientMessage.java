/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.client;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;

/**
 * Provides additional data about a client computer, such as its ID and current state.
 */
public class ComputerDataClientMessage extends ComputerClientMessage
{
    private static final Identifier ID = new Identifier( ComputerCraft.MOD_ID, "computer_changed" );

    private ComputerState state;
    private CompoundTag userData;

    public ComputerDataClientMessage( ServerComputer computer )
    {
        super( computer.getInstanceID() );
        this.state = computer.getState();
        this.userData = computer.getUserData();
    }

    public ComputerDataClientMessage()
    {
    }

    @Override
    public @Nonnull Identifier getId()
    {
        return ID;
    }

    public ComputerState getState()
    {
        return state;
    }

    public CompoundTag getUserData()
    {
        return userData;
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        super.toBytes( buf );
        buf.writeEnumConstant( state );
        buf.writeCompoundTag( userData );
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        super.fromBytes( buf );
        state = buf.readEnumConstant( ComputerState.class );
        userData = buf.readCompoundTag();
    }
}
