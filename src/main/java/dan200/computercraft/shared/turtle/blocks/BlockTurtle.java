/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.blocks;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.BlockComputerBase;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BlockTurtle extends BlockComputerBase
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private final ComputerFamily family;
    private final Supplier<TileTurtle> factory;

    public BlockTurtle( ComputerFamily family, Supplier<TileTurtle> factory )
    {
        super( Material.IRON );
        this.family = family;
        this.factory = factory;

        setHardness( 2.5f );
        setCreativeTab( ComputerCraft.mainCreativeTab );
        setDefaultState( this.blockState.getBaseState()
            .withProperty( FACING, EnumFacing.NORTH )
        );
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType( IBlockState state )
    {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube( IBlockState state )
    {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube( IBlockState state )
    {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape( IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side )
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer( this, FACING );
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta( int meta )
    {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState( IBlockState state )
    {
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState( @Nonnull IBlockState state, IBlockAccess world, BlockPos pos )
    {
        return state.withProperty( FACING, getDirection( world, pos ) );
    }

    @Override
    protected IBlockState getDefaultBlockState( EnumFacing placedSide )
    {
        return getDefaultState();
    }

    @Override
    @Deprecated
    public float getExplosionResistance( @Nullable Entity exploder )
    {
        return getFamily() == ComputerFamily.Advanced || exploder instanceof EntityLivingBase || exploder instanceof EntityFireball
            ? 2000 : super.getExplosionResistance( exploder );
    }

    @Nonnull
    @Override
    public ComputerFamily getFamily()
    {
        return family;
    }

    @Override
    protected TileComputerBase createTile()
    {
        return factory.get();
    }


    @Override
    protected void getDroppedItems( IBlockState state, IBlockAccess world, BlockPos pos, @Nonnull NonNullList<ItemStack> drops, boolean creative )
    {
        TileEntity te = world.getTileEntity( pos );
        if( te instanceof TileTurtle )
        {
            TileTurtle turtle = (TileTurtle) te;
            if( !creative || turtle.createProxy().getLabel() != null )
            {
                drops.add( TurtleItemFactory.create( turtle ) );
            }
        }
    }

    @Override
    protected ItemStack getComputerItem( IBlockState state, IBlockAccess world, BlockPos pos )
    {
        TileEntity te = world.getTileEntity( pos );
        return te instanceof TileTurtle ? TurtleItemFactory.create( (TileTurtle) te ) : ItemStack.EMPTY;
    }

    @Override
    public void onBlockPlacedBy( World world, BlockPos pos, IBlockState state, EntityLivingBase player, @Nonnull ItemStack itemstack )
    {
        // Not sure why this is necessary
        TileEntity tile = world.getTileEntity( pos );
        if( tile instanceof TileTurtle )
        {
            tile.setWorld( world ); // Not sure why this is necessary
            tile.setPos( pos ); // Not sure why this is necessary
            if( player instanceof EntityPlayer )
            {
                ((TileTurtle) tile).setOwningPlayer( ((EntityPlayer) player).getGameProfile() );
            }
        }

        // Set direction
        EnumFacing dir = DirectionUtil.fromEntityRot( player );
        setDirection( world, pos, dir.getOpposite() );
    }
}
