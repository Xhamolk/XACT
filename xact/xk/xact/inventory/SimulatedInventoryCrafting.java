package xk.xact.inventory;


import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

/**
 * Necessary for GUIs that involve crafting grids.
 * <p/>
 * Wraps an IInventory instance.
 */
class SimulatedInventoryCrafting extends InventoryCrafting {

	private IInventory inventory;
	private Container container;

	public SimulatedInventoryCrafting(Container container, IInventory inventory) {
		super( container, 3, 3 );
		this.container = container;
		this.inventory = inventory;
	}

	@Override
	public int getSizeInventory() {
		return 9;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.getStackInSlot( slot );
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack item = inventory.decrStackSize( slot, amount );
		if( item != null ) {
			updateContainer();
		}
		return item;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inventory.getStackInSlotOnClosing( slot );
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		inventory.setInventorySlotContents( slot, itemstack );
		updateContainer();
	}

	@Override
	public String getInvName() {
		return inventory.getInvName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return inventory.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged() {
		inventory.onInventoryChanged();
	}

	private void updateContainer() {
		if( container != null )
			container.onCraftMatrixChanged( this );
	}

	/**
	 * Fills the crafting grid with the specified ingredients.
	 * Note: fill order is right then down.
	 *
	 * @param ingredients a ItemStack[9] containing all the ingredients.
	 */
	public void setContents(ItemStack[] ingredients) {
		for( int slot = 0; slot < 9; slot++ ) {
			inventory.setInventorySlotContents( slot, ingredients.length > slot ? ingredients[slot] : null );
		}
		updateContainer();
	}

	/**
	 * Clears the crafting grid.
	 */
	public void cleanContents() {
		for( int slot = 0; slot < 9; slot++ ) {
			inventory.setInventorySlotContents( slot, null );
		}
		updateContainer();
	}

	/**
	 * Gets the contents in this inventory
	 */
	public ItemStack[] getContents() {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for( int slot = 0; slot < 9; slot++ ) {
			list.add( this.getStackInSlot( slot ) );
		}
		return list.toArray( new ItemStack[list.size()] );
	}

}
