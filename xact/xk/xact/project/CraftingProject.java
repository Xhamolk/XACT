package xk.xact.project;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xk.xact.recipes.CraftRecipe;
import xk.xact.inventory.InventoryUtils;
import xk.xact.util.Utils;

import java.util.ArrayList;

/**
 * This is the representation of the crafting project.
 */
public class CraftingProject {

	private IngredientsList list;
	private ItemStack targetItem;
	private CraftRecipe targetRecipe;

	private CraftingProject() {
		this.list = new IngredientsList( this );
	}

	public void setTarget(ItemStack item, CraftRecipe recipe) {
		list.clear();

		this.targetItem = item;
		this.targetRecipe = recipe;

		// add the ingredients to the list.
		for( ItemStack ingredient : recipe.getSimplifiedIngredients() ) {
			list.addIngredient( ingredient );
		}
	}

	public ItemStack getTargetItem() {
		return this.targetItem;
	}

	public CraftRecipe getMainRecipe() {
		return this.targetRecipe;
	}

	public void addRecipe(ItemStack item, CraftRecipe recipe) {
		list.setRecipe( item, recipe );
	}

	public void removeRecipe(ItemStack item) {
		list.removeRecipe( item );
	}

	public CraftRecipe getRecipeFor(ItemStack itemStack) {
		return list.getRecipe( itemStack );
	}


	/**
	 * Gets an ordered list of all the raw materials involved in this Crafting Project.
	 */
	public ArrayList<ItemStack> getRawMaterials() {
		ArrayList<ItemStack> rawMaterials = new ArrayList<ItemStack>();

		for( ItemStack current : list ) {
			if( !list.hasRecipe( current ) )
				rawMaterials.add( current );
		}

		return rawMaterials;
	}

	/**
	 * Gets an ordered list of the amounts required of each raw material.
	 *
	 * @see xk.xact.project.CraftingProject#getRawMaterials()
	 */
	public ArrayList<Integer> getRawMaterialsCount() {
		ArrayList<Integer> retValue = new ArrayList<Integer>();
		for( ItemStack current : getRawMaterials() ) {
			retValue.add( countIngredientRequirement( current, 1 ) );
		}

		return retValue;
	}

	public int countIngredientRequirement(ItemStack item, int amount) {
		CraftRecipe recipe = getMainRecipe();
		double multiplier = amount / recipe.getResult().stackSize;
		return (int) Math.ceil( count( item, recipe, multiplier ) );
	}

	private double count(ItemStack item, CraftRecipe recipe, double multiplier) {
		int count = 0;
		for( ItemStack ingredient : recipe.getSimplifiedIngredients() ) {
			int amount = ingredient.stackSize;
			if( InventoryUtils.similarStacks( ingredient, item, false ) ) {
				count += amount;
			} else if( list.hasRecipe( ingredient ) ) {
				CraftRecipe recipe2 = list.getRecipe( item );
				double nextMultiplier = multiplier * amount / recipe2.getResult().stackSize;

				count += count( item, recipe2, nextMultiplier );
			}
		}
		return count * multiplier;
	}

	public boolean itemHasRecipe(ItemStack item) {
		return list.hasRecipe( item );
	}

	///////////////
	///// NBT

	public void writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = new NBTTagCompound();

		// Write target item
		Utils.writeItemStackToNBT( nbt, targetItem, "targetItem" );

		// Write target recipe
		targetRecipe.writeToNBT( nbt );

		// Write the list of ingredients
		list.writeToNBT( nbt );

		compound.setTag( "craftingProject", nbt );
	}

	public static CraftingProject readFromNBT(NBTTagCompound compound) {
		CraftingProject project = new CraftingProject();

		if( compound == null )
			return project;

		NBTTagCompound nbt = (NBTTagCompound) compound.getTag( "craftingProject" );
		if( nbt == null )
			return project;

		// Read target item
		ItemStack item = Utils.readStackFromNBT( (NBTTagCompound) nbt.getTag( "targetItem" ) );

		// Read target recipe
		CraftRecipe recipe = CraftRecipe.readFromNBT( nbt );

		project.setTarget( item, recipe );

		// Read the list of ingredients
		project.list.readFromNBT( nbt );

		return project;
	}

}
