package xk.xact.api;


import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.recipes.CraftRecipe;

public interface SpecialCasedRecipe {

	public boolean isSpecialCased(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex);

	public boolean isMatchingIngredient(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex, World world);

}
