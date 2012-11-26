package xk.xact;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.*;
import net.minecraftforge.common.Configuration;
import xk.xact.core.*;
import xk.xact.gui.XactTab;
import xk.xact.network.CommonProxy;
import xk.xact.network.PacketHandler;

import java.util.ArrayList;

/**
 * XACT adds an electronic crafting table capable of reading recipes encoded into chips.
 */
@Mod(modid = "xact", name = "XACT Mod", version = "beta-0.1.7pre1")
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

	// Items
	public static Item itemRecipeBlank;
	public static Item itemRecipeEncoded;

	// Block
	public static Block blockMachine;

    public static XactTab xactTab;


	@Mod.PreInit
	public void preInit(FMLPreInitializationEvent event){
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		machineID = config.getBlock("machineID", 3919).getInt();
		blankChipID = config.getItem("blankChip", 9100).getInt();
		encodedChipID =  config.getItem("encodedChip", 9101).getInt();

		config.save();
	}
	
	@Mod.Init
	public void initializeAll(FMLInitializationEvent ignoredEvent) {
		proxy.registerRenderInformation();

        xactTab = new XactTab();

		// Init Items
		itemRecipeBlank = new ItemChip(blankChipID, false);
		itemRecipeEncoded = new ItemChip(encodedChipID, true);

		// Init Blocks
		blockMachine = new BlockMachine(machineID);

		// Register Blocks
		GameRegistry.registerBlock(blockMachine, ItemMachine.class);

		// Register TileEntities
		GameRegistry.registerTileEntity(TileEncoder.class, "tile.xact.Encoder");
		GameRegistry.registerTileEntity(TileCrafter.class, "tile.xact.Crafter");

		// Add names
		LanguageRegistry.addName(itemRecipeBlank, "Recipe Chip");
		LanguageRegistry.addName(itemRecipeEncoded, "\u00a72"+"Recipe Chip");
			
		// machine's names
		LanguageRegistry.addName(new ItemStack(blockMachine, 1, 0), "XACT Encoder");
		LanguageRegistry.addName(new ItemStack(blockMachine, 1, 1), "XACT Crafter");

		// tab's name
		LanguageRegistry.instance().addStringLocalization("itemGroup.xact", "XACT");

		// Register GUIs
		NetworkRegistry.instance().registerGuiHandler(XActMod.instance, proxy);

		// Add the recipes
		addRecipes();
	}

	private void addRecipes() {
		ItemStack[] ingredients;
		
		// Recipe Chip
		ItemStack chip = new ItemStack(itemRecipeBlank, 4);
		ingredients = ingredients(Item.ingotIron, Item.paper);
		GameRegistry.addRecipe(shapelessRecipe(chip, ingredients));
		
//		// Encoder todo: remove
//		ingredients = ingredients(
//				Block.glass, 	itemRecipeBlank,	Block.glass,
//				Item.ingotGold,	Block.workbench, 	Item.ingotGold,
//				Block.stone, 	Item.ingotIron, 	Block.stone
//		);
//		GameRegistry.addRecipe(new ShapedRecipes(3, 3, ingredients, new ItemStack(blockMachine, 1, 0)));

		// Crafter
		ingredients = ingredients(
                itemRecipeBlank, Block.glass, 		itemRecipeBlank,
                itemRecipeBlank, Block.workbench, 	itemRecipeBlank,
				Item.ingotIron,  Block.chest, 		Item.ingotIron
		);
		GameRegistry.addRecipe(new ShapedRecipes(3, 3, ingredients, new ItemStack(blockMachine, 1, 1)));
	}


	private IRecipe shapelessRecipe(ItemStack output, ItemStack... ingredients){
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for( ItemStack ingredient : ingredients ){
			if( ingredient != null )
				list.add(ingredient);
		}
		return new ShapelessRecipes(output, list);
	}
	private ItemStack[] ingredients(Object... objects){
		ItemStack[] retValue = new ItemStack[9];
		int index =-1;
		for( Object o : objects ){
			index++;
			if( index >= 9 )
				break;
			if( o == null ){
				retValue[index] = null;
				continue;
			}
			if( o instanceof Item ){
				retValue[index] = new ItemStack((Item) o);
				continue;
			}
			if( o instanceof Block ){
				retValue[index] = new ItemStack((Block) o);
			}
            if( o instanceof ItemStack )
                retValue[index] = (ItemStack) o;
		}

		return retValue;
	}

}
