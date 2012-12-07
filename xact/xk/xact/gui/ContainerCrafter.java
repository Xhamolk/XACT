package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.core.ItemChip;
import xk.xact.core.TileCrafter;
import xk.xact.recipes.CraftManager;


/**
 * The container used for the Crafter's GUI.
 */
public class ContainerCrafter extends ContainerMachine {

	private TileCrafter crafter;
	
	private EntityPlayer player;

	public ContainerCrafter(TileCrafter crafter, EntityPlayer player) {
		this.crafter = crafter;
		this.player = player;
		buildContainer();
	}


	private void buildContainer() {
		// craft results
		for(int i=0; i<4; i++) {
			addSlotToContainer(new SlotCraft(crafter, crafter.results, i, 26 + 36*i, 25));
		}

		// circuits
		for(int i=0; i<4; i++){
			addSlotToContainer(new Slot(crafter.circuits, i, 26 + 36*i, 47) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return CraftManager.isEncoded(stack);
				}
				@Override
				public int getSlotStackLimit() {
					return 1;
				}
			});
		}

		// resources
		for(int i=0; i<2; i++){
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 71;
				addSlotToContainer(new Slot(crafter.resources, e + i*9, x, y));
			}
		}
		
		// player's inventory
		for(int i=0; i<3; i++) {
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 117;
				addSlotToContainer(new Slot(player.inventory, e + i*9 + 9, x, y));
			}
		}
		// player's hot bar
		for(int i=0; i<9; i++){
			addSlotToContainer(new Slot(player.inventory, i, 18*i + 8, 175));
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
			if (mergeItemStack(stackInSlot, 8, 8+18, false)) {
				slot.onPickupFromSlot(player, stack);
				slot.onSlotChanged();
				return stack;
			}

			return null;
		}

		// From the crafter to the resources buffer.
		if( slotID < 8 ) {
			if (!mergeItemStack(stackInSlot, 8, 18+8, false))
				return null;
			
		} else if( slotID < 8+18 ){ // from the resources buffer
			// chips first try to go to the chip slots.
			if( stackInSlot.getItem() instanceof ItemChip){
				if (!mergeItemStack(stackInSlot, 4, 8, false)) // try add to the chip slots.
					if (!mergeItemStack(stackInSlot, 8+18, inventorySlots.size(), false)) // add to the player's inv.
						return null;

			} else { // any other item goes to the player's inventory.
				if (!mergeItemStack(stackInSlot, 8+18, inventorySlots.size(), false))
					return null;
			}

		} else { // From the player's inventory to the resources buffer.
			if (!mergeItemStack(stackInSlot, 8, 8+18, false))
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
		ItemStack retValue = null;
		InventoryPlayer inventoryPlayer = player.inventory;
		Slot slot;
		ItemStack stackInSlot;
		int amount;

		if ((flag == 0 || flag == 1) && (buttomPressed == 0 || buttomPressed == 1)) { // normal actions.
			if (slotID == -999) { // out of the GUI.
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
			} else if (flag == 1) { // pressing shift
				slot = (Slot)this.inventorySlots.get(slotID);

				if (slot != null && slot.canTakeStack(player)) {
					stackInSlot = this.transferStackInSlot(player, slotID);

					if (stackInSlot != null) {
						retValue = stackInSlot.copy();

						if ( slot.getStack() != null && slot.getStack().itemID == stackInSlot.itemID) {
							this.retrySlotClick(slotID, buttomPressed, true, player);
						}
					}
				}
			} else {
				if( slotID < 0 || (slot = (Slot)this.inventorySlots.get(slotID)) == null )
					return null;

				stackInSlot = slot.getStack();
				if( slot instanceof SlotCraft ) {
					stackInSlot = ((SlotCraft)slot).getCraftedStack(player);
					// todo: copy the slotClick method from ContainerPad
				}

				ItemStack playerStack = inventoryPlayer.getItemStack();

				if (stackInSlot != null) {
					retValue = stackInSlot.copy();
				}

				if (stackInSlot == null) { // Placing player's stack on empty slot.

					if (playerStack != null && slot.isItemValid(playerStack)) {
						amount = buttomPressed == 0 ? playerStack.stackSize : 1;

						if (amount > slot.getSlotStackLimit()) {
							amount = slot.getSlotStackLimit();
						}

						slot.putStack(playerStack.splitStack(amount));

						if (playerStack.stackSize == 0) {
							inventoryPlayer.setItemStack(null);
						}
					}

				} else if (slot.canTakeStack(player)) {  // interact with the slot.

					if (playerStack == null) { // Full extraction from slot.
						amount = buttomPressed == 0 ? stackInSlot.stackSize : (stackInSlot.stackSize + 1) / 2;
						inventoryPlayer.setItemStack( slot.decrStackSize(amount) );

						if (stackInSlot.stackSize == 0) {
							slot.putStack(null);
						}

						slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());

					} else if (slot.isItemValid(playerStack)) { // Merge to slot

						if (equalsStacks(stackInSlot, playerStack)) { // split player's into slot.
							amount = buttomPressed == 0 ? playerStack.stackSize : 1;

							int max = Math.min( slot.getSlotStackLimit() - stackInSlot.stackSize, playerStack.getMaxStackSize() - stackInSlot.stackSize);
							if( amount > max )
								amount = max;

							playerStack.splitStack(amount);
							stackInSlot.stackSize += amount;

							if (playerStack.stackSize == 0) {
								inventoryPlayer.setItemStack(null);
							}

						} else if (playerStack.stackSize <= slot.getSlotStackLimit()) { // swap stacks.
							slot.putStack(playerStack);
							inventoryPlayer.setItemStack(stackInSlot);
						}

					} else if (equalsStacks(stackInSlot, playerStack) && playerStack.getMaxStackSize() > 1) { // extract some
						amount = stackInSlot.stackSize;

						if (amount > 0 && amount + playerStack.stackSize <= playerStack.getMaxStackSize()) {
							playerStack.stackSize += amount;
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
		}
		else if (flag == 2 && buttomPressed >= 0 && buttomPressed < 9) { // Interact with the hot-bar.

			slot = (Slot)this.inventorySlots.get(slotID);

			if (slot.canTakeStack(player)) {
				ItemStack itemStack = inventoryPlayer.getStackInSlot(buttomPressed);
				boolean var9 = itemStack == null || slot.inventory == inventoryPlayer && slot.isItemValid(itemStack);
				int index = -1;

				if (!var9) {
					index = inventoryPlayer.getFirstEmptyStack();
					var9 = index > -1;
				}

				if (slot.getHasStack() && var9) {
					ItemStack slotStack = slot.getStack();
					inventoryPlayer.setInventorySlotContents(buttomPressed, slotStack);

					if ((slot.inventory != inventoryPlayer || !slot.isItemValid(itemStack)) && itemStack != null) {
						if (index > -1) {
							inventoryPlayer.addItemStackToInventory(itemStack);
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
		else if (flag == 3 && player.capabilities.isCreativeMode && inventoryPlayer.getItemStack() == null && slotID > 0) {
			// Shift-clicking on the creative inventory.

			slot = (Slot)this.inventorySlots.get(slotID);

			if (slot != null && slot.getHasStack())
			{
				stackInSlot = slot.getStack().copy();
				stackInSlot.stackSize = stackInSlot.getMaxStackSize();
				inventoryPlayer.setItemStack(stackInSlot);
			}
		}

		return retValue;
	}


	protected void retrySlotClick(int slotID, int mouseClick, boolean flag, EntityPlayer player) {
		Slot slot = (Slot)this.inventorySlots.get(slotID);
		if(slot instanceof SlotCraft) {
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

}
