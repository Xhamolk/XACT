package xk.xact.recipes;

import net.minecraft.src.ItemStack;
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
					list.addStack(current);
					// System.out.println("simple-ref: "+current.getItem().getItemDisplayName(current));
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("|>>>>>>>>>  getSimplifiedIngredients() ");
            }
        }

		return simpleIngredients = list.toArray();
	}


    public String toString() {
        return this.result.getItem().getItemDisplayName(result);
    }

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

}
