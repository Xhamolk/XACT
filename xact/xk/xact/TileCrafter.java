package xk.xact;

import net.minecraft.src.*;

import xk.xact.event.XactEvent;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.FakeCraftingInventory;
import xk.xact.util.InvSlot;
import xk.xact.util.Inventory;
import xk.xact.util.InventoryUtils;

import java.util.ArrayList;

import static xk.xact.util.InventoryUtils.inventoryIterator;

/**
 * 
 * @author Xhamolk_
 */
public class TileCrafter extends TileMachine implements IInventory {

	/*
	Available Inventories:
		This should be able to pull items from adjacent chests.

	Crafting mechanism:
		The CraftRecipe object knows the ingredients and their position on the crafting grid.
		If the required items are present on the resources buffer, then
		 (per operation) each ingredient will be placed into a fake crafting grid.
		 The recipe will be crafted.
		 Finally, the remaining items on the crafting grid will be placed back on the resources.
	 */

	/**
	 * Holds the recipes' outputs
	 */
	public final Inventory results;

	/**
	 * The inventory that holds the circuits.
	 */
	public final Inventory circuits; // size = 4

	/**
	 * The resources inventory.
	 * You can access this inventory through pipes/tubes.
	 */
	public final Inventory resources; // size = 2*9 = 18


