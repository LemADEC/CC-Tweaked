/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.Holiday;
import dan200.computercraft.shared.util.HolidayUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.HitResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class TileEntityTurtleRenderer extends BlockEntityRenderer<TileTurtle>
{
    private static final ModelIdentifier NORMAL_TURTLE_MODEL = new ModelIdentifier( "computercraft:turtle_normal", "inventory" );
    private static final ModelIdentifier ADVANCED_TURTLE_MODEL = new ModelIdentifier( "computercraft:turtle_advanced", "inventory" );
    private static final ModelIdentifier COLOUR_TURTLE_MODEL = new ModelIdentifier( "computercraft:turtle_colour", "inventory" );
    private static final ModelIdentifier ELF_OVERLAY_MODEL = new ModelIdentifier( "computercraft:turtle_elf_overlay", "inventory" );

    @Override
    public void render( TileTurtle tileEntity, double posX, double posY, double posZ, float f, int i )
    {
        if( tileEntity != null )
        {
            // Render the turtle
            renderTurtleAt( tileEntity, posX, posY, posZ, f, i );
        }
    }

    public static ModelIdentifier getTurtleModel( ComputerFamily family, boolean coloured )
    {
        switch( family )
        {
            case Normal:
            default:
                return coloured ? COLOUR_TURTLE_MODEL : NORMAL_TURTLE_MODEL;
            case Advanced:
                return coloured ? COLOUR_TURTLE_MODEL : ADVANCED_TURTLE_MODEL;
        }
    }

    public static ModelIdentifier getTurtleOverlayModel( Identifier overlay, boolean christmas )
    {
        if( overlay != null )
        {
            return new ModelIdentifier( overlay, "inventory" );
        }
        else if( christmas )
        {
            return ELF_OVERLAY_MODEL;
        }
        else
        {
            return null;
        }
    }

    private void renderTurtleAt( TileTurtle turtle, double posX, double posY, double posZ, float f, int i )
    {
        BlockState state = turtle.getWorld().getBlockState( turtle.getPos() );
        GlStateManager.pushMatrix();
        try
        {
            // Setup the transform
            Vec3d offset;
            float yaw;
            offset = turtle.getRenderOffset( f );
            yaw = turtle.getRenderYaw( f );
            GlStateManager.translated( posX + offset.x, posY + offset.y, posZ + offset.z );

            // Render the label
            String label = turtle.createProxy().getLabel();
            if( label != null )
            {
                renderLabel( turtle.getAccess().getPosition(), label );
            }

            // Render the turtle
            GlStateManager.translatef( 0.5f, 0.5f, 0.5f );
            GlStateManager.rotatef( 180.0f - yaw, 0.0f, 1.0f, 0.0f );
            if( label != null && (label.equals( "Dinnerbone" ) || label.equals( "Grumm" )) )
            {
                // Flip the model and swap the cull face as winding order will have changed.
                GlStateManager.scalef( 1.0f, -1.0f, 1.0f );
                GlStateManager.cullFace( GlStateManager.FaceSides.FRONT );
            }
            GlStateManager.translatef( -0.5f, -0.5f, -0.5f );
            // Render the turtle
            int colour;
            ComputerFamily family;
            Identifier overlay;
            colour = turtle.getColour();
            family = turtle.getFamily();
            overlay = turtle.getOverlay();

            renderModel( state, getTurtleModel( family, colour != -1 ), colour == -1 ? null : new int[] { colour } );

            // Render the overlay
            ModelIdentifier overlayModel = getTurtleOverlayModel(
                overlay,
                HolidayUtil.getCurrentHoliday() == Holiday.Christmas
            );
            if( overlayModel != null )
            {
                GlStateManager.disableCull();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
                try
                {
                    renderModel( state, overlayModel, null );
                }
                finally
                {
                    GlStateManager.disableBlend();
                    GlStateManager.enableCull();
                }
            }

            // Render the upgrades
            renderUpgrade( state, turtle, TurtleSide.Left, f );
            renderUpgrade( state, turtle, TurtleSide.Right, f );
        }
        finally
        {
            GlStateManager.popMatrix();
            GlStateManager.cullFace( GlStateManager.FaceSides.BACK );
        }
    }

    private void renderUpgrade( BlockState state, TileTurtle turtle, TurtleSide side, float f )
    {
        ITurtleUpgrade upgrade = turtle.getUpgrade( side );
        if( upgrade != null )
        {
            GlStateManager.pushMatrix();
            try
            {
                float toolAngle = turtle.getToolRenderAngle( side, f );
                GlStateManager.translatef( 0.0f, 0.5f, 0.5f );
                GlStateManager.rotatef( -toolAngle, 1.0f, 0.0f, 0.0f );
                GlStateManager.translatef( 0.0f, -0.5f, -0.5f );

                Pair<BakedModel, Matrix4f> pair = upgrade.getModel( turtle.getAccess(), side );
                if( pair != null )
                {
                    if( pair.getRight() != null )
                    {
                        GlStateManager.multMatrix( pair.getRight() );
                    }
                    if( pair.getLeft() != null )
                    {
                        renderModel( state, pair.getLeft(), null );
                    }
                }
            }
            finally
            {
                GlStateManager.popMatrix();
            }
        }
    }

    private void renderModel( BlockState state, ModelIdentifier modelLocation, int[] tints )
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        BakedModelManager modelManager = mc.getItemRenderer().getModelMap().getModelManager();
        renderModel( state, modelManager.getModel( modelLocation ), tints );
    }

    private void renderModel( BlockState state, BakedModel model, int[] tints )
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        Random random = new Random( 0 );
        Tessellator tessellator = Tessellator.getInstance();
        mc.getTextureManager().bindTexture( SpriteAtlasTexture.BLOCK_ATLAS_TEX );
        renderQuads( tessellator, model.getQuads( state, null, random ), tints );
        for( Direction facing : DirectionUtil.FACINGS )
        {
            renderQuads( tessellator, model.getQuads( state, facing, random ), tints );
        }
    }

    private void renderQuads( Tessellator tessellator, List<BakedQuad> quads, int[] tints )
    {
        BufferBuilder buffer = tessellator.getBufferBuilder();
        VertexFormat format = VertexFormats.POSITION_COLOR_UV_NORMAL;
        buffer.begin( GL11.GL_QUADS, format );
        for( BakedQuad quad : quads )
        {
            /*
            VertexFormat quadFormat = quad.getFormat();
            if( quadFormat != format )
            {
                tessellator.draw();
                format = quadFormat;
                buffer.begin( GL11.GL_QUADS, format );
            }
            */

            int colour = 0xFFFFFFFF;
            if( quad.hasColor() && tints != null )
            {
                int index = quad.getColorIndex();
                if( index >= 0 && index < tints.length ) colour = tints[index] | 0xFF000000;
            }

            // LightUtil.renderQuadColor( buffer, quad, colour );
            buffer.putVertexData( quad.getVertexData() );
            buffer.setQuadColor( colour );
            Vec3i normal = quad.getFace().getVector();
            buffer.postNormal( (float) normal.getX(), (float) normal.getY(), (float) normal.getZ() );
        }
        tessellator.draw();
    }

    private void renderLabel( BlockPos position, String label )
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        HitResult mop = mc.hitResult;
        if( mop != null && mop.type == HitResult.Type.BLOCK && mop.getBlockPos().equals( position ) )
        {
            FontRenderer fontrenderer = renderManager.getFontRenderer();
            float scale = 0.016666668F * 1.6f;

            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
            try
            {
                GlStateManager.translatef( 0.5f, 1.25f, 0.5f );
                GlStateManager.rotatef( -renderManager.cameraYaw, 0.0F, 1.0F, 0.0F );
                GlStateManager.rotatef( renderManager.cameraPitch, 1.0F, 0.0F, 0.0F );// TODO: Right way round?
                GlStateManager.scalef( -scale, -scale, scale );

                int yOffset = 0;
                int xOffset = fontrenderer.getStringWidth( label ) / 2;

                // Draw background
                GlStateManager.depthMask( false );
                GlStateManager.disableDepthTest();
                try
                {
                    // Quad
                    GlStateManager.disableTexture();
                    try
                    {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder renderer = tessellator.getBufferBuilder();
                        renderer.begin( GL11.GL_QUADS, VertexFormats.POSITION_COLOR );
                        renderer.vertex( -xOffset - 1, -1 + yOffset, 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).next();
                        renderer.vertex( -xOffset - 1, 8 + yOffset, 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).next();
                        renderer.vertex( xOffset + 1, 8 + yOffset, 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).next();
                        renderer.vertex( xOffset + 1, -1 + yOffset, 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).next();
                        tessellator.draw();
                    }
                    finally
                    {
                        GlStateManager.enableTexture();
                    }

                    // Text
                    fontrenderer.draw( label, -fontrenderer.getStringWidth( label ) / 2.0f, yOffset, 0x20ffffff );
                }
                finally
                {
                    GlStateManager.enableDepthTest();
                    GlStateManager.depthMask( true );
                }

                // Draw foreground text
                fontrenderer.draw( label, -fontrenderer.getStringWidth( label ) / 2.0f, yOffset, -1 );
            }
            finally
            {
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }
}
