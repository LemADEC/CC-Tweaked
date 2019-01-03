/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import dan200.computercraft.shared.peripheral.printer.ContainerPrinter;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

public class GuiPrinter extends ContainerGui
{
    private static final Identifier BACKGROUND = new Identifier( "computercraft", "textures/gui/printer.png" );

    private final ContainerPrinter m_container;

    public GuiPrinter( ContainerPrinter container )
    {
        super( container );
        m_container = container;
    }

    @Override
    protected void drawForeground( int par1, int par2 )
    {
        String title = m_container.getPrinter().getDisplayName().getString();
        fontRenderer.draw( title, (containerWidth - fontRenderer.getStringWidth( title )) / 2.0f, 6, 0x404040 );
        fontRenderer.draw( I18n.translate( "container.inventory" ), 8, (containerHeight - 96) + 2, 0x404040 );
    }

    @Override
    protected void drawBackground( float f, int i, int j )
    {
        GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
        client.getTextureManager().bindTexture( BACKGROUND );
        int startX = (width - containerWidth) / 2;
        int startY = (height - containerHeight) / 2;
        drawTexturedRect( startX, startY, 0, 0, containerWidth, containerHeight );

        boolean printing = m_container.isPrinting();
        if( printing ) drawTexturedRect( startX + 34, startY + 21, 176, 0, 25, 45 );
    }

    @Override
    public void draw( int mouseX, int mouseY, float partialTicks )
    {
        drawBackground();
        super.draw( mouseX, mouseY, partialTicks );
        drawMousoverTooltip( mouseX, mouseY );
    }
}
