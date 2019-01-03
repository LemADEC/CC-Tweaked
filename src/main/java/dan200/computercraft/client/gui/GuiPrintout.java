/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.common.ContainerHeldItem;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.client.gui.ContainerGui;
import org.lwjgl.glfw.GLFW;

import static dan200.computercraft.client.render.PrintoutRenderer.*;

public class GuiPrintout extends ContainerGui
{
    private final boolean m_book;
    private final int m_pages;
    private final TextBuffer[] m_text;
    private final TextBuffer[] m_colours;
    private int m_page;

    public GuiPrintout( ContainerHeldItem container )
    {
        super( container );

        String[] text = ItemPrintout.getText( container.getStack() );
        m_text = new TextBuffer[text.length];
        for( int i = 0; i < m_text.length; i++ ) m_text[i] = new TextBuffer( text[i] );

        String[] colours = ItemPrintout.getColours( container.getStack() );
        m_colours = new TextBuffer[colours.length];
        for( int i = 0; i < m_colours.length; i++ ) m_colours[i] = new TextBuffer( colours[i] );

        m_page = 0;
        m_pages = Math.max( m_text.length / ItemPrintout.LINES_PER_PAGE, 1 );
        m_book = ((ItemPrintout) container.getStack().getItem()).getType() == ItemPrintout.Type.BOOK;
    }

    @Override
    public boolean keyPressed( int key, int scancode, int modifiers )
    {
        if( super.keyPressed( key, scancode, modifiers ) ) return true;

        if( key == GLFW.GLFW_KEY_RIGHT )
        {
            if( m_page < m_pages - 1 ) m_page++;
            return true;
        }

        if( key == GLFW.GLFW_KEY_LEFT )
        {
            if( m_page > 0 ) m_page--;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled( double delta )
    {
        if( super.mouseScrolled( delta ) ) return true;
        if( delta < 0 )
        {
            // Scroll up goes to the next page
            if( m_page < m_pages - 1 ) m_page++;
            return true;
        }

        if( delta > 0 )
        {
            // Scroll down goes to the previous page
            if( m_page > 0 ) m_page--;
            return true;
        }

        return false;
    }

    @Override
    public void draw( int mouseX, int mouseY, float v )
    {
        // Draw background
        zOffset = zOffset - 1;
        drawBackground();
        zOffset = zOffset + 1;

        // Draw the printout
        GlStateManager.color4f( 1.0f, 1.0f, 1.0f, 1.0f );

        int startY = (height - Y_SIZE) / 2;
        int startX = (width - X_SIZE) / 2;

        drawBorder( startX, startY, zOffset, m_page, m_pages, m_book );
        drawText( startX + X_TEXT_MARGIN, startY + Y_TEXT_MARGIN, ItemPrintout.LINES_PER_PAGE * m_page, m_text, m_colours );
    }

    @Override
    protected void drawBackground( float v, int i, int i1 )
    {
    }
}
