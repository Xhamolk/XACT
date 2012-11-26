package xk.xact.core;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.Inventory;

import java.util.ArrayList;

/**
 * 
 * @author Xhamolk_
 */
public class TileCrafter extends TileMachine implements IInventory, ICraftingDevice {

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
		this.results = new Inventory(4, "Results") {
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
		CraftRecipe recipe = this.getRecipe(slot);
		return recipe == null ? null : recipe.getResult();
	}

	// updates the stored recipe results.
	public void updateRecipes() {
		for(int i=0; i<4; i++) {
			ItemStack stack = this.circuits.getStackInSlot(i);
			if( stack == null )
				recipes[i] = null;
			else
				recipes[i] = CraftManager.decodeRecipe( stack );
		}

		for(int i=0; i<4; i++) {
			ItemStack stack = recipes[i] == null ? null : recipes[i].getResult();
			results.setInventorySlotContents(i, stack);
		}
	}

	public void updateStates() {
		for(int i=0;i<4; i++) {
			// if there are not enough ingredients, color is red.
			redState[i] = (recipes[i] != null) && !getHandler().canCraft(this.getRecipe(i));
		}
	}

	///////////////
	///// ICraftingDevice

	/**
	 * Gets all the available inventories.
	 * In other words, all the inventories from which this crafter can pull resources.
	 * Adjacent chests (except on the top) will be included.
	 *
	 * @return an array of all the available IInventories.
	 */
	public IInventory[] getAvailableInventories() {
		return new Inventory[] {resources}; // todo: add adjacent chests.
	}

	@Override
	public int getRecipeCount() {
		return 4;
	}

	@Override
	public CraftRecipe getRecipe(int index) {
		if( index < 0 || index > 4 )
			return null;
		if( recipes[index] == null ) {
			ItemStack stack = this.circuits.getStackInSlot(index);
			if( stack != null )
				recipes[index] = CraftManager.decodeRecipe(stack);
		}
		return recipes[index];
	}


	private CraftingHandler handler;

	@Override
	public CraftingHandler getHandler() {
		if( handler == null )
			handler = CraftingHandler.createCraftingHandler(this);
		return handler;
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
