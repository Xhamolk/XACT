package xk.xact.recipes;

import net.minecraft.src.*;
import xk.xact.core.ItemChip;
import xk.xact.XActMod;
import xk.xact.util.FakeCraftingInventory;

/**
 * Handles the encoding/decoding of CraftRecipes.
 * @author Xhamolk_
 */
public class CraftManager {

	/**
	 * Encodes a crafting recipe into an item stack.
	 *
	 * @param recipe the recipe.
	 * @return an ItemStack with the recipe stored in it's NBT.
	 */
	public static ItemStack encodeRecipe(CraftRecipe recipe) {
		ItemStack stack = new ItemStack(XActMod.itemRecipeEncoded, 1);

		if( stack.stackTagCompound == null )
			stack.stackTagCompound = new NBTTagCompound();

		recipe.writeToNBT(stack.stackTagCompound);
		return stack;
	}

	public static NBTTagCompound generateNBTTagFor(CraftRecipe recipe){
		// Result
		NBTTagCompound result = new NBTTagCompound();
		recipe.result.writeToNBT(result);

		// Ingredients
		NBTTagList listIngredients = new NBTTagList();
		ItemStack[] ingredients = recipe.ingredients;
		for(int i=0; i<9; i++) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("index", i);
			if( ingredients[i] != null )
				ingredients[i].writeToNBT(tag);
			listIngredients.appendTag(tag);
		}

		// Actual encoding
		NBTTagCompound recipeCompound = new NBTTagCompound("encodedRecipe");
		recipeCompound.setTag("recipeResult", result);
		recipeCompound.setTag("recipeIngredients", listIngredients);

		return recipeCompound;
	}

	/**
	 * Decodes the CraftRecipe stored on the specified stack.
	 *
	 * @param stack the stack containing the ItemChip
	 * @return a CraftRecipe representation of the recipe.
	 */
	public static CraftRecipe decodeRecipe(ItemStack stack){
		if( stack == null || !(stack.getItem() instanceof ItemChip) )
			return null;

		// Read recipe.
		if( stack.stackTagCompound == null )
			return null;

		return CraftRecipe.readFromNBT(stack.stackTagCompound);
	}

    /**
     * Generate a CraftRecipe instance from the ingredients provided.
     * @param ingredients the recipe's ingredients.
     * @return null if invalid or recipe not found. Else, a CraftRecipe representation.
     */
    public static CraftRecipe generateRecipe(ItemStack[] ingredients, World world) {
        if(ingredients == null || ingredients.length != 9)
            return null;

        FakeCraftingInventory craftGrid = FakeCraftingInventory.emulateContents(ingredients);
        if( craftGrid == null )
            return null;
        ItemStack output = CraftingManager.getInstance().findMatchingRecipe(craftGrid, world);
        if( output == null )
            return null;

        ItemStack[] realIngredients = new ItemStack[9];
        for( int i=0; i<9; i++  ) {
            if( ingredients[i] != null ) {
                realIngredients[i] = ingredients[i].copy();
                realIngredients[i].stackSize = 1;
				// System.out.println("gen: "+ realIngredients[i].getItem().getItemDisplayName(realIngredients[i]));
            } else
                realIngredients[i] = null;
        }

        return new CraftRecipe(output, ingredients);
    }

	////////////////////
	///// NBT Structure

	/*
	stackTagCompound:
		encodedRecipe:
				recipeResult -> ItemStack
				recipeIngredients: (1-9) -> ItemStack[9]
				 	 [ int index, ItemStack ingredient ]
	 */

	/**
	 * Checks if the specified ItemStack is valid.
	 *
	 * @param stack the target to check.
	 * @return true if contains an instance of this class.
	 */
	public static boolean isValid(ItemStack stack) {
		return ( stack != null && stack.getItem() instanceof ItemChip);
	}

	/**
	 * Checks if the specified ItemStack contains an encoded recipe.
	 * Note: it must be a valid stack for this to work correctly.
	 *
	 * @param stack the stack to check.
	 * @return true if contains an encoded recipe.
	 */
	public static boolean isEncoded(ItemStack stack){
		return isValid(stack) && ((ItemChip) stack.getItem()).encoded;
	}
}
