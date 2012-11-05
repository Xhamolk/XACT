package xk.xact.gui;


import net.minecraft.src.*;


public abstract class ContainerMachine extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return true;
	}

	// returns the stack on the slot before the operation.
		// modified so it checks if SpecialSlot allows the extraction.
	@Override
	public ItemStack slotClick(int slotID, int mouseButton, int flag, EntityPlayer player) {
		ItemStack retStack = null;

		if( mouseButton < 0 || mouseButton > 1 )
			return null;

		InventoryPlayer inventoryPlayer = player.inventory;

		if (slotID == -999) { // clicking out of the GUI.
			if (inventoryPlayer.getItemStack() != null) {
				if (mouseButton == 0) {
					player.dropPlayerItem(inventoryPlayer.getItemStack());
					inventoryPlayer.setItemStack(null);
				}

				if (mouseButton == 1) {
					player.dropPlayerItem(inventoryPlayer.getItemStack().splitStack(1));

					if (inventoryPlayer.getItemStack().stackSize == 0) {
						inventoryPlayer.setItemStack(null);
					}
				}
			}
			return null;
		}
		if (flag == 1) {
			ItemStack transferStack = this.transferStackInSlot(player, slotID);

			if (transferStack != null) {
				int itemID = transferStack.itemID;
				retStack = transferStack.copy();
				Slot slot = (Slot)this.inventorySlots.get(slotID);

				if (slot != null && slot.getStack() != null && slot.getStack().itemID == itemID) {
					this.retrySlotClick(slotID, mouseButton, true, player);
				}
			}
			return retStack;
		}


		Slot slot;
		
		if( slotID < 0 || (slot = (Slot)this.inventorySlots.get(slotID)) == null )
			return null;

		ItemStack slotStack = slot.getStack();
		ItemStack playerStack = inventoryPlayer.getItemStack();

		retStack = (slotStack == null) ? null : slotStack.copy();

		int amount;
		if (slotStack == null) {
			// Places the player's stack into the slot. (requires valid item in hand)
			if (playerStack != null && slot.isItemValid(playerStack)) {
				// on left click, place it all. on right click, place only one item.
				amount = mouseButton == 0 ? playerStack.stackSize : 1;

				if (amount > slot.getSlotStackLimit()) {
					amount = slot.getSlotStackLimit();
				}

				slot.putStack(playerStack.splitStack(amount));

				if (playerStack.stackSize == 0) {
					inventoryPlayer.setItemStack(null);
				}
				slot.onSlotChanged();
			}
			return null;
		}

		if ( playerStack == null ) { // clicking with nothing selected. (Extracting)
			if( slot instanceof SpecialSlot ) {
				if( !((SpecialSlot) slot).allowPickUp() )
					return null;
			}

			amount = (mouseButton == 0) ? slotStack.stackSize : (slotStack.stackSize + 1) / 2;

			if( slot instanceof SlotCraftResult ){
			    // do not extract.
				inventoryPlayer.setItemStack(slotStack.copy());
				slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());
			} else {
				ItemStack tempStack = slot.decrStackSize(amount);
				inventoryPlayer.setItemStack(tempStack);

				if (slotStack.stackSize == 0) {
					slot.putStack(null);
				}
			}
			
			slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());
			slot.onSlotChanged();
			return retStack;
		}

		if( slot.isItemValid(playerStack) ) { // adding player's stack to the slot. (Inserting)

			if( canMergeStacks(slotStack, playerStack) ) { // if can merge.
				amount = mouseButton == 0 ? playerStack.stackSize : 1;

				if (amount > slot.getSlotStackLimit() - slotStack.stackSize) {
					amount = slot.getSlotStackLimit() - slotStack.stackSize;
				}

				if (amount > playerStack.getMaxStackSize() - slotStack.stackSize) {
					amount = playerStack.getMaxStackSize() - slotStack.stackSize;
				}

				playerStack.splitStack(amount);

				if (playerStack.stackSize == 0)
					inventoryPlayer.setItemStack(null);

				slotStack.stackSize += amount;
			} else  if (playerStack.stackSize <= slot.getSlotStackLimit()) { // can't merge, so swaps stacks.
				slot.putStack(playerStack);
				inventoryPlayer.setItemStack(slotStack);
			}

			slot.onSlotChanged();
			return retStack;
		}

		// extract and merge to player's hand
		if( canMergeStacks(slotStack, playerStack) && playerStack.getMaxStackSize() > 1 ) {
			amount = slotStack.stackSize;

			if (amount > 0 && amount + playerStack.stackSize <= playerStack.getMaxStackSize()) {
				if( slot instanceof SpecialSlot ) {
					if( !((SpecialSlot) slot).allowPickUp() )
						return null;
				}

				playerStack.stackSize += amount;
				if (!(slot instanceof SlotCraftResult)) {
					slotStack = slot.decrStackSize(amount);

					if (slotStack != null && slotStack.stackSize == 0) {
						slot.putStack(null);
					}
				}

				slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());
				slot.onSlotChanged();
			}
//			return retStack;
		}

		return retStack;
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
    protected boolean mergeItemStack(ItemStack itemStack, int firstSlot, int lastSlot, boolean reverseOrder)
    {
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
