package xk.xact.util;


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
