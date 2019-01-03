/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.recipe;

import com.google.gson.JsonObject;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.util.NetworkUtil;
import dan200.computercraft.shared.util.RecipeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

public abstract class ComputerFamilyRecipe extends ComputerConvertRecipe
{
    private final ComputerFamily family;

    public ComputerFamilyRecipe( Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result, ComputerFamily family )
    {
        super( identifier, group, width, height, ingredients, result );
        this.family = family;
    }

    public ComputerFamily getFamily()
    {
        return family;
    }

    public static abstract class Serializer<T extends ComputerFamilyRecipe> implements RecipeSerializer<T>
    {
        protected abstract T create( Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result, ComputerFamily family );

        @Override
        public T read( Identifier identifier, JsonObject json )
        {
            String group = JsonHelper.getString( json, "group", "" );
            ComputerFamily family = RecipeUtil.getFamily( json, "family" );

            RecipeUtil.ShapedTemplate template = RecipeUtil.getTemplate( json );
            ItemStack result = RecipeUtil.getItem( JsonHelper.getObject( json, "result" ) );

            return create( identifier, group, template.width, template.height, template.ingredients, result, family );
        }

        @Override
        public T read( Identifier identifier, PacketByteBuf buf )
        {
            int width = buf.readVarInt();
            int height = buf.readVarInt();
            String group = buf.readString( Short.MAX_VALUE );

            DefaultedList<Ingredient> ingredients = DefaultedList.create( width * height, Ingredient.EMPTY );
            for( int i = 0; i < ingredients.size(); i++ ) ingredients.set( i, Ingredient.fromPacket( buf ) );

            ItemStack result = buf.readItemStack();
            ComputerFamily family = NetworkUtil.readFamily( buf );
            return create( identifier, group, width, height, ingredients, result, family );
        }

        @Override
        public void write( PacketByteBuf buf, T recipe )
        {
            buf.writeVarInt( recipe.getWidth() );
            buf.writeVarInt( recipe.getHeight() );
            buf.writeString( recipe.getGroup() );
            for( Ingredient ingredient : recipe.getPreviewInputs() ) ingredient.write( buf );
            buf.writeItemStack( recipe.getOutput() );
            NetworkUtil.writeFamily( buf, recipe.getFamily() );
        }
    }
}
