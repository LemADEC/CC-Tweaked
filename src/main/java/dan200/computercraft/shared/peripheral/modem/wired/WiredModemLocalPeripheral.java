/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2018. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem.wired;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a local peripheral exposed on the wired network
 *
 * This is responsible for getting the peripheral in world, tracking id and type and determining whether
 * it has changed.
 */
public final class WiredModemLocalPeripheral
{
    private int id;
    private String type;

    private IPeripheral peripheral;

    /**
     * Attach a new peripheral from the world
     *
     * @param world     The world to search in
     * @param origin    The position to search from
     * @param direction The direction so search in
     * @return Whether the peripheral changed.
     */
    public boolean attach( @Nonnull World world, @Nonnull BlockPos origin, @Nonnull EnumFacing direction )
    {
        IPeripheral oldPeripheral = this.peripheral;
        IPeripheral peripheral = this.peripheral = getPeripheralFrom( world, origin, direction );

        if( peripheral == null )
        {
            return oldPeripheral != null;
        }
        else
        {
            String type = peripheral.getType();
            int id = this.id;

            if( id > 0 && this.type == null )
            {
                // If we had an ID but no type, then just set the type.
                this.type = type;
            }
            else if( id < 0 || !type.equals( this.type ) )
            {
                this.type = type;
                this.id = IDAssigner.getNextIDFromFile( new File(
                    ComputerCraft.getWorldDir( world ),
                    "computer/lastid_" + type + ".txt"
                ) );
            }

            return oldPeripheral == null || !oldPeripheral.equals( peripheral );
        }
    }

    /**
     * Detach the current peripheral
     *
     * @return Whether the peripheral changed
     */
    public boolean detach()
    {
        if( peripheral == null ) return false;
        peripheral = null;
        return true;
    }

    @Nullable
    public String getConnectedName()
    {
        return peripheral != null ? type + "_" + id : null;
    }

    @Nullable
    public IPeripheral getPeripheral()
    {
        return peripheral;
    }

    public boolean hasPeripheral()
    {
        return peripheral != null;
    }

    public void extendMap( @Nonnull Map<String, IPeripheral> peripherals )
    {
        if( peripheral != null ) peripherals.put( type + "_" + id, peripheral );
    }

    public Map<String, IPeripheral> toMap()
    {
        return peripheral == null
            ? Collections.emptyMap()
            : Collections.singletonMap( type + "_" + id, peripheral );
    }

    public void writeNBT( @Nonnull NBTTagCompound tag, @Nonnull String suffix )
    {
        if( id >= 0 ) tag.setInteger( "peripheralID" + suffix, id );
        if( type != null ) tag.setString( "peripheralType" + suffix, type );
    }

    public void readNBT( @Nonnull NBTTagCompound tag, @Nonnull String suffix )
    {
        id = tag.hasKey( "peripheralID" + suffix, Constants.NBT.TAG_ANY_NUMERIC )
            ? tag.getInteger( "peripheralID" + suffix ) : -1;

        type = tag.hasKey( "peripheralType" + suffix, Constants.NBT.TAG_STRING )
            ? tag.getString( "peripheralType" + suffix ) : null;
    }

    private static IPeripheral getPeripheralFrom( World world, BlockPos pos, EnumFacing direction )
    {
        BlockPos offset = pos.offset( direction );

        Block block = world.getBlockState( offset ).getBlock();
        if( block == ComputerCraft.Blocks.wiredModemFull || block == ComputerCraft.Blocks.cable ) return null;

        IPeripheral peripheral = PeripheralUtil.getPeripheral( world, offset, direction.getOpposite() );
        return peripheral instanceof WiredModemPeripheral ? null : peripheral;
    }
}
