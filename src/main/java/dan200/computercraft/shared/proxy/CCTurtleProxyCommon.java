/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.TurtleUpgrades;
import dan200.computercraft.shared.turtle.recipes.TurtleRecipe;
import dan200.computercraft.shared.turtle.recipes.TurtleUpgradeRecipe;
import dan200.computercraft.shared.turtle.upgrades.*;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializers;
import net.minecraft.util.Identifier;

public abstract class CCTurtleProxyCommon implements ICCTurtleProxy
{

    @Override
    public void preInit()
    {
        /*
        MinecraftForge.EVENT_BUS.register( this );

        EntityRegistry.registerModEntity(
            new Identifier( ComputerCraft.MOD_ID, "turtle_player" ), TurtlePlayer.class, "turtle_player",
            0, ComputerCraft.instance, Integer.MAX_VALUE, Integer.MAX_VALUE, false
        );
        */

        registerUpgrades();

        // Recipe types
        RecipeSerializers.register( TurtleRecipe.SERIALIZER );
        RecipeSerializers.register( TurtleUpgradeRecipe.SERIALIZER );
    }

    @Override
    public void init()
    {
        // registerForgeHandlers();
        // registerTileEntities();
    }

    /*
    @SubscribeEvent
    public void registerRecipes( RegistryEvent.Register<IRecipe> event )
    {
        IForgeRegistry<IRecipe> registry = event.getRegistry();

        // Add a bunch of impostor recipes
        // TODO: Figure out a way to do this in a "nice" way.
        for( ITurtleUpgrade upgrade : TurtleUpgrades.getVanillaUpgrades() )
        {
            // Add fake recipes to fool NEI
            ItemStack craftingItem = upgrade.getCraftingItem();

            // A turtle just containing this upgrade
            for( ComputerFamily family : ComputerFamily.values() )
            {
                if( !TurtleUpgrades.suitableForFamily( family, upgrade ) ) continue;

                ItemStack baseTurtle = TurtleItemFactory.create( -1, null, -1, family, null, null, 0, null );
                if( !baseTurtle.isEmpty() )
                {
                    ItemStack craftedTurtle = TurtleItemFactory.create( -1, null, -1, family, upgrade, null, 0, null );
                    ItemStack craftedTurtleFlipped = TurtleItemFactory.create( -1, null, -1, family, null, upgrade, 0, null );
                    registry.register(
                        new ImpostorRecipe( "computercraft:" + family.toString() + "_turtle_upgrade", 2, 1, new ItemStack[] { baseTurtle, craftingItem }, craftedTurtle )
                            .setRegistryName( new Identifier( "computercraft:" + family + "_turtle_upgrade_" + upgrade.getUpgradeID().toString().replace( ':', '_' ) + "_1" ) )
                    );
                    registry.register(
                        new ImpostorRecipe( "computercraft:" + family.toString() + "_turtle_upgrade", 2, 1, new ItemStack[] { craftingItem, baseTurtle }, craftedTurtleFlipped )
                            .setRegistryName( new Identifier( "computercraft:" + family + "_turtle_upgrade_" + upgrade.getUpgradeID().toString().replace( ':', '_' ) + "_2" ) )
                    );

                    /*
                    // A turtle containing this upgrade and another upgrade
                    for( ITurtleUpgrade otherUpgrade : m_turtleUpgrades.values() )
                    {
                        if( isUpgradeVanilla( otherUpgrade ) && isUpgradeSuitableForFamily( family, otherUpgrade ) )
                        {
                            ItemStack otherCraftingItem = otherUpgrade.getCraftingItem();

                            ItemStack otherCraftedTurtle = TurtleItemFactory.create( -1, null, -1, family, null, otherUpgrade, 0, null );
                            ItemStack comboCraftedTurtle = TurtleItemFactory.create( -1, null, -1, family, upgrade, otherUpgrade, 0, null );

                            ItemStack otherCraftedTurtleFlipped = TurtleItemFactory.create( -1, null, -1, family, otherUpgrade, null, 0, null );
                            ItemStack comboCraftedTurtleFlipped = TurtleItemFactory.create( -1, null, -1, family, otherUpgrade, upgrade, 0, null );

                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { otherCraftingItem, craftedTurtle }, comboCraftedTurtle ) );
                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { otherCraftedTurtle, craftingItem }, comboCraftedTurtle ) );
                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { craftedTurtleFlipped, otherCraftingItem }, comboCraftedTurtleFlipped ) );
                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { craftingItem, otherCraftedTurtleFlipped }, comboCraftedTurtleFlipped ) );
                            recipeList.add( new ImpostorRecipe( 3, 1, new ItemStack[] { otherCraftingItem, baseTurtle, craftingItem,  }, comboCraftedTurtle ) );
                            recipeList.add( new ImpostorRecipe( 3, 1, new ItemStack[] { craftingItem, baseTurtle, otherCraftingItem }, comboCraftedTurtleFlipped ) );
                        }
                    }
                    *//*
                }
            }
        }
    }
    */

