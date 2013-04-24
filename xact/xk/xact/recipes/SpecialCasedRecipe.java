package xk.xact.recipes;


import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface SpecialCasedRecipe {

	public boolean isSpecialCased(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex);

	public boolean isMatchingIngredient(CraftRecipe recipe, ItemStack ingredient, int ingredientIndex, World world);

}
