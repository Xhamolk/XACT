package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.core.CraftPad;
import xk.xact.core.ItemChip;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

public class ContainerPad extends ContainerItem implements InteractiveCraftingContainer {

	public CraftPad craftPad;

	EntityPlayer player;

	public boolean contentsChanged = false;

	public ContainerPad(CraftPad pad, EntityPlayer player){
		this.craftPad = pad;
		this.player = player;
		buildContainer();
		super.isInUse = true;
	}

	private void buildContainer() {
		// grid: 24, 24
		// output: 90, 35
		// chip: 137, 40

		// inv: 8, 98
		// hot-bar: 8, 156


		// output slot
		this.addSlotToContainer(new SlotCraft(craftPad, craftPad.outputInv, player, 0, 90, 35));

		// grid
		for (int i=0; i<3; i++) {
			for (int e=0; e<3; e++) {
				this.addSlotToContainer(new Slot(craftPad.gridInv, i*3 + e, e*18 +24, i*18 +24) {
					@Override
					public boolean canTakeStack(EntityPlayer player) {
						return false;
					}
					@Override
					public void onSlotChanged() {
						super.onSlotChanged();
						onGridChanged();
					}
				});
			}
		}

		// chip
		this.addSlotToContainer(new Slot(craftPad.chipInv, 0, 137, 40){
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack != null && stack.getItem() instanceof ItemChip;
			}

			@Override
			public void onSlotChanged() {
				onChipChanged(this);
				super.onSlotChanged();
			}

			@Override
			public int getSlotStackLimit(){
				return 1;
			}

		});

		// main player inv
		for (int i=0; i<3; i++) {
			for (int e=0; e<9; e++) {
				this.addSlotToContainer(new Slot(player.inventory, (i+1)*9 +e, e*18 +8, i*18 +98));
			}
		}

