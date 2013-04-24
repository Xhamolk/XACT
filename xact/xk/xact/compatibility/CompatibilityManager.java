package xk.xact.compatibility;


import cpw.mods.fml.common.Loader;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.plugin.mps.PluginForMPS;
import xk.xact.recipes.CraftRecipe;
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

	private static void checkIC2() {
		if( !Loader.isModLoaded( "IC2" ) )
			return;

		// Special case IC2's recipes that involve electric items.
		specialCasedRecipes.add( new SpecialCasedRecipe() {
			@Override
			public boolean isSpecialCased(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex) {
				if( classMatches( recipe.getRecipePointer().getIRecipe(), "AdvRecipe" ) ) { // ic2.core.AdvRecipe
					if( classMatches( ingredient.getItem(), "IElectricItem" ) ) // ic2.api.item.IElectricItem
						return true;
				}
				return false;
			}

			@Override
			public boolean isMatchingIngredient(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex, World world) {
				return true;
			}
		} );
	}


	public static boolean classMatches(Object o, String className) {
		return o != null && o.getClass().getSimpleName().equals( className );
	}

	public static List<SpecialCasedRecipe> getSpecialCasedRecipes() {
		return specialCasedRecipes;
	}
}
