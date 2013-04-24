package xk.xact.compatibility;


import cpw.mods.fml.common.Loader;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import xk.xact.inventory.InventoryUtils;
import xk.xact.plugin.mps.PluginForMPS;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.recipes.SpecialCasedRecipe;

import java.util.ArrayList;
import java.util.List;

public class CompatibilityManager {

	private static List<SpecialCasedRecipe> specialCasedRecipes = new ArrayList<SpecialCasedRecipe>();


	public static void checkEverything() {
		checkIC2();
	}

	public static void initializePlugins() {
		PluginForMPS.loadPlugin();
	}

	public static List<SpecialCasedRecipe> getSpecialCasedRecipes() {
		return specialCasedRecipes;
	}

	private static void checkIC2() {
		if( !Loader.isModLoaded( "IC2" ) )
			return;

		final Class electricItemIC2 = getClass( "ic2.api.item.IElectricItem" );
		if( electricItemIC2 == null )
			return;

		System.out.println( "Special Casing IC2 Recipes." );

		// Special case IC2's recipes that involve electric items.
		specialCasedRecipes.add( new SpecialCasedRecipe() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean isSpecialCased(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex) {
				if( classMatches( recipe.getRecipePointer().getIRecipe(), "AdvRecipe" ) ) { // ic2.core.AdvRecipe
					if( electricItemIC2.isInstance( ingredient.getItem() ) ) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean isMatchingIngredient(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex, World world) {
				InventoryCrafting craftingGrid = RecipeUtils.simulateGrid( recipe, ingredientIndex, ingredient );
				ItemStack output = CraftingManager.getInstance().findMatchingRecipe( craftingGrid, world );

				return output != null && InventoryUtils.similarStacks( recipe.getResult(), output, false );
			}
		} );
	}


	private static boolean classMatches(Object o, String className) {
		return o != null && o.getClass().getSimpleName().equals( className );
	}

	private static Class getClass(String classFullName) {
		try {
			return Class.forName( classFullName );
		} catch( ClassNotFoundException e ) {
			return null;
		}
	}
}
