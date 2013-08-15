package xk.xact.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

/**
 * Adapter used to hide all the inaccessible slots from the original ISidedInventory.
 *
 * @author Xhamolk_
 */
public class SidedInventory implements IInventory, ISidedInventory {

	private ISidedInventory inv;
	private int[] slots;
	private int side; // as expected by vanilla ISided.


	public SidedInventory(ISidedInventory inventory, ForgeDirection side) {
		this.inv = inventory;
		this.side = side.ordinal();
		this.slots = inventory.getAccessibleSlotsFromSide( side.ordinal() );
	}

	// ----- IInventory -----

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot( slots[i] );
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return inv.decrStackSize( slots[i], j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return inv.getStackInSlotOnClosing( slots[i] );
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents( slots[i], itemstack );
	}

	@Override
	public String getInvName() {
		return inv.getInvName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return inv.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged() {
		inv.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return inv.isUseableByPlayer( player );
	}

	@Override
	public void openChest() {
		inv.openChest();
	}

	@Override
	public void closeChest() {
		inv.closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return inv.isItemValidForSlot( slots[i], itemstack );
	}

	// ----- ISided Inventory -----

	// The "available" slots for this inventory.
	// Should be an array of integers from 0 to slots.length-1
	private int[] availableSlots = null;

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		if( availableSlots == null ) {
			availableSlots = new int[slots.length];
			for( int i = 0; i < slots.length; i++ ) {
				availableSlots[i] = i;
			}
		}
		return availableSlots;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		return inv.canInsertItem( slots[i], itemstack, side );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return inv.canExtractItem( slots[i], itemstack, side );
	}

}