		// hot-bar
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, i*18 +8,  156));
		}
	}

	private void onChipChanged(Slot slot) {
		// Placing an encoded chips will replace the current recipe, by design

		if( slot.getHasStack() ) { // placed a chip
			CraftRecipe recipe = RecipeUtils.getRecipe(slot.getStack(), player.worldObj);

			if( recipe != null ) { // placed an encoded chip
				// update the crafting grid, without notifying it
				craftPad.gridInv.setContents(recipe.getIngredients());

				// update the output slot
				craftPad.outputInv.setInventorySlotContents(0, recipe.getResult());
				craftPad.outputInv.onInventoryChanged();

				// update the button
				craftPad.buttonID = CraftPad.MODE_ERASE;

			} else { // placing a blank chip
				// update the button
				CraftRecipe currentRecipe = craftPad.getRecipe(0);
				if( currentRecipe != null )
					craftPad.buttonID = CraftPad.MODE_WRITE;

				// Automatically clear invalid chips.
				if( CraftManager.isEncoded(slot.getStack()) ) {
					slot.putStack( new ItemStack(XActMod.itemRecipeBlank) );
				}
			}

		} else { // removed a chip
			if( craftPad.gridInv.isEmpty() )
				craftPad.buttonID = CraftPad.MODE_null;
			else
				craftPad.buttonID = CraftPad.MODE_CLEAR;
		}

		this.notifyOfChange();
	}

	private void onGridChanged() {

		// update the output slot.
		CraftRecipe recipe = craftPad.getRecipe(0);
		ItemStack outputStack = recipe == null ? null : recipe.getResult();
		craftPad.outputInv.setInventorySlotContents(0, outputStack);

		// update the button
		ItemStack chip = craftPad.chipInv.getStackInSlot(0);
		if( chip == null ) { // empty circuit slot
			if( craftPad.gridInv.isEmpty() )
				craftPad.buttonID = CraftPad.MODE_null;
			else
				craftPad.buttonID = CraftPad.MODE_CLEAR;

		} else { // there is a chip involved.
			if( recipe != null )
				craftPad.buttonID = CraftPad.MODE_WRITE;
			else {
				if( CraftManager.isEncoded(chip) )
					craftPad.buttonID = CraftPad.MODE_ERASE;
				else
					craftPad.buttonID = CraftPad.MODE_null;
			}
		}

		this.notifyOfChange();
	}

	public void buttonClicked(int buttonID) {
		craftPad.buttonID = buttonID;
		craftPad.buttonPressed();
		notifyOfChange();
	}

	private void notifyOfChange() {
		this.contentsChanged = true;
	}

	@Override
	public void setStack(int slotID, ItemStack stack) {

		Slot slot = (Slot) this.inventorySlots.get(slotID);
		if( slot != null ) {
			slot.putStack(stack);
		}
		this.notifyOfChange();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
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

		// Special Handle for the grid slot.
		if( 1 <= slotID && slotID < 10 ) {

			if( flag == 0 ) { // regular clicking.
				if( buttomPressed == 0 || playerStack == null ){
					slot.putStack(null);
				} else if( buttomPressed == 1 ){
					ItemStack copy = playerStack.copy();
					copy.stackSize = 1;
					slot.putStack(copy);
				}
				slot.onSlotChanged();
				return null;
			}

			if( flag == 1 )
				return null; // do nothing on shift click.

			if( flag == 2 ) { // interact with the hot-bar
				ItemStack invStack = player.inventory.getStackInSlot(buttomPressed);
				if( invStack != null ) {
					ItemStack copy = invStack.copy();
					copy.stackSize = 1;

					slot.putStack(copy);
					slot.onSlotChanged();
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

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		// only the output slot and any slot with a chip on it will respond to shift-clicking

		Slot slot = (Slot) inventorySlots.get(slotID);
		if( slot == null || !slot.getHasStack() )
			return null;

		ItemStack stackInSlot = slot.getStack();
		ItemStack retValue = stackInSlot.copy();

		// output's slot
		if( slot instanceof SlotCraft ) {
			stackInSlot = ((SlotCraft)slot).getCraftedStack();
			ItemStack copy = stackInSlot == null ? null : stackInSlot.copy();

			if ( mergeCraftedStack(stackInSlot, 11, inventorySlots.size()) ) {
				slot.onPickupFromSlot(player, stackInSlot);
				slot.onSlotChanged();
				return copy;
			}
			return null;
		}

		// Special treatment for chips.
		if( stackInSlot.getItem() instanceof ItemChip ) {
			if( slotID == 10 ) { // chip slot
				// try add to player's inventory
				if (!mergeItemStack(stackInSlot, 11, inventorySlots.size(), false))
					return null;
			} else if( slotID >= 11 ) { // slot on player's inv
				ItemStack currentChip = craftPad.chipInv.getStackInSlot(0);

				if( currentChip == null ) { // empty chip slot
					// add to the chip's slot
					if (!mergeItemStack(stackInSlot.splitStack(1), 10, 11, false))
						return null;

				} else if( CraftManager.isEncoded(currentChip) && CraftManager.isEncoded(stackInSlot) ) {
					// swap the chips.
					slot.putStack(currentChip);

					Slot chipSlot = (Slot) inventorySlots.get(10);
					chipSlot.putStack(stackInSlot);
					chipSlot.onSlotChanged();
				}
			}
		}

		if( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		}

		slot.onPickupFromSlot(player, retValue);
		slot.onSlotChanged();

		return retValue;
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
	protected void retrySlotClick(int slotID, int button, boolean shift, EntityPlayer player) {
		/*
		Only the output slot responds to shift clicking.
		Also, will only retry if shift-left clicking.
		 */
		if( slotID == 0 ) // the output slot
			if( button == 0 ) // left click + shift button.
				slotClick(slotID, button, 1, player); // retry
	}

	@Override
	public void onCraftGuiClosed(EntityPlayer player) {
		super.onCraftGuiClosed(player);
		ItemStack current = player.inventory.getCurrentItem();
		if( current != null ) {
			current.setItemDamage(0);
		}
	}

	private boolean equalsStacks( ItemStack stack1, ItemStack stack2 ){
		return stack1.itemID == stack2.itemID
				&& (!stack1.getHasSubtypes() || stack1.getItemDamage() == stack2.getItemDamage())
				&& ItemStack.areItemStackTagsEqual(stack1, stack2);
	}


	///////////////
	///// ContainerItem

	@Override
	public boolean hasInventoryChanged() {
		return craftPad.inventoryChanged;
	}

	@Override
	public void saveContentsToNBT(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.setTagCompound(new NBTTagCompound());
		craftPad.writeToNBT(itemStack.stackTagCompound);
	}

	@Override
	public void onContentsStored(ItemStack itemStack) {
		craftPad.inventoryChanged = false;
	}

}
