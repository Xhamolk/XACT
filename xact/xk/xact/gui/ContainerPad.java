package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.core.CraftPad;
import xk.xact.core.ItemChip;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;

public class ContainerPad extends Container {

	private CraftPad craftPad;
	private EntityPlayer player;

	public ContainerPad(CraftPad pad, EntityPlayer player){
		this.craftPad = pad;
		this.player = player;
		buildContainer();
	}

	private void buildContainer() {
		// grid: 24, 24
		// output: 90, 35
		// chip: 137, 40

		// inv: 8, 98
		// hot-bar: 8, 156


		// output slot
		this.addSlotToContainer(new SlotCraft(craftPad, craftPad.outputInv, 0, 90, 35));

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
				super.onSlotChanged();
				onChipChanged(this);
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
			this.addSlotToContainer(new Slot(player.inventory, i, 	i*18 +8,  156));
		}
	}

	private void onChipChanged(Slot slot) {
		// Placing an encoded chips will replace the current recipe, by design

		if( slot.getHasStack() ) { // placed a chip
			CraftRecipe recipe = CraftManager.decodeRecipe(slot.getStack());

			if( recipe != null ) { // placed an encoded chip
				// update the crafting grid, without notifying it
				craftPad.gridInv.setContents(recipe.getIngredients());

				// update the output slot
				craftPad.outputInv.setInventorySlotContents(0, recipe.getResult());

				// update the button
				craftPad.buttonID = CraftPad.MODE_ERASE;

			} else { // placing a blank chip
				// update the button
				CraftRecipe currentRecipe = craftPad.getRecipe(0);
				if( currentRecipe != null )
					craftPad.buttonID = CraftPad.MODE_WRITE;
			}

		} else { // removed a chip
			if( craftPad.gridInv.isEmpty() )
				craftPad.buttonID = CraftPad.MODE_null;
			else
				craftPad.buttonID = CraftPad.MODE_CLEAR;
		}
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

	}

	public void handlePacket(int buttonID) {
		craftPad.buttonID = buttonID;
		craftPad.buttonPressed();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack slotClick(int slotID, int buttomPressed, int flag, EntityPlayer player) {
		/*
		Special handle for the Grid slots (applies only to the grid's 9 slots).

		On regular clicks:
			- left click: copies the player's stack to the slot (null stacks also apply)
			- right click:

		On shift-clicking:
			do nothing. Users shouldn't shift click on the grid.

		Keyboard number:
			copy what ever is on that hot-bar slot.

		Otherwise: do the usual stuff.
		 */

		// todo: flag=2

		if( 1 <= slotID && slotID < 10 ) {
			Slot slot = ((Slot)this.inventorySlots.get(slotID));

			if( flag == 0 ) { // regular clicking.
				ItemStack playerStack = player.inventory.getItemStack();
				if( buttomPressed == 1 || playerStack == null ){
					slot.putStack(null);
				} else if( buttomPressed == 0 ){
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

		}

		return super.slotClick(slotID, buttomPressed, flag, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		// only the output slot and any slot with a chip on it will respond to shift-clicking

		Slot slot = (Slot) inventorySlots.get(slotID);
		if( slot == null || !slot.getHasStack() )
			return null;

		ItemStack stackInSlot = slot.getStack();
		ItemStack retValue = stackInSlot.copy();

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

		// output's slot
		if( slotID == 0 ) {
			if (!mergeItemStack(stackInSlot, 11, inventorySlots.size(), false))
				return null;
		}

		if( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		}

		slot.onPickupFromSlot(player, retValue);
		slot.onSlotChanged();

		return retValue;
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
		if( player.inventory.getCurrentItem() != null )
			craftPad.writeToNBT( player.inventory.getCurrentItem().getTagCompound() );
	}




}
