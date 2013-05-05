package xk.xact.recipes;


import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import xk.xact.inventory.InventoryUtils;

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
			return getIRecipe().getRecipeOutput();
		return getIRecipe().getCraftingResult( craftingGrid );
	}

	public IRecipe getIRecipe() {
		if( recipe == null ) {
			recipe = (IRecipe) CraftingManager.getInstance().getRecipeList().get( recipeID );
		}
		return recipe;
	}

	public boolean isOreRecipe() {
		recipe = getIRecipe();
		return recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe;
	}

	@SuppressWarnings("unchecked")
	public CraftRecipe getCraftRecipe(InventoryCrafting craftingGrid) {
		ItemStack result = getOutputFrom( craftingGrid );
		ItemStack[] ingredients;
		getIRecipe(); // make sure recipe is instantiated.

		ingredients = new ItemStack[9];
		ItemStack[] tempIngredients = InventoryUtils.getContents( craftingGrid );

		for( int i = 0; i < 9; i++ ) {
			if( tempIngredients[i] != null ) {
				ingredients[i] = tempIngredients[i].copy();
				ingredients[i].stackSize = 1;
			} else
				ingredients[i] = null;
		}

		CraftRecipe craftRecipe = new CraftRecipe( result, ingredients );
		craftRecipe.recipeID = this.recipeID;
		return craftRecipe;
	}


}