    private void registerUpgrades()
    {
        // Upgrades
        ComputerCraft.Upgrades.wirelessModemNormal = new TurtleModem( false, new Identifier( ComputerCraft.MOD_ID, "wireless_modem" ) );
        TurtleUpgrades.register( ComputerCraft.Upgrades.wirelessModemNormal );

        ComputerCraft.Upgrades.wirelessModemAdvanced = new TurtleModem( true, new Identifier( ComputerCraft.MOD_ID, "advanced_modem" ) );
        TurtleUpgrades.register( ComputerCraft.Upgrades.wirelessModemAdvanced );

        ComputerCraft.Upgrades.speaker = new TurtleSpeaker( new Identifier( ComputerCraft.MOD_ID, "speaker" ) );
        TurtleUpgrades.register( ComputerCraft.Upgrades.speaker );

        ComputerCraft.Upgrades.craftingTable = new TurtleCraftingTable();
        TurtleUpgrades.register( ComputerCraft.Upgrades.craftingTable );

        ComputerCraft.Upgrades.diamondSword = new TurtleSword( new Identifier( "minecraft", "diamond_sword" ), "upgrade.minecraft.diamond_sword.adjective", Items.DIAMOND_SWORD );
        TurtleUpgrades.register( ComputerCraft.Upgrades.diamondSword );

        ComputerCraft.Upgrades.diamondShovel = new TurtleShovel( new Identifier( "minecraft", "diamond_shovel" ), "upgrade.minecraft.diamond_shovel.adjective", Items.DIAMOND_SHOVEL );
        TurtleUpgrades.register( ComputerCraft.Upgrades.diamondShovel );

        ComputerCraft.Upgrades.diamondPickaxe = new TurtleTool( new Identifier( "minecraft", "diamond_pickaxe" ), "upgrade.minecraft.diamond_pickaxe.adjective", Items.DIAMOND_PICKAXE );
        TurtleUpgrades.register( ComputerCraft.Upgrades.diamondPickaxe );

        ComputerCraft.Upgrades.diamondAxe = new TurtleAxe( new Identifier( "minecraft", "diamond_axe" ), "upgrade.minecraft.diamond_axe.adjective", Items.DIAMOND_AXE );
        TurtleUpgrades.register( ComputerCraft.Upgrades.diamondAxe );

        ComputerCraft.Upgrades.diamondHoe = new TurtleHoe( new Identifier( "minecraft", "diamond_hoe" ), "upgrade.minecraft.diamond_hoe.adjective", Items.DIAMOND_HOE );
        TurtleUpgrades.register( ComputerCraft.Upgrades.diamondHoe );
    }

    /*
    private void registerTileEntities()
    {
        // TileEntities
        GameRegistry.registerTileEntity( TileTurtle.class, new Identifier( ComputerCraft.MOD_ID, "turtle" ) );
        GameRegistry.registerTileEntity( TileTurtleExpanded.class, new Identifier( ComputerCraft.MOD_ID, "turtleex" ) );
        GameRegistry.registerTileEntity( TileTurtleAdvanced.class, new Identifier( ComputerCraft.MOD_ID, "turtleadv" ) );
    }

    private void registerForgeHandlers()
    {
        MinecraftForge.EVENT_BUS.register( new ForgeHandlers() );
        MinecraftForge.EVENT_BUS.register( DropConsumer.instance() );
    }

    private class ForgeHandlers
    {
        @SubscribeEvent
        public void onTurtleAction( TurtleActionEvent event )
        {
            if( ComputerCraft.turtleDisabledActions.contains( event.getAction() ) )
            {
                event.setCanceled( true, "Action has been disabled" );
            }
        }
    }
    */
}
