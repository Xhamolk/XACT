package xk.xact.inventory;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Iterator;

// used by InventoryUtils.inventoryIterator(IInventory)
public class InvSlotIterator implements Iterable<InvSlot>, Iterator<InvSlot> {

	private IInventory inv;
	private int size;

	int currentSlot = -1;

	protected InvSlotIterator(IInventory inventory) {
		this.inv = inventory;
		this.size = inventory.getSizeInventory();
	}

	/**
	 * Generates an Iterator for the specified IInventory.
	 * This is helpful to iterate through every slot on the inventory.
	 *
	 * @param inventory the inventory that holds the items.
	 * @return an iterable representation of the IInventory.
	 */
	public static InvSlotIterator createNewFor(IInventory inventory) {
		if( inventory != null )
			return new InvSlotIterator( inventory );
		return null;
	}

	@Override
	public Iterator<InvSlot> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return (currentSlot - 1 < size);
	}

	@Override
	public InvSlot next() {
		if( ++currentSlot < size ) {
			ItemStack stack = inv.getStackInSlot( currentSlot );
			return new InvSlot( currentSlot, stack );
		}
		return null;
	}

	@Override
	public void remove() {
	} // do nothing.

}
