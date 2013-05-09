package xk.xact;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;
import xk.xact.core.Machines;
import xk.xact.core.blocks.BlockMachine;
import xk.xact.core.blocks.BlockVanillaWorkbench;
import xk.xact.core.items.*;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.core.tileentities.TileWorkbench;
import xk.xact.gui.CreativeTabXACT;
import xk.xact.network.CommonProxy;
import xk.xact.network.PacketHandler;
import xk.xact.plugin.PluginManager;
import xk.xact.recipes.RecipeUtils;

import java.util.logging.Logger;

/**
 * XACT adds an electronic crafting table capable of reading recipes encoded into chips.
 */
@Mod(modid = "xact", name = "XACT Mod", useMetadata = true)
@NetworkMod(clientSideRequired = true, serverSideRequired = true,
		channels = { "xact_channel" }, packetHandler = PacketHandler.class)
public class XActMod {


	@Mod.Instance("xact")
	public static XActMod instance;

	@SidedProxy(clientSide = "xk.xact.network.ClientProxy", serverSide = "xk.xact.network.CommonProxy")
	public static CommonProxy proxy;

	public static Logger logger;

	// IDs
	public static int machineID;
	public static int blankChipID;
	public static int encodedChipID;
	public static int caseID;
	public static int padID;
	public static int blankBlueprintID;
	public static int blueprintID;

	// Items
	public static Item itemRecipeBlank;
	public static Item itemRecipeEncoded;
	public static Item itemChipCase;
	public static Item itemCraftPad;
	public static Item itemBlueprintBlank;
	public static Item itemBlueprint;

	// Blocks
	public static Block blockMachine;
	public static Block blockWorkbench;

	public static CreativeTabXACT xactTab;

	// debugging information.
	public static boolean DEBUG_MODE = false;

	public static boolean REPLACE_WORKBENCH;

	@Mod.PreInit
	@SuppressWarnings("unused")
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();

		// Initialize the logger.
		logger = Logger.getLogger( "XACT-" + FMLCommonHandler.instance().getEffectiveSide() );
		logger.setParent( FMLLog.getLogger() );

		machineID = config.getBlock( "machineID", 3919 ).getInt();
		blankChipID = config.getItem( "blankChip", 9100 ).getInt();
		encodedChipID = config.getItem( "encodedChip", 9101 ).getInt();
		caseID = config.getItem( "chipCase", 9102 ).getInt();
		padID = config.getItem( "craftPad", 9103 ).getInt();
		blankBlueprintID = config.getItem( "blankBlueprint", 9104 ).getInt();
		blueprintID = config.getItem( "blueprint", 9105 ).getInt();

		REPLACE_WORKBENCH = config.get( "Miscellaneous", "addWorkbenchTileEntity", true,
				"If true, XACT will make the vanilla workbench able to keep it's contents on the grid after the GUI is closed. \n" +
						"Make sure you clear the workbench's grid before setting this to false, or you will lose your items." )
				.getBoolean( true );

		config.save();
	}

	@Mod.Init
	@SuppressWarnings("unused")
	public void initializeAll(FMLInitializationEvent ignoredEvent) {

		xactTab = new CreativeTabXACT();

		// Init Items
		itemRecipeBlank = new ItemChip( blankChipID, false );
		itemRecipeEncoded = new ItemChip( encodedChipID, true );
		itemChipCase = new ItemCase( caseID );
		itemCraftPad = new ItemPad( padID );
		itemBlueprintBlank = new ItemBlankBlueprint( blankBlueprintID );
		itemBlueprint = new ItemBlueprint( blueprintID );

		// Init Blocks
		blockMachine = new BlockMachine( machineID );
		if( REPLACE_WORKBENCH )
			blockWorkbench = BlockVanillaWorkbench.createNew();

		// Register side-sensitive Stuff
		proxy.registerRenderInformation();
		proxy.registerHandlers();

		// Register Blocks
		GameRegistry.registerBlock( blockMachine, ItemMachine.class, "XACT Mod" );

		// Register TileEntities
		GameRegistry.registerTileEntity( TileCrafter.class, "tile.xact.Crafter" );
		GameRegistry.registerTileEntity( TileWorkbench.class, "tile.xact.VanillaWorkbench" );

		// Add names
		LanguageRegistry.addName( itemRecipeBlank, "Recipe Chip" );
		LanguageRegistry.addName( itemRecipeEncoded, "\u00a72" + "Recipe Chip" );
		LanguageRegistry.addName( itemChipCase, "Chip Case" );
		LanguageRegistry.addName( itemCraftPad, "Craft Pad" );
		LanguageRegistry.addName( itemBlueprintBlank, "Blank Blueprint" );
		LanguageRegistry.addName( itemBlueprint, "\u00a76" + "Blueprint" );

		// machine's names
		for( Machines machine : Machines.values() ) {
			LanguageRegistry.addName( new ItemStack( blockMachine, 1, machine.ordinal() ), machine.getLocalizedName() );
		}

		// tab's name
		LanguageRegistry.instance().addStringLocalization( "itemGroup.xact", "XACT" );

		// keybinding names
		LanguageRegistry.instance().addStringLocalization( "xact.clear", "XACT: Clear Crafting Grid" );
		LanguageRegistry.instance().addStringLocalization( "xact.load", "XACT: Load recipe from chip" );
		LanguageRegistry.instance().addStringLocalization( "xact.prev", "XACT: Get Previous Recipe" );
		LanguageRegistry.instance().addStringLocalization( "xact.next", "XACT: Get Next Recipe" );
		LanguageRegistry.instance().addStringLocalization( "xact.delete", "XACT: Clear Recipe List" );
		LanguageRegistry.instance().addStringLocalization( "xact.reveal", "XACT: Hold to Reveal Chip's Recipe" );

		// Register GUIs
		NetworkRegistry.instance().registerGuiHandler( XActMod.instance, proxy );

		// Add the recipes
		addRecipes();
	}

	@Mod.PostInit
	@SuppressWarnings("unused")
	public void postInit(FMLPostInitializationEvent event) {
		PluginManager.checkEverything();
		PluginManager.initializePlugins();
	}

	private void addRecipes() {
		ItemStack[] ingredients;

		// todo: add blueprint recipe.

		// Recipe Chip
		GameRegistry.addRecipe( new ItemStack( itemRecipeBlank, 16 ),
				new String[] { "ii", "ir", "gg" },
				'i', Item.ingotIron,
				'r', Item.redstone,
				'g', Item.goldNugget
		);

		// Chip Case
		ItemStack chip = new ItemStack( itemRecipeBlank );
		GameRegistry.addRecipe( new ShapedOreRecipe( itemChipCase,
				new String[] { "cgc", "c c", "wCw" },
				'c', chip,
				'g', Block.thinGlass,
				'w', "plankWood",
				'C', Block.chest
		) );

		// Craft Pad
		ingredients = RecipeUtils.ingredients(
				Item.ingotIron, Item.ingotIron, null,
				Item.ingotIron, Block.workbench, chip,
				null, null, null
		);
		GameRegistry.addRecipe( new ShapedRecipes( 3, 2, ingredients, new ItemStack( itemCraftPad ) ) );

		// Machines
		for( Machines machine : Machines.values() ) {
			GameRegistry.addRecipe( machine.getMachineRecipe() );
		}

	}

}
