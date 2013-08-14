package xk.xact.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Used to access multiple inventories as if they were one.
 *
 * @author Xhamolk_
 */
public class JointInventory implements IInventory {

	private String name;
	private IInventory[] inventories;
	private int[] inventorySizes;
	private final int totalSize;

	/**
	 * Creates a new JointInventory.
	 * <p/>
	 * The order of the arguments matter.
	 *
	 * @param name the name for this compound inventory.
	 * @param inventories all the inventories that will take part of this inventory.
	 */
	public JointInventory(String name, IInventory... inventories) {
		this.name = name;
		this.inventories = inventories;
		this.inventorySizes = new int[inventories.length];
		int sum = 0;
		for( int i = 0; i < inventories.length; i++ ) {
			if( inventories[i] != null ) {
				inventorySizes[i] = inventories[i].getSizeInventory();
				sum += inventorySizes[i];
			} else {
				inventorySizes[i] = 0;
			}
		}
		this.totalSize = sum;
	}

	private int findIndex(int index) {
		if( index < 0 || index >= totalSize )
			return -1;

		int i = 0; // array index
		int sum = 0; // sum of slots.
		for( int s : inventorySizes ) {
			if( sum + s > index )
				return i;
			sum += s;
			i++;
		}
		return -1;
	}

	private IInventory getInventoryFromIndex(int index) {
		int find = findIndex( index );
		if( find == -1 ) {
			return null;
		} else {
			return inventories[find];
		}
	}

	private int getSlotFromIndex(int index) {
		int find = findIndex( index );
		if( find == -1 )
			return -1;

		int sum = 0;
		for( int i = 0; i < find; i++ ) {
			sum += inventorySizes[i];
		}
		return index - sum;
	}

	private boolean validIndex(int index) {
		return index >= 0 && index < totalSize;
	}

	@Override
	public int getSizeInventory() {
		return totalSize;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if( !validIndex( i ))
			return null;
		IInventory inv = getInventoryFromIndex( i );
		int slot = getSlotFromIndex( i );
		return inv.getStackInSlot( slot );
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if( !validIndex( i ))
			return null;
		IInventory inv = getInventoryFromIndex( i );
		int slot = getSlotFromIndex( i );
		return inv.decrStackSize( slot, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if( !validIndex( i ))
			return null;
		IInventory inv = getInventoryFromIndex( i );
		int slot = getSlotFromIndex( i );
		return inv.getStackInSlotOnClosing( slot );
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if( !validIndex( i ))
			return;
		IInventory inv = getInventoryFromIndex( i );
		int slot = getSlotFromIndex( i );
		inv.setInventorySlotContents( slot, itemstack );
	}

	@Override
	public String getInvName() {
		return name;
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {
		for(IInventory inv : inventories) {
			inv.onInventoryChanged();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if( !validIndex( i ))
			return false;
		IInventory inv = getInventoryFromIndex( i );
		int slot = getSlotFromIndex( i );
		return inv.isItemValidForSlot( slot, itemstack );
	}

}
