/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.event.ClickEvent;
import net.minecraft.text.event.HoverEvent;

import static net.minecraft.server.command.ServerCommandManager.argument;
import static net.minecraft.server.command.ServerCommandManager.literal;

public class CommandCopy
{
    public static void register( CommandDispatcher<ServerCommandSource> registry )
    {
        registry.register( literal( "computercraft" )
            .then( literal( "copy" ) )
            .then( argument( "message", StringArgumentType.greedyString() ) )
            .executes( context -> {
                MinecraftClient.getInstance().keyboard.setClipboard( context.getArgument( "message", String.class ) );
                return 1;
            } )
        );
    }

    private CommandCopy()
    {
    }

    public static TextComponent createCopyText( String text )
    {
        StringTextComponent name = new StringTextComponent( text );
        name.getStyle()
            .setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/computercraft copy " + text ) )
            .setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new TranslatableTextComponent( "gui.computercraft.tooltip.copy" ) ) );
        return name;
    }
}
