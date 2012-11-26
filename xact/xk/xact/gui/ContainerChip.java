package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.core.ChipDevice;

public class ContainerChip extends Container {

	private ChipDevice device;
	private EntityPlayer player;

	public ContainerChip( ChipDevice state, EntityPlayer player ){
		this.device = state;
		this.player = player;
		buildContainer();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	private void buildContainer() {
		// grid: 44, 24
		// output: 110, 35
		// inv: 7, 97
		// hot-bar: 7, 155


		// output slot
		this.addSlotToContainer(new SlotCraft(device, device.outputInv, 0, 110, 35));

		// grid
		for (int i=0; i<3; i++) {
			for (int e=0; e<3; e++) {
				this.addSlotToContainer(new Slot(device.gridInv, i*3 + e, e*18 +44, i*18 +24) {
					@Override
					public boolean canTakeStack(EntityPlayer player) {
						return false;
					}
				});
			}
		}

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

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		// only the output slot responds to shift clicking.

		Slot slot = (Slot) inventorySlots.get(slotID);
		if( slot == null || slotID != 0 || !slot.getHasStack() )
			return null;

		ItemStack stackInSlot = slot.getStack();
		ItemStack retValue = stackInSlot.copy();

		if (!mergeItemStack(stackInSlot, 10, inventorySlots.size(), false))
			return null;
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


	public void handlePacket(int buttonID) {
		device.buttonID = buttonID;
		device.buttonPressedBy(player);
	}

}
