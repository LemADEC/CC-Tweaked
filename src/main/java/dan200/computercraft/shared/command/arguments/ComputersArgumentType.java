/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.command.arguments;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.command.arguments.serialize.ArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.PacketByteBuf;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static dan200.computercraft.shared.command.CommandUtils.suggest;
import static dan200.computercraft.shared.command.CommandUtils.suggestOnServer;
import static dan200.computercraft.shared.command.Exceptions.COMPUTER_SELECTOR_NONE;

public class ComputersArgumentType implements ArgumentType<ComputersArgumentType.ComputersSupplier>
{
    private static final ComputersArgumentType MANY = new ComputersArgumentType( false );
    private static final ComputersArgumentType SOME = new ComputersArgumentType( true );

    private static final List<String> EXAMPLES = Arrays.asList(
        "0", "#0", "@Label", "~Advanced"
    );

    public static ComputersArgumentType manyComputers()
    {
        return MANY;
    }

    public static ComputersArgumentType someComputers()
    {
        return SOME;
    }

    public static Collection<ServerComputer> getComputersArgument( CommandContext<ServerCommandSource> context, String name ) throws CommandSyntaxException
    {
        return context.getArgument( name, ComputersSupplier.class ).unwrap( context.getSource() );
    }

    private final boolean requireSome;

    private ComputersArgumentType( boolean requireSome )
    {
        this.requireSome = requireSome;
    }

    @Override
    public ComputersSupplier parse( StringReader reader ) throws CommandSyntaxException
    {
        int start = reader.getCursor();
        char kind = reader.peek();
        ComputersSupplier computers;
        if( kind == '@' )
        {
            reader.skip();
            String label = reader.readUnquotedString();
            computers = getComputers( x -> Objects.equals( label, x.getLabel() ) );
        }
        else if( kind == '~' )
        {
            reader.skip();
            String family = reader.readUnquotedString();
            computers = getComputers( x -> x.getFamily().name().equalsIgnoreCase( family ) );
        }
        else if( kind == '#' )
        {
            reader.skip();
            int id = reader.readInt();
            computers = getComputers( x -> x.getId() == id );
        }
        else
        {
            int instance = reader.readInt();
            computers = s -> {
                ServerComputer computer = ComputerCraft.serverComputerRegistry.get( instance );
                return computer == null ? Collections.emptyList() : Collections.singletonList( computer );
            };
        }

        if( requireSome )
        {
            String selector = reader.getString().substring( start, reader.getCursor() );
            return source -> {
                Collection<ServerComputer> matched = computers.unwrap( source );
                if( matched.isEmpty() ) throw COMPUTER_SELECTOR_NONE.create( selector );
                return matched;
            };
        }
        else
        {
            return computers;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions( CommandContext<S> context, SuggestionsBuilder builder )
    {
        String remaining = builder.getRemaining();

        // We can run this one on the client, for obvious reasons.
        if( remaining.startsWith( "~" ) )
        {
            return suggest( builder, ComputerFamily.values(), x -> "~" + x.name() );
        }

        // Verify we've a command source and we're running on the server
        return suggestOnServer( context, builder, s -> {
            if( remaining.startsWith( "@" ) )
            {
                suggestComputers( builder, remaining, x -> {
                    String label = x.getLabel();
                    return label == null ? null : "@" + label;
                } );
            }
            else if( remaining.startsWith( "#" ) )
            {
                suggestComputers( builder, remaining, c -> "#" + c.getId() );
            }
            else
            {
                suggestComputers( builder, remaining, c -> Integer.toString( c.getInstanceID() ) );
            }

            return builder.buildFuture();
        } );
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    private static void suggestComputers( SuggestionsBuilder builder, String remaining, Function<ServerComputer, String> renderer )
    {
        remaining = remaining.toLowerCase( Locale.ROOT );
        // TODO: DO we need to copy?
        List<ServerComputer> computers = Lists.newArrayList( ComputerCraft.serverComputerRegistry.getComputers() );
        for( ServerComputer computer : computers )
        {
            String converted = renderer.apply( computer );
            if( converted != null && converted.toLowerCase( Locale.ROOT ).startsWith( remaining ) )
            {
                builder.suggest( converted );
            }
        }
    }

    private static ComputersSupplier getComputers( Predicate<ServerComputer> predicate )
    {
        return s -> {
            // TODO: DO we need to copy?
            ArrayList<ServerComputer> computers = new ArrayList<>( ComputerCraft.serverComputerRegistry.getComputers() );
            computers.removeIf( predicate.negate() );
            return Collections.unmodifiableList( computers );
        };
    }

    public static class Serializer implements ArgumentSerializer<ComputersArgumentType>
    {

        @Override
        public void toPacket( ComputersArgumentType arg, PacketByteBuf buf )
        {
            buf.writeBoolean( arg.requireSome );
        }

        @Override
        public ComputersArgumentType fromPacket( PacketByteBuf buf )
        {
            return buf.readBoolean() ? SOME : MANY;
        }

        @Override
        public void toJson( ComputersArgumentType arg, JsonObject json )
        {
            json.addProperty( "requireSome", arg.requireSome );
        }
    }

    @FunctionalInterface
    public interface ComputersSupplier
    {
        Collection<ServerComputer> unwrap( ServerCommandSource source ) throws CommandSyntaxException;
    }

    public static Set<ServerComputer> unwrap( ServerCommandSource source, Collection<ComputersSupplier> suppliers ) throws CommandSyntaxException
    {
        Set<ServerComputer> computers = new HashSet<>();
        for( ComputersSupplier supplier : suppliers ) computers.addAll( supplier.unwrap( source ) );
        return computers;
    }
}
