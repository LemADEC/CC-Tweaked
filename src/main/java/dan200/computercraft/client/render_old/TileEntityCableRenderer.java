/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.modem.wired.BlockCable;
import dan200.computercraft.shared.peripheral.modem.wired.CableModemVariant;
import dan200.computercraft.shared.peripheral.modem.wired.CableShapes;
import dan200.computercraft.shared.peripheral.modem.wired.TileCable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * Render breaking animation only over part of a {@link TileCable}.
 */
public class TileEntityCableRenderer extends BlockEntityRenderer<TileCable>
{
    @Override
    public void render( @Nonnull TileCable te, double x, double y, double z, float partialTicks, int destroyStage )
    {
        if( destroyStage < 0 ) return;

        BlockPos pos = te.getPos();

        MinecraftClient mc = MinecraftClient.getInstance();

        HitResult hit = mc.hitResult;
        if( hit == null || !hit.getBlockPos().equals( pos ) ) return;

        if( MinecraftForgeClient.getRenderPass() != 0 ) return;

        World world = te.getWorld();
        BlockState state = world.getBlockState( pos );
        Block block = state.getBlock();
        if( block != ComputerCraft.Blocks.cable ) return;

        VoxelShape shape = CableShapes.getModemState( state );
        state = te.hasModem() && shape.getBoundingBox().contains( hit.pos.subtract( pos.getX(), pos.getY(), pos.getZ() ) )
            ? block.getDefaultState().with( BlockCable.MODEM, state.get( BlockCable.MODEM ) )
            : state.with( BlockCable.MODEM, CableModemVariant.None );

        BakedModel model = mc.getBlockRendererDispatcher().getModelForState( state );
        if( model == null ) return;

        preRenderDamagedBlocks();

        VertexBuffer buffer = Tessellator.getInstance().getVertexBuffer();
        buffer.begin( GL11.GL_QUADS, VertexFormats.BLOCK );
        buffer.setTranslation( x - pos.getX(), y - pos.getY(), z - pos.getZ() );
        buffer.noColor();

        ForgeHooksClient.setRenderLayer( block.getRenderLayer() );

        // See BlockRendererDispatcher#renderBlockDamage
        TextureAtlasSprite breakingTexture = mc.getTextureMapBlocks().getAtlasSprite( "minecraft:blocks/destroy_stage_" + destroyStage );
        mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
            world,
            ForgeHooksClient.getDamageModel( model, breakingTexture, state, world, pos ),
            state, pos, buffer, true
        );

        ForgeHooksClient.setRenderLayer( BlockRenderLayer.SOLID );

        buffer.setTranslation( 0, 0, 0 );
        Tessellator.getInstance().draw();

        postRenderDamagedBlocks();
    }

    /**
     * @see RenderGlobal#preRenderDamagedBlocks()
     */
    private void preRenderDamagedBlocks()
    {
        GlStateManager.disableLighting();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
            GlStateManager.SrcBlendFactor.DST_COLOR, GlStateManager.DstBlendFactor.SRC_COLOR,
            GlStateManager.SrcBlendFactor.ONE, GlStateManager.DstBlendFactor.ZERO
        );
        GlStateManager.enableBlend();
        GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 0.5F );
        GlStateManager.polygonOffset( -3.0F, -3.0F );
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc( 516, 0.1F );
        GlStateManager.enableAlphaTest();
        GlStateManager.pushMatrix();
    }

    /**
     * @see RenderGlobal#postRenderDamagedBlocks()
     */
    private void postRenderDamagedBlocks()
    {
        GlStateManager.disableAlphaTest();
        GlStateManager.polygonOffset( 0.0F, 0.0F );
        GlStateManager.disablePolygonOffset();
        GlStateManager.disablePolygonOffset();
        GlStateManager.depthMask( true );
        GlStateManager.popMatrix();
    }
}
