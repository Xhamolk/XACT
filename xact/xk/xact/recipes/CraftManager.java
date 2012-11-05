package xk.xact.recipes;

import net.minecraft.src.*;
import xk.xact.ItemRecipe;
import xk.xact.XActMod;
import xk.xact.util.FakeCraftingInventory;

import java.util.Iterator;

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
		NBTTagCompound recipeCompound = new NBTTagCompound();
		recipeCompound.setTag("recipeResult", result);
		recipeCompound.setTag("recipeIngredients", listIngredients);

		if( stack.stackTagCompound == null )
			stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setTag("encodedRecipe", recipeCompound);

		return stack;
	}

	/**
	 * Decodes the CraftRecipe stored on the specified stack.
	 *
	 * @param stack the stack containing the ItemRecipe
	 * @return a CraftRecipe representation of the recipe.
	 */
	public static CraftRecipe decodeRecipe(ItemStack stack){
		if( stack == null || !(stack.getItem() instanceof ItemRecipe) )
			return null;

		// Read recipe.
		if( stack.stackTagCompound == null )
			return null;
		
		NBTTagCompound compound = (NBTTagCompound) stack.stackTagCompound.getTag("encodedRecipe");
		if( compound == null ) return null;

		ItemStack[] ingredients = new ItemStack[9];
		NBTTagList tagList = compound.getTagList("recipeIngredients");
		for(int i=0; i<9; i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
			ingredients[i] = ItemStack.loadItemStackFromNBT(tag);
		}

		ItemStack result = ItemStack.loadItemStackFromNBT((NBTTagCompound) compound.getTag("recipeResult"));

		return new CraftRecipe(result, ingredients);
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
		return ( stack != null && stack.getItem() instanceof ItemRecipe );
	}

	/**
	 * Checks if the specified ItemStack contains an encoded recipe.
	 * Note: it must be a valid stack for this to work correctly.
	 *
	 * @param stack the stack to check.
	 * @return true if contains an encoded recipe.
	 */
	public static boolean isEncoded(ItemStack stack){
		return isValid(stack) && ((ItemRecipe) stack.getItem()).encoded;
	}
}
