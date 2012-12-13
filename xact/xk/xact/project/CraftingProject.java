package xk.xact.project;


import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.InventoryUtils;

import java.util.ArrayList;

/**
 * This is the representation of the crafting project.
 */
public class CraftingProject {

	private IngredientsList list;
	private ItemStack targetItem;
	private CraftRecipe targetRecipe;

	private CraftingProject() {
		this.list = new IngredientsList(this);
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
		list.setRecipe(item, recipe);
	}

	public void removeRecipe(ItemStack item) {
		list.removeRecipe(item);
	}


	/**
	 * Gets an ordered list of all the raw materials involved in this Crafting Project.
	 */
	public ArrayList<ItemStack> getRawMaterials() {
		ArrayList<ItemStack> rawMaterials = new ArrayList<ItemStack>();

		for( ItemStack current : list ) {
			if( !list.hasRecipe(current) )
				rawMaterials.add( current );
		}

		return rawMaterials;
	}

	/**
	 * Gets an ordered list of the amounts required of each raw material.
	 * @see xk.xact.project.CraftingProject#getRawMaterials()
	 */
	public ArrayList<Integer> getRawMaterialsCount() {
		ArrayList<Integer> retValue = new ArrayList<Integer>();
		for( ItemStack current : getRawMaterials() ) {
			retValue.add( countIngredientRequirement( current ) );
		}

		return retValue;
	}

	public int countIngredientRequirement(ItemStack item) {
		return count(item, getMainRecipe(), 1); // this '1' could be changed.
	}

	private int count(ItemStack item, CraftRecipe recipe, int multiplier) {
		int count = 0;
		for( ItemStack ingredient : recipe.getSimplifiedIngredients() ) {
			int amount = ingredient.stackSize;
			if( InventoryUtils.similarStacks( ingredient, item) ) {
				count += amount;
			} else if( list.hasRecipe( ingredient ) ) {
				count += count( item, list.getRecipe(item), multiplier * amount);
			}
		}
		return count * multiplier;
	}


	///////////////
	///// NBT

	public void writeToNBT( NBTTagCompound compound ) {
		NBTTagCompound nbt = new NBTTagCompound();

		// Write target item
		InventoryUtils.writeItemStackToNBT( nbt, targetItem, "targetItem");

		// Write target recipe
		targetRecipe.writeToNBT( nbt );

		// Write the list of ingredients
		list.writeToNBT( nbt );

		compound.setTag( "craftingProject" , nbt);
	}

	public static CraftingProject readFromNBT( NBTTagCompound compound ) {
		CraftingProject project = new CraftingProject();

		NBTTagCompound nbt = (NBTTagCompound) compound.getTag("craftingProject");
		if( nbt == null )
			return project;

		// Read target item
		ItemStack item = InventoryUtils.readStackFromNBT((NBTTagCompound) nbt.getTag("targetItem"));

		// Read target recipe
		CraftRecipe recipe = CraftRecipe.readFromNBT( nbt );

		project.setTarget(item, recipe);

		// Read the list of ingredients
		project.list.readFromNBT( nbt );

		return project;
	}


}
