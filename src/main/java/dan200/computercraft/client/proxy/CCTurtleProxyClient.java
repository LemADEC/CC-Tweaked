/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.TileEntityTurtleRenderer;
import dan200.computercraft.client.render.TurtleItemUnbakedModel;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.items.ItemTurtle;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.fabricmc.fabric.client.render.BlockEntityRendererRegistry;
import net.minecraft.client.render.item.ItemColorMapper;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public class CCTurtleProxyClient extends CCTurtleProxyCommon
{
    // IComputerCraftProxy implementation

    @Override
    public void preInit()
    {
        super.preInit();

        // Setup client forge handlers
        // registerForgeHandlers();

        ModelLoadingRegistry.INSTANCE.registerVariantProvider( manager -> ( identifier, context ) -> {
            if( identifier.getNamespace().equals( ComputerCraft.MOD_ID ) && identifier.getVariant().equals( "inventory" ) )
            {
                switch( identifier.getPath() )
                {
                    case "turtle_normal":
                        return new TurtleItemUnbakedModel( ComputerFamily.Normal );
                    case "turtle_advanced":
                        return new TurtleItemUnbakedModel( ComputerFamily.Advanced );
                }
            }

            return null;
        } );

        ModelLoadingRegistry.INSTANCE.registerAppender( ( manager, load ) -> {
            for( String upgrade : EXTRA_MODELS )
            {
                load.accept( new ModelIdentifier( new Identifier( ComputerCraft.MOD_ID, upgrade ), "inventory" ) );
            }
        } );
    }

    @Override
    public void init()
    {
        super.init();

        // Setup turtle colours
        ColorProviderRegistry.ITEM.register(
            new TurtleItemColour(),
            ComputerCraft.Blocks.turtleNormal, ComputerCraft.Blocks.turtleAdvanced
        );

        // Setup renderers
        BlockEntityRendererRegistry.INSTANCE.register( TileTurtle.class, new TileEntityTurtleRenderer() );
    }

    private static class TurtleItemColour implements ItemColorMapper
    {
        @Override
        public int getColor( @Nonnull ItemStack stack, int tintIndex )
        {
            if( tintIndex == 0 )
            {
                ItemTurtle turtle = (ItemTurtle) stack.getItem();
                int colour = turtle.getColour( stack );
                if( colour != -1 ) return colour;
            }

            return 0xFFFFFF;
        }
    }

    private static final String[] EXTRA_MODELS = {
        "turtle_crafting_table_left",
        "turtle_crafting_table_right",

        "turtle_modem_normal_off_left",
        "turtle_modem_normal_on_left",
        "turtle_modem_normal_off_right",
        "turtle_modem_normal_on_right",

        "turtle_modem_advanced_off_left",
        "turtle_modem_advanced_on_left",
        "turtle_modem_advanced_off_right",
        "turtle_modem_advanced_on_right",

        "turtle_speaker_upgrade_left",
        "turtle_speaker_upgrade_right",

        "turtle_elf_overlay",
    };
}
