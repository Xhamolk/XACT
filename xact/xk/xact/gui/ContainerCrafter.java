package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.core.ItemChip;
import xk.xact.core.TileCrafter;
import xk.xact.recipes.CraftManager;


/**
 * The container used for the Crafter's GUI.
 */
public class ContainerCrafter extends ContainerMachine implements InteractiveCraftingContainer {

	TileCrafter crafter;
	
	private EntityPlayer player;

	public ContainerCrafter(TileCrafter crafter, EntityPlayer player) {
		this.crafter = crafter;
		this.player = player;
		buildContainer();
	}

	private void buildContainer() {
		// craft results
		for(int i=0; i<4; i++) {
			int x = 20 + (i % 2) * 120;
			int y = 20 + (i / 2) * 44;
			addSlotToContainer(new SlotCraft(crafter, crafter.results, player, i, x, y));
		}

		// circuits
		for(int i=0; i<4; i++) {
			int x = 20 + (i % 2) * 120;
			int y = 40 + (i / 2) * 44;

			addSlotToContainer(new Slot(crafter.circuits, i, x, y) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return CraftManager.isValid(stack);
				}
				@Override
				public int getSlotStackLimit() {
					return 1;
				}
			});
		}

		// crafting grid (62,17) 3x3 (18x18)
		for(int i=0; i<3; i++) {
			for(int e=0; e<3; e++){
				int x = 18*e + 62, y = 18*i + 17, index = e + i*3;
				addSlotToContainer(new Slot(crafter.craftGrid, index, x, y));
			}
		}

		// grid's output (80,78)
		addSlotToContainer(new SlotCraft(crafter, crafter.results, player, 4, 80, 78));


		// resources (8,107) 3x9 (18x18)
		for(int i=0; i<3; i++){
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 107;
				addSlotToContainer(new Slot(crafter.resources, e + i*9, x, y));
			}
		}
		
		// player's inventory (8,174) 3x9 (18x18)
		for(int i=0; i<3; i++) {
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 174;
				addSlotToContainer(new Slot(player.inventory, e + i*9 + 9, x, y));
			}
		}
		// player's hot bar (8,232) 1x9 (18x18)
		for(int i=0; i<9; i++){
			addSlotToContainer(new Slot(player.inventory, i, 18*i + 8, 232));
		}
		
	}


	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID){
		Slot slot = (Slot) inventorySlots.get(slotID);

		if( slot == null || !slot.getHasStack() )
			return null;
		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		if( slot instanceof SlotCraft ) {
			// add to the resources buffer.
			stackInSlot = ((SlotCraft)slot).getCraftedStack();
			ItemStack copy = stackInSlot == null ? null : stackInSlot.copy();

			if( mergeCraftedStack(stackInSlot, 8+10, 8+10+27) ) {
				slot.onPickupFromSlot(player, copy);
				slot.onSlotChanged();
				return copy;
			}
			return null;
		}

		// From the crafter to the resources buffer.
		if( slotID < 8 ) {
			if (!mergeItemStack(stackInSlot, 8+10, 8+10+27, false))
				return null;
			
		} else if( slotID < 8+10 ) { // from the crafting grid.
			if (!mergeItemStack(stackInSlot, 8+10, inventorySlots.size(), false))
				return null;

		} else if( slotID < 8+10+27 ) { // from the resources buffer
			// chips first try to go to the chip slots.
			if( stackInSlot.getItem() instanceof ItemChip ) {
				if (!mergeItemStack(stackInSlot, 4, 8, false)) // try add to the chip slots.
					if (!mergeItemStack(stackInSlot, 8+10+27, inventorySlots.size(), false)) // add to the player's inv.
						return null;

				// prevent retrying by returning null.
				stack = null;

			} else { // any other item goes to the player's inventory.
				if (!mergeItemStack(stackInSlot, 8+10+27, inventorySlots.size(), false))
					return null;
			}

		} else { // From the player's inventory to the resources buffer.
			if (!mergeItemStack(stackInSlot, 8+10, 8+10+27, false))
				return null;
		}

		if ( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		}
        slot.onSlotChanged();

		return stack;
	}

	@Override
	public ItemStack slotClick(int slotID, int buttomPressed, int flag, EntityPlayer player) {
		InventoryPlayer inventoryPlayer = player.inventory;

		// clicking out of the GUI. drop stuff.
		if (slotID == -999) {
			if ( inventoryPlayer.getItemStack() != null ) {
				if (buttomPressed == 0) {
					player.dropPlayerItem(inventoryPlayer.getItemStack());
					inventoryPlayer.setItemStack(null);
				}
				if (buttomPressed == 1) {
					player.dropPlayerItem(inventoryPlayer.getItemStack().splitStack(1));
					if (inventoryPlayer.getItemStack().stackSize == 0) {
						inventoryPlayer.setItemStack(null);
					}
				}
			}
			return null;
		}

		Slot slot = ((Slot)this.inventorySlots.get(slotID));
		if( slot == null )
			return null;
		boolean craftingSlot = slot instanceof SlotCraft;

		ItemStack stackInSlot = slot.getStack(),
				playerStack = inventoryPlayer.getItemStack(),
				retValue = null;

		// Special handling for the grid slots
		if( 8 <= slotID && slotID < 17 ) {

			if( flag == 0 ) { // regular clicking.
				if( buttomPressed == 0 || playerStack == null ){
					slot.putStack(null);
				} else if( buttomPressed == 1 ){
					ItemStack copy = playerStack.copy();
					copy.stackSize = 1;
					slot.putStack( copy );
				}
				return null;
			}

			if( flag == 1 ) {
				slot.putStack( null );
				return null; // clear on shift-clicking.
			}

			if( flag == 2 ) { // interact with the hot-bar
				ItemStack invStack = player.inventory.getStackInSlot(buttomPressed);
				if( invStack != null ) {
					ItemStack copy = invStack.copy();
					copy.stackSize = 1;

					slot.putStack(copy);
					return invStack;
				}
			}
			return null;
		}

		// Default behaviour
		if( flag == 0 && (buttomPressed == 0 || buttomPressed == 1) ) {

			if( stackInSlot == null ) { // Placing player's stack on empty slot.

				if( playerStack != null && slot.isItemValid(playerStack) ) {
					int amount = buttomPressed == 0 ? playerStack.stackSize : 1;

					if (amount > slot.getSlotStackLimit()) {
						amount = slot.getSlotStackLimit();
					}

					slot.putStack(playerStack.splitStack(amount));

					if (playerStack.stackSize == 0) {
						inventoryPlayer.setItemStack(null);
					}
				}

			} else if( slot.canTakeStack(player) ) {  // interact with the slot.

				if( playerStack == null ) { // Full extraction from slot.
					int amount = buttomPressed == 0 || craftingSlot ? stackInSlot.stackSize : (stackInSlot.stackSize + 1) / 2;

					ItemStack itemStack = craftingSlot ?
							((SlotCraft)slot).getCraftedStack() : slot.decrStackSize(amount);

					inventoryPlayer.setItemStack( itemStack );

					if( stackInSlot.stackSize == 0 )
						slot.putStack(null);

					slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());

				} else if( slot.isItemValid(playerStack) ) { // Merge to slot

					if( equalsStacks(stackInSlot, playerStack) ) { // split player's into slot.
						int amount = buttomPressed == 0 ? playerStack.stackSize : 1;

						int max = Math.min( slot.getSlotStackLimit() - stackInSlot.stackSize, playerStack.getMaxStackSize() - stackInSlot.stackSize);
						if( amount > max )
							amount = max;

						playerStack.splitStack(amount);
						stackInSlot.stackSize += amount;

						if (playerStack.stackSize == 0) {
							inventoryPlayer.setItemStack(null);
						}

					} else if( playerStack.stackSize <= slot.getSlotStackLimit() ) { // swap stacks.
						slot.putStack(playerStack);
						inventoryPlayer.setItemStack(stackInSlot);
					}

				} else if (equalsStacks(stackInSlot, playerStack) && playerStack.getMaxStackSize() > 1) { // extract some
					if( craftingSlot )
						stackInSlot = ((SlotCraft)slot).getCraftedStack();
					int amount = stackInSlot.stackSize;

					if (amount > 0 && amount + playerStack.stackSize <= playerStack.getMaxStackSize()) {
						playerStack.stackSize += amount;
						if( !craftingSlot )
							stackInSlot = slot.decrStackSize(amount);

						if (stackInSlot.stackSize == 0) {
							slot.putStack(null);
						}

						slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());
					}
				}
			}

			slot.onSlotChanged();

		}
		// Shift-clicking
		else if( flag == 1 && (buttomPressed == 0 || buttomPressed == 1) ) {
			if (slot != null && slot.canTakeStack(player)) {
				ItemStack stack = this.transferStackInSlot(player, slotID);

				if (stack != null) {
					retValue = stack.copy();

					if( craftingSlot || ( slot.getStack() != null && slot.getStack().itemID == stack.itemID) ) {
						this.retrySlotClick(slotID, buttomPressed, true, player);
					}
				}
				return retValue;
			}
		}
		// Interacting with the hot bar.
		else if( flag == 2 ) {
			if (slot.canTakeStack(player)) {
				ItemStack itemStack = inventoryPlayer.getStackInSlot(buttomPressed);
				boolean var9 = itemStack == null || slot.inventory == inventoryPlayer && slot.isItemValid(itemStack);
				int index = -1;

				if (!var9) {
					index = inventoryPlayer.getFirstEmptyStack();
					var9 = index > -1;
				}

				if (slot.getHasStack() && var9) {
					ItemStack slotStack = craftingSlot ? ((SlotCraft)slot).getCraftedStack() : slot.getStack();
					inventoryPlayer.setInventorySlotContents(buttomPressed, slotStack);

					if ((slot.inventory != inventoryPlayer || !slot.isItemValid(itemStack)) && itemStack != null) {
						if (index > -1) {
							inventoryPlayer.addItemStackToInventory(itemStack);
							if( !craftingSlot )
								slot.putStack(null);
							slot.onPickupFromSlot(player, slotStack);
						}
					} else {
						slot.putStack(itemStack);
						slot.onPickupFromSlot(player, slotStack);
					}
				}
				else if (!slot.getHasStack() && itemStack != null && slot.isItemValid(itemStack))
				{
					inventoryPlayer.setInventorySlotContents(buttomPressed, null);
					slot.putStack(itemStack);
				}
			}
		}

		return null;
	}

	protected boolean mergeCraftedStack(ItemStack itemStack, int indexMin, int indexMax) {

		// First, check if the stack can fit.
		int missingSpace = itemStack.stackSize;
		int emptySlots = 0;

		for( int i = indexMin; i < indexMax && missingSpace > 0; i++ ) {
			Slot tempSlot = (Slot) this.inventorySlots.get(i);
			ItemStack stackInSlot = tempSlot.getStack();

			if( stackInSlot == null ) {
				emptySlots++;
				continue;
			}

			if( stackInSlot.itemID == itemStack.itemID
					&& (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage())
					&& ItemStack.areItemStackTagsEqual(itemStack, stackInSlot)) {

				missingSpace -= Math.min(stackInSlot.getMaxStackSize(), tempSlot.getSlotStackLimit()) - stackInSlot.stackSize;
			}
		}

		// prevent crafting if there is no space for the crafted item.
		if( missingSpace > 0 )
			if( emptySlots == 0 )
				return false;

		// Try to merge with existing stacks.
		if( itemStack.isStackable() ) {

			for( int i = indexMin; i < indexMax; i++ ) {
				if( itemStack.stackSize <= 0 )
					break;

				Slot targetSlot = (Slot)this.inventorySlots.get(i);
				ItemStack stackInSlot = targetSlot.getStack();

				if( stackInSlot == null )
					continue;

				if( stackInSlot.itemID == itemStack.itemID
						&& (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage())
						&& ItemStack.areItemStackTagsEqual(itemStack, stackInSlot)) {

					int sum = itemStack.stackSize + stackInSlot.stackSize;
					int maxStackSize = Math.min(stackInSlot.getMaxStackSize(), targetSlot.getSlotStackLimit());

					if( sum <= maxStackSize ) {
						stackInSlot.stackSize = sum;
						targetSlot.onSlotChanged();
						return true;
					}
					else if( stackInSlot.stackSize < maxStackSize  ) {
						itemStack.stackSize -= maxStackSize - stackInSlot.stackSize;
						stackInSlot.stackSize = maxStackSize;
						targetSlot.onSlotChanged();
					}
				}
			}
		}

		// Add to an empty slot.
		if (itemStack.stackSize > 0) {

			for( int i = indexMin; i < indexMax; i++ ) {

				Slot targetSlot = (Slot)this.inventorySlots.get(i);
				ItemStack stackInSlot = targetSlot.getStack();

				if( stackInSlot != null )
					continue;

				targetSlot.putStack( itemStack );
				targetSlot.onSlotChanged();
				return true;
			}
		}

		return true;
	}

	@Override
	protected boolean mergeItemStack(ItemStack itemStack, int indexMin, int indexMax, boolean reverse) {
		boolean retValue = false;
		int index = indexMin;

		if (reverse) {
			index = indexMax - 1;
		}

		Slot slot;
		ItemStack stackInSlot;

		if( itemStack.isStackable() ) {
			while( itemStack.stackSize > 0 && (!reverse && index < indexMax || reverse && index >= indexMin) ) {
				slot = (Slot)this.inventorySlots.get( index );
				stackInSlot = slot.getStack();

				int maxStackSize = Math.min(itemStack.getMaxStackSize(), slot.getSlotStackLimit());

				if( stackInSlot != null && stackInSlot.itemID == itemStack.itemID
						&& (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage())
						&& ItemStack.areItemStackTagsEqual(itemStack, stackInSlot) ) {

					int sum = stackInSlot.stackSize + itemStack.stackSize;

					if( sum <= maxStackSize ) {
						itemStack.stackSize = 0;
						stackInSlot.stackSize = sum;
						slot.onSlotChanged();
						retValue = true;
					} else if( stackInSlot.stackSize < maxStackSize ) {
						itemStack.stackSize -= maxStackSize - stackInSlot.stackSize;
						stackInSlot.stackSize = maxStackSize;
						slot.onSlotChanged();
						retValue = true;
					}
				}

				if( reverse ) {
					--index;
				} else {
					++index;
				}
			}
		}

		if( itemStack.stackSize > 0 ) {
			if( reverse ) {
				index = indexMax - 1;
			} else {
				index = indexMin;
			}

			while( !reverse && index < indexMax || reverse && index >= indexMin ) {
				slot = (Slot)this.inventorySlots.get(index);
				stackInSlot = slot.getStack();
				int maxStackSize = Math.min(itemStack.getMaxStackSize(), slot.getSlotStackLimit());

				if( stackInSlot == null ) {
					int remaining = 0;
					ItemStack tempStack = itemStack;

					if( itemStack.stackSize > maxStackSize ) {
						remaining = itemStack.stackSize - maxStackSize;
						tempStack = itemStack.splitStack( maxStackSize );
					}

					slot.putStack(tempStack.copy());
					slot.onSlotChanged();
					itemStack.stackSize = remaining;
					retValue = true;
					break;
				}

				if( reverse ) {
					--index;
				} else {
					++index;
				}
			}
		}

		return retValue;
	}

	@Override
	protected void retrySlotClick(int slotID, int mouseClick, boolean flag, EntityPlayer player) {
		Slot slot = (Slot)this.inventorySlots.get(slotID);
		if( slot instanceof SlotCraft ) {
			if( mouseClick == 1 )
				return;
		}
		this.slotClick(slotID, mouseClick, 1, player);
	}

	private boolean equalsStacks( ItemStack stack1, ItemStack stack2 ){
		return stack1.itemID == stack2.itemID
				&& (!stack1.getHasSubtypes() || stack1.getItemDamage() == stack2.getItemDamage())
				&& ItemStack.areItemStackTagsEqual(stack1, stack2);
	}


	// InteractiveCraftingContainer
	@Override
	public void setStack(int slotID, ItemStack stack) {
		if( slotID == -1 ) { // Clear the grid
			for( int i = 0; i < 9; i++ ) {
				Slot slot = (Slot) this.inventorySlots.get(i +8);
				slot.putStack( null );
			}
			return;
		}

		Slot slot = (Slot) this.inventorySlots.get(slotID);

		if( slot != null ) {
			slot.putStack(stack);
		}
	}

}
