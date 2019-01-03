/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.blocks;

import dan200.computercraft.shared.computer.blocks.BlockComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.Parameters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockTurtle extends BlockComputerBase<TileTurtle>
{
    public static final DirectionProperty FACING = Properties.FACING_HORIZONTAL;

    private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cube(
        0.125, 0.125, 0.125,
        0.875, 0.875, 0.875
    );

    public BlockTurtle( Settings settings, ComputerFamily family, BlockEntityType<TileTurtle> type )
    {
        super( settings, family, type );
        setDefaultState( getStateFactory().getDefaultState()
            .with( FACING, Direction.NORTH )
        );
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType( BlockState state )
    {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected void appendProperties( StateFactory.Builder<Block, BlockState> builder )
    {
        builder.with( FACING );
    }

    @Override
    @Deprecated
    public VoxelShape getBoundingShape( BlockState state, BlockView world, BlockPos pos )
    {
        BlockEntity tile = world.getBlockEntity( pos );
        Vec3d offset = tile instanceof TileTurtle ? ((TileTurtle) tile).getRenderOffset( 1.0f ) : Vec3d.ZERO;
        return offset.equals( Vec3d.ZERO ) ? DEFAULT_SHAPE : DEFAULT_SHAPE.method_1096( offset.x, offset.y, offset.z );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerHorizontalFacing() );
    }

    @Override
    public void onPlaced( World world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack itemStack )
    {
        super.onPlaced( world, pos, state, player, itemStack );

        BlockEntity tile = world.getBlockEntity( pos );
        if( !world.isClient && tile instanceof TileTurtle && player instanceof PlayerEntity )
        {
            ((TileTurtle) tile).setOwningPlayer( ((PlayerEntity) player).getGameProfile() );
        }
    }

    /*
    @Override
    @Deprecated
    public float getExplosionResistance( Entity exploder )
    {
        if( getFamily() == ComputerFamily.Advanced && (exploder instanceof LivingEntity || exploder instanceof FireballEntity) )
        {
            return 2000;
        }

        return super.getExplosionResistance( exploder );
    }
    */
    @Override
    @Deprecated
    public List<ItemStack> getDroppedStacks( BlockState block, LootContext.Builder lootBuilder )
    {
        BlockEntity entity = lootBuilder.getNullable( Parameters.BLOCK_ENTITY );
        if( entity instanceof TileTurtle )
        {
            TileTurtle turtle = (TileTurtle) entity;
            lootBuilder.putDrop( COMPUTER_DROP, ( lootContext, consumer ) ->
                consumer.accept( TurtleItemFactory.create( turtle ) ) );
            InventoryUtil.dropContents( turtle, lootBuilder );
        }
        return super.getDroppedStacks( block, lootBuilder );
    }

    @Override
    @Nonnull
    public ItemStack getPickStack( @Nonnull BlockView world, @Nonnull BlockPos pos, @Nonnull BlockState state )
    {
        BlockEntity tile = world.getBlockEntity( pos );
        return tile instanceof TileTurtle ? TurtleItemFactory.create( (TileTurtle) tile ) : super.getPickStack( world, pos, state );
    }
}
