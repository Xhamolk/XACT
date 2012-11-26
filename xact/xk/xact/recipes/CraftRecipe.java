package xk.xact.recipes;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import xk.xact.util.StackList;

/**
 * The representation of a crafting recipe.
 */
public class CraftRecipe {

	protected final ItemStack result;

	protected final ItemStack[] ingredients;

    private ItemStack[] simpleIngredients = null;
	
	public final int size;

	// protected so it can only be accessed by CraftManager
    protected CraftRecipe(ItemStack result, ItemStack[] ingredients) {
		this.result = result;
		this.ingredients = ingredients.clone();
		this.size = ingredients.length;
	}

	/**
	 * Gets a copy of the output item of this recipe.
	 * @return an ItemStack representation.
	 */
	public ItemStack getResult() {
		if( result == null )
			return null;
		return result.copy();
	}

	/**
	 * Gets the ingredients of this recipe.
	 *
	 * @return an ItemStack[size] ordered as it should be displayed on a crafting grid.
	 */
	public ItemStack[] getIngredients() {
		return ingredients.clone();
	}


	/**
	 * Gets the ingredients in a convenient form.
	 * The array is packed so that the ItemStack.stackSize represents the quantity required of that item.
	 * Also,
	 *
	 * @return an array of ItemStack containing the ingredients.
	 */
	public ItemStack[] getSimplifiedIngredients() {
        if( simpleIngredients != null )
            return simpleIngredients;

		StackList list = new StackList();
        for( ItemStack current : getIngredients() ){
            try {
                if( current != null ) {
					list.addStack(current.copy());
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("|>>>>>>>>>  getSimplifiedIngredients() ");
            }
        }

		return simpleIngredients = list.toArray();
	}

	// the output's name.
    public String toString() {
        return this.result.getItem().getItemDisplayName(result);
    }

	/**
	 * A string listing all the ingredients for this recipe by their amount and name.
	 *
	 * @see xk.xact.recipes.CraftRecipe#getSimplifiedIngredients()
	 */
    public String ingredientsToString() {
        String retValue = "";
        ItemStack[] ingredients = this.getSimplifiedIngredients();
        for( int i=0; i<ingredients.length; i++){
            ItemStack stack = ingredients[i];
            if( stack == null )
                continue;

            retValue += stack.stackSize + "x " + stack.getItem().getItemDisplayName(stack);
            if( i < ingredients.length-1 )
                retValue += ", ";
        }
        return retValue;
    }

	/**
	 * Create a CraftRecipe from the NBT.
	 * If can't read it, will return null.
	 *
	 * @param nbtCompound the tag from which to read.
	 * @return a CraftRecipe object, or null if something went wrong.
	 */
	public static CraftRecipe readFromNBT( NBTTagCompound nbtCompound ) {
		NBTTagCompound compound = (NBTTagCompound) nbtCompound.getTag("encodedRecipe");
		if( compound == null ) return null;

		ItemStack[] ingredients = new ItemStack[9];
		NBTTagList tagList = compound.getTagList("recipeIngredients");
		if( tagList == null ) return null;
		for(int i=0; i<9; i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
			ingredients[i] = ItemStack.loadItemStackFromNBT(tag);
		}

		ItemStack result = ItemStack.loadItemStackFromNBT((NBTTagCompound) compound.getTag("recipeResult"));

		return new CraftRecipe(result, ingredients);
	}

	/**
	 * Write this CraftRecipe to the NBT.
	 * @param nbtCompound the tag to which write.
	 */
	public void writeToNBT( NBTTagCompound nbtCompound ) {
		NBTTagCompound tag = CraftManager.generateNBTTagFor(this);
		if( tag != null)
			nbtCompound.setTag("encodedRecipe", tag);
	}

}
