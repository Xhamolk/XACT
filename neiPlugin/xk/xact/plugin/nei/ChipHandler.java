package xk.xact.plugin.nei;

import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.ShapedRecipeHandler.CachedShapedRecipe;
import cpw.mods.fml.client.FMLClientHandler;

public class ChipHandler extends ShapedRecipeHandler {

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		if(CraftManager.isEncoded(ingredient)){
			
			CraftRecipe recipe = RecipeUtils.getRecipe( ingredient, FMLClientHandler.instance().getClient().theWorld );
			if(recipe != null) {
				CachedShapedRecipe recipeT = new CachedShapedRecipe(3, 3, recipe.getIngredients(), ingredient);
				arecipes.add(recipeT);
			}
		}
	}
}
