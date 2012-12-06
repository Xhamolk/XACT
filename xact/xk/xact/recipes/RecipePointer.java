package xk.xact.recipes;


import net.minecraft.src.*;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import xk.xact.util.FakeCraftingInventory;

import java.util.List;

public class RecipePointer {

	public final int recipeID;

	protected IRecipe recipe;

	private RecipePointer(int index) {
		this.recipeID = index;
	}

	public static RecipePointer getRecipe(int index) {
		if( index < CraftingManager.getInstance().getRecipeList().size() ) {
			return new RecipePointer( index );
		}
		return null;
	}


	public ItemStack getOutputFrom(InventoryCrafting craftingGrid) {
		if( craftingGrid == null )
			return recipe.getRecipeOutput();
		return recipe.getCraftingResult(craftingGrid);
	}

	public IRecipe getIRecipe() {
		if( recipe == null ) {
			recipe = (IRecipe) CraftingManager.getInstance().getRecipeList().get(recipeID);
		}
		return recipe;
	}

	public boolean isOreRecipe() {
		recipe = getIRecipe();
		return recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe;
	}

	@SuppressWarnings("unchecked")
	public CraftRecipe getCraftRecipe(InventoryCrafting craftingGrid) {
		ItemStack result = getOutputFrom(craftingGrid);
		ItemStack[] ingredients;

		if( recipe instanceof ShapedRecipes ) {
			ingredients = ((ShapedRecipes) recipe).recipeItems;

		} else if( recipe instanceof ShapelessRecipes ) {
			List list = ((ShapelessRecipes) recipe).recipeItems;
			ingredients = (ItemStack[]) list.toArray(new ItemStack[list.size()]);

		} else {
			ingredients = new ItemStack[9];
			ItemStack[] tempIngredients = ((FakeCraftingInventory)craftingGrid).getContents();

			for( int i=0; i<9; i++  ) {
				if( tempIngredients[i] != null ) {
					ingredients[i] = ingredients[i].copy();
					ingredients[i].stackSize = 1;
				} else
					ingredients[i] = null;
			}
		}

		CraftRecipe craftRecipe = new CraftRecipe(result, ingredients);
		craftRecipe.recipeID = this.recipeID;

		return craftRecipe;
	}


}
