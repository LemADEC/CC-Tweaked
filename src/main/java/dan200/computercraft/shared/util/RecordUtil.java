/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.network.NetworkMessage;
import dan200.computercraft.shared.network.client.PlayRecordClientMessage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RecordItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RecordUtil
{
    public static void playRecord( SoundEvent record, String recordInfo, World world, BlockPos pos )
    {
        NetworkMessage packet = record != null ? new PlayRecordClientMessage( pos, record, recordInfo ) : new PlayRecordClientMessage( pos );
        ComputerCraft.sendToAllAround( packet, world, new Vec3d( pos ), 64 );
    }

    public static String getRecordInfo( @Nonnull ItemStack recordStack )
    {
        Item item = recordStack.getItem();
        if( !(item instanceof RecordItem) ) return null;

        return ((RecordItem) item).getDescription().getText();
    }
}
