package xk.xact.gui;


import net.minecraft.src.*;


public abstract class ContainerMachine extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return true;
	}


	// if the stacks are the same, and if they can be stacked together.
	private boolean canMergeStacks(ItemStack stack1, ItemStack stack2){
		if( stack1 == null || stack2 == null )
			return false;

		if( stack1.itemID != stack2.itemID )
			return false;

		if( stack1.getHasSubtypes() )
			if( stack1.getItemDamage() != stack2.getItemDamage() )
				return false;

		return ItemStack.areItemStackTagsEqual(stack1, stack2); // func_77970_a
	}

	@Override
	public abstract ItemStack transferStackInSlot(EntityPlayer player, int slot);

	@Override
    protected boolean mergeItemStack(ItemStack itemStack, int firstSlot, int lastSlot, boolean reverseOrder) {
        boolean retValue = false;
        int currentSlot = firstSlot;

        if (reverseOrder)
            currentSlot = lastSlot - 1;

        Slot slot;
        ItemStack stackInSlot;


        // First try to merge the stack.

        if (itemStack.isStackable()) {
            while (itemStack.stackSize > 0 && (!reverseOrder && currentSlot < lastSlot || reverseOrder && currentSlot >= firstSlot)){
                slot = (Slot)this.inventorySlots.get(currentSlot);
                stackInSlot = slot.getStack();

                if (stackInSlot != null && stackInSlot.itemID == itemStack.itemID && (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemStack, stackInSlot)){
                    int totalSize = stackInSlot.stackSize + itemStack.stackSize;

                    if (totalSize <= itemStack.getMaxStackSize()) {
                        itemStack.stackSize = 0;
                        stackInSlot.stackSize = totalSize;
                        slot.onSlotChanged();
                        retValue = true;
                    } else if (stackInSlot.stackSize < itemStack.getMaxStackSize()) {
                        itemStack.stackSize -= itemStack.getMaxStackSize() - stackInSlot.stackSize;
                        stackInSlot.stackSize = itemStack.getMaxStackSize();
                        slot.onSlotChanged();
                        retValue = true;
                    }
                }

                if (reverseOrder) {
                    --currentSlot;
                } else {
                    ++currentSlot;
                }
            }
        }

        if (itemStack.stackSize > 0) {
            if (reverseOrder) {
                currentSlot = lastSlot - 1;
            } else {
                currentSlot = firstSlot;
            }

            while (!reverseOrder && currentSlot < lastSlot || reverseOrder && currentSlot >= firstSlot) {
                slot = (Slot)this.inventorySlots.get(currentSlot);
                stackInSlot = slot.getStack();

                if (stackInSlot == null) {
                    slot.putStack(itemStack.copy());
                    slot.onSlotChanged();
                    itemStack.stackSize = 0;
                    retValue = true;
                    break;
                }

                if (reverseOrder) {
                    --currentSlot;
                } else {
                    ++currentSlot;
                }
            }
        }

        return retValue;
    }
}
