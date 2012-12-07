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
			stackInSlot = ((SlotCraft)slot).getCraftedStack(player);
			ItemStack copy = stackInSlot == null ? null : stackInSlot.copy();

			if ( mergeItemStack(stackInSlot, 8, 8+18, false) ) {
				slot.onPickupFromSlot(player, copy);
				slot.onSlotChanged();
				return copy;
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
							((SlotCraft)slot).getCraftedStack(player) : slot.decrStackSize(amount);

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
					stackInSlot = ((SlotCraft)slot).getCraftedStack(player);
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
					ItemStack slotStack = craftingSlot ? ((SlotCraft)slot).getCraftedStack(player) : slot.getStack();
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
