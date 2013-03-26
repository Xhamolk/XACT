package xk.xact.core;


import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import xk.xact.XActMod;
import xk.xact.core.items.ItemMachine;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.Textures;

public enum Machines {

	CRAFTER( "crafter", "XACT Crafter" ) {
		@Override
		ItemStack[] ingredients() {
			return RecipeUtils.ingredients(
					XActMod.itemRecipeBlank,    Block.glass,        XActMod.itemRecipeBlank,
					XActMod.itemRecipeBlank,    Block.workbench,    XActMod.itemRecipeBlank,
					Item.ingotIron,             Block.chest,        Item.ingotIron
			);
		}

		@Override
		public String[] getTextureFiles() {
			return new String[] {
					Textures.CRAFTER_BOTTOM, Textures.CRAFTER_TOP, Textures.CRAFTER_FRONT, Textures.CRAFTER_SIDE
			};
		}
	};


	private final String machineName;
	private final String localizedName;

	private Machines(String name, String localizedName) {
		this.machineName = name;
		this.localizedName = localizedName;
	}

	///////////////
	///// Names

	public String getLocalizedName() {
		return localizedName; // temp - until i set up localizations.
	}

	public static String getMachineName(ItemStack itemStack) {
		if( itemStack != null && itemStack.getItem() instanceof ItemMachine ) {
			return Machines.values()[itemStack.getItemDamage()].machineName;
		}
		return null;
	}

	///////////////
	///// Recipes

	public IRecipe getMachineRecipe() {
		return new ShapedRecipes( 3, 3, ingredients(), new ItemStack( XActMod.blockMachine, 1, this.ordinal() ) );
	}

	abstract ItemStack[] ingredients();

	///////////////
	///// Misc

	public static int getMachineFromMetadata(int metadata) {
		metadata = (metadata & 0xE) >> 1;
		if( metadata >= 0 && metadata < Machines.values().length ) {
			return Machines.values()[metadata].ordinal();
		}
		return 0; // to maintain backwards compatibility
	}

	///////////////
	///// Textures

	public static String[] getTextureFiles(int machine) {
		if( machine >= 0 && machine < Machines.values().length )
			return Machines.values()[machine].getTextureFiles();
		return new String[0];
	}

	// Bottom, Top, Front, Side.
	abstract String[] getTextureFiles();

}
