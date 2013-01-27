package xk.xact;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
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
import xk.xact.core.*;
import xk.xact.gui.CreativeTabXACT;
import xk.xact.network.CommonProxy;
import xk.xact.network.PacketHandler;

/**
 * XACT adds an electronic crafting table capable of reading recipes encoded into chips.
 */
@Mod(modid = "xact", name = "XACT Mod", version = "beta-0.2.6")
@NetworkMod(clientSideRequired = true, serverSideRequired = true,
		channels = {"xact_channel"}, packetHandler = PacketHandler.class)
public class XActMod {


	@Mod.Instance("xact")
	public static XActMod instance;

	@SidedProxy(clientSide = "xk.xact.network.ClientProxy", serverSide = "xk.xact.network.CommonProxy")
	public static CommonProxy proxy;


	// Fields
	public static final String TEXTURE_BLOCKS = "/gfx/xact/machines.png";
	public static final String TEXTURE_ITEMS = "/gfx/xact/items.png";

	public static int machineID;
	public static int blankChipID;
	public static int encodedChipID;
	public static int caseID;
	public static int padID;

	// Items
	public static Item itemRecipeBlank;
	public static Item itemRecipeEncoded;
	public static Item itemChipCase;
	public static Item itemCraftPad;

	// Block
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

		machineID = config.getBlock( "machineID", 3919 ).getInt();
		blankChipID = config.getItem( "blankChip", 9100 ).getInt();
		encodedChipID = config.getItem( "encodedChip", 9101 ).getInt();
		caseID = config.getItem( "chipCase", 9102 ).getInt();
		padID = config.getItem( "craftPad", 9103 ).getInt();

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

		// Init Blocks
		blockMachine = new BlockMachine( machineID );
		if( REPLACE_WORKBENCH )
			blockWorkbench = BlockVanillaWorkbench.createNew();

		// Register side-sensitive Stuff
		proxy.registerRenderInformation();
		proxy.registerKeyBindings();

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

		// machine's names
		LanguageRegistry.addName( new ItemStack( blockMachine, 1, 0 ), "XACT Crafter" );

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

	private void addRecipes() {
		ItemStack[] ingredients;

		// Recipe Chip
		GameRegistry.addRecipe( new ItemStack( itemRecipeBlank, 16 ),
				new String[] {"ii", "ir", "gg"},
				'i', Item.ingotIron,
				'r', Item.redstone,
				'g', Item.goldNugget
		);

		// Chip Case
		ItemStack chip = new ItemStack( itemRecipeBlank );
		GameRegistry.addRecipe( new ShapedOreRecipe( itemChipCase,
				new String[] {"cgc", "c c", "wCw"},
				'c', chip,
				'g', Block.thinGlass,
				'w', "plankWood",
				'C', Block.chest
		) );

		// Craft Pad
		ingredients = ingredients(
				Item.ingotIron, Item.ingotIron, null,
				Item.ingotIron, Block.workbench, chip,
				null, null, null
		);
		GameRegistry.addRecipe( new ShapedRecipes( 3, 2, ingredients, new ItemStack( itemCraftPad ) ) );

		// Crafter
		ingredients = ingredients(
				itemRecipeBlank, Block.glass, itemRecipeBlank,
				itemRecipeBlank, Block.workbench, itemRecipeBlank,
				Item.ingotIron, Block.chest, Item.ingotIron
		);
		GameRegistry.addRecipe( new ShapedRecipes( 3, 3, ingredients, new ItemStack( blockMachine, 1, 0 ) ) );
	}

	private ItemStack[] ingredients(Object... objects) {
		ItemStack[] retValue = new ItemStack[objects.length];
		int index = -1;
		for( Object o : objects ) {
			index++;
			if( index >= 9 )
				break;
			if( o == null ) {
				retValue[index] = null;
				continue;
			}
			if( o instanceof Item ) {
				retValue[index] = new ItemStack( (Item) o );
				continue;
			}
			if( o instanceof Block ) {
				retValue[index] = new ItemStack( (Block) o );
			}
			if( o instanceof ItemStack )
				retValue[index] = (ItemStack) o;
		}

		return retValue;
	}

}