	public TileCrafter() {
		this.results = new Inventory(4, "Results"){
			@Override
			public ItemStack getStackInSlot(int slot) {
				if( 0 <= slot && slot < 4 )
					return getRecipeResult(slot);
				return null;
			}
		};
		this.circuits = new Inventory(4, "Encoded Recipes") {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateRecipes();
				TileCrafter.this.updateStates();
			}
		};
		this.resources = new Inventory(2*9, "Resources") {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateStates();
			}
		};
	}

	@Override // the items to be dropped when the block is destroyed.
	public ArrayList<ItemStack> getDropItems() {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(int i=0; i<this.circuits.getSizeInventory(); i++){
			ItemStack stack = circuits.getStackInSlotOnClosing(i);
			if( stack != null )
				list.add( stack );
		}
		for(int i=0; i<this.resources.getSizeInventory(); i++){
			ItemStack stack = resources.getStackInSlotOnClosing(i);
			if( stack != null )
				list.add( stack );
		}
		return list;
	}


	/**
	 * Gets all the available inventories.
	 * In other words, all the inventories from which this crafter can pull resources.
	 * Adjacent chests (except on the top) will be included.
	 *
	 * By design, the first inventory on the list is always the internal resources buffer.
	 * @return an array of all the available IInventories.
	 */
	public IInventory[] getAvailableInventories() {
		return new Inventory[]{resources}; // temporal. todo: add adjacent chests.
	}
	



	///////////////
	///// Current State (requires updates)

	private boolean[] redState = new boolean[4];

	private CraftRecipe[] recipes = new CraftRecipe[4];


	// whether if the recipe's state must be red.
	public boolean isRedState( int i ) {
		return redState[i];
	}

	// Gets the recipe's result.
	public ItemStack getRecipeResult(int slot) {
		CraftRecipe recipe = this.getRecipeAt(slot);
		return recipe == null ? null : recipe.getResult();
	}

	// updates the stored recipe results.
	public void updateRecipes() {
		for(int i=0; i<4; i++) {
			ItemStack stack = this.circuits.getStackInSlot(i);
			if( stack == null )
				recipes[i] = null;
			else
				recipes[i] = getRecipeFrom(stack);
		}

		for(int i=0; i<4; i++) {
			ItemStack stack = recipes[i] == null ? null : recipes[i].getResult();
			results.setInventorySlotContents(i, stack);
		}
	}

	public void updateStates() {
		for(int i=0;i<4; i++) {
			// if there are not enough ingredients, color is red.
			redState[i] = (recipes[i] != null) && !canCraft(getRecipeAt(i));
		}
	}


	///////////////
	///// Functionality

	/**
	 * Whether if the specified recipe can be crafted.
	 * In other words, if there are enough items to craft it.
	 *
	 * @param recipe the recipe to check.
	 * @return true if contains all the required ingredients.
	 */
	public boolean canCraft(CraftRecipe recipe) {
		ItemStack[] ingredients = recipe.getSimplifiedIngredients();
		for(ItemStack cur : ingredients) {
			if( cur == null ) continue;
			
			int found = getCountFor(cur, false);
			if( found < cur.stackSize )
				return false;
		}
		return true;
	}

	public boolean canCraftRecipe(int recipeIndex) {
		return canCraft(this.getRecipeAt(recipeIndex));
	}

	/**
	 * Gets the amount of items of the same kind of the passed stack on the available inventories.
	 *
	 * @param stack the stack to compare with.
	 * @param countAll if false, will iterate through all the inventories until found enough.
	 *               Enough means the amount found is equal or higher than the passed stack.stackSize.
	 * @return the count of the items found.
	 */
	public int getCountFor(ItemStack stack, boolean countAll) {
		int found = 0;
		IInventory[] inventories = getAvailableInventories();
		for( IInventory inv : inventories ) {
			int size = inv.getSizeInventory();
			for( int i=0; i<size; i++ ) {

				if( !countAll && found >= stack.stackSize )
					break; // prevent counting on more if found enough already.

				ItemStack current = inv.getStackInSlot(i);
				if( current != null && stack.itemID == current.itemID ){
					if( stack.getItemDamage() == current.getItemDamage() )
						found += current.stackSize;
				}
			}
		}
		return found;
	}

	/**
	 * Gets the amount of missing ingredients.
	 * The array contains the amount (associated by index with the ingredient it represents) missing of that item.
	 *
	 * @param recipe the CraftRecipe representation of the recipe.
	 * @return an array of int. any value of 0 represents there's no items left.
	 * @see xk.xact.recipes.CraftRecipe#getSimplifiedIngredients()
	 */
	public int[] getMissingIngredientsCount(CraftRecipe recipe) { // Example: Missing 3 redstone, 2 cobblestone.
		ItemStack[] ingredients = recipe.getSimplifiedIngredients();
		int[] retValue = new int[ingredients.length];
		for( int i=0; i<ingredients.length; i++) {
			ItemStack stack = ingredients[i];
			if( stack == null ){
				retValue[i] = 0;
				continue;
			}
			int found = getCountFor(stack, false);
			retValue[i] = (found >= stack.stackSize) ? 0 : stack.stackSize - found;
		}
		return retValue;
	}


    public String getMissingIngredients(CraftRecipe recipe) {
        if( recipe == null )
			return "<invalid recipe>";

		String retValue = "";
		ItemStack[] ingredients = recipe.getSimplifiedIngredients();
		int[] missingCount = getMissingIngredientsCount(recipe);
		boolean addCommas = false;
		for( int i=0; i<ingredients.length; i++ ){
			if( missingCount[i] > 0 ) {
				ItemStack tempStack = ingredients[i].copy();
				tempStack.stackSize = missingCount[i];
				if( addCommas )
					retValue += ", ";
				retValue += InventoryUtils.stackDescription(tempStack);
				addCommas = true;
			}
		}
		return retValue.equals("") ? "none" : retValue;
    }


	/**
	 * Decodes and gets the recipe encoded to the circuit on the specified slot.
	 *
	 * @param slot the slot from where to get.
	 * @return a CraftRecipe representation, or null if invalid.
	 */
	public CraftRecipe getRecipeAt(int slot) {
		if( slot < 0 || slot > 4 )
			return null;
		if( recipes[slot] == null ) {
			ItemStack stack = this.circuits.getStackInSlot(slot);
			if( stack != null )
				recipes[slot] = CraftManager.decodeRecipe(stack);
		}
		return recipes[slot];
	}

	public CraftRecipe getRecipeFrom(ItemStack stack){
		if( stack != null && CraftManager.isEncoded(stack) )
			return CraftManager.decodeRecipe( stack );
		return null;
	}

	/**
	 * Will generate a temporary crafting grid based on the recipe's ingredients.
	 * The items that populate the grid will be taken from the available inventories.
	 *
	 * @param recipe the recipe from which to take the ingredients.
	 * @return A FakeCraftingInventory
	 * @throws xk.xact.util.MissingIngredientsException if cannot find enough items to fill the grid with.
	 */
	public FakeCraftingInventory generateTemporaryCraftingGridFor(CraftRecipe recipe) {
		if( !canCraft(recipe) ) {
            System.err.println("XACT: generateTemporaryCraftingGridFor: !canCraft");
			return null;
        }

		ItemStack[] ingredients = recipe.getIngredients();
		ItemStack[] contents = new ItemStack[recipe.size]; // the return value.

		items: for( int i=0; i<ingredients.length; i++) {
			ItemStack ingredient = ingredients[i];
			if( ingredient == null ) {
                continue;
            }

			int required = ingredient.stackSize;
			
			// iterate through every slot on every available inventory.
			for( IInventory inv : getAvailableInventories() ) {
				for( InvSlot slot : inventoryIterator(inv) ){
					if( required <= 0 ) continue items;
					if( slot == null )
						continue;

					if( slot.containsItemsFrom(ingredient) ) {
						ItemStack stackInSlot = inv.getStackInSlot(slot.slotIndex);

						if( stackInSlot.stackSize > required ){
							contents[i] = inv.decrStackSize( slot.slotIndex, required );
							inv.onInventoryChanged();
							continue items;
						} else {
							if( contents[i] == null ){
								contents[i] = stackInSlot;
							} else {
								contents[i].stackSize += stackInSlot.stackSize;
							}
							required -= stackInSlot.stackSize;
							inv.setInventorySlotContents(slot.slotIndex, null);
							inv.onInventoryChanged();
						}
					}
				}
			}
			// should find the all items, since canCraft was true.
		}

		return FakeCraftingInventory.emulateContents(contents);
	}

	///////////////
	///// IInventory: provide access to the resources inventory
	
	
	@Override
	public int getSizeInventory() {
		return resources.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return resources.getStackInSlot(var1);
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return resources.decrStackSize(var1, var2);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return resources.getStackInSlotOnClosing(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		resources.setInventorySlotContents(var1, var2);
	}

	@Override
	public String getInvName() {
		return resources.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	public void onInventoryChanged() { // this is called when adding stuff through pipes/tubes/etc 
	    resources.onInventoryChanged();
	}  

	@Override public void openChest() { }

	@Override public void closeChest() { }

	///////////////
	///// NBT

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		resources.readFromNBT(compound);
		circuits.readFromNBT(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		resources.writeToNBT(compound);
		circuits.writeToNBT(compound);
	}

}
