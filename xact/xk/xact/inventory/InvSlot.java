package xk.xact.inventory;

import net.minecraft.item.ItemStack;


/**
 * Represents a slot on an inventory.
 * Provides it's index and it's contents.
 * <p/>
 * Mainly used by SlotIterator and InvSlotIterator
 */
public class InvSlot {

	public int slotIndex = -1;
	public ItemStack stack = null;

	public InvSlot(int index, ItemStack stack) {
		this.slotIndex = index;
		this.stack = stack;
	}

	public boolean isEmpty() {
		return this.stack == null;
	}

	public boolean isFull() {
		return !isEmpty() && this.stack.stackSize == this.stack.getMaxStackSize();
	}

	public boolean containsItemsFrom(ItemStack otherStack) {
		return InventoryUtils.similarStacks( this.stack, otherStack, true );
	}

	public int getSpaceFor(ItemStack otherStack) {
		if( isEmpty() )
			return 64;
		if( isFull() )
			return 0;
		return InventoryUtils.getSpaceInStackFor( this.stack, otherStack );
	}


}
