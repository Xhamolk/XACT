package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.core.ChipCase;
import xk.xact.core.ItemChip;

public class ContainerCase extends Container {

	private ChipCase chipCase;

	public ContainerCase(ChipCase chipCase, EntityPlayer player) {
		this.chipCase = chipCase;

		buildContainer(chipCase.getInternalInventory(), player.inventory);
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return true;
	}

	// storage: 78, 9 (slots 6x5)
	// inv: 17, 108
	// hot-bar: 17, 166

	private void buildContainer(IInventory storage, IInventory playerInv) {

		// storage (30 slots)
		for (int i=0; i<5; i++) {
			for (int e=0; e<6; e++) {
				this.addSlotToContainer(new Slot(storage, i*6 +e, e*18 +79, i*18 +10){
					@Override
					public boolean isItemValid(ItemStack stack){
						return stack != null && stack.getItem() instanceof ItemChip;
					}
				});
			}
		}

		// main player inv
		for (int i=0; i<3; i++) {
			for (int e=0; e<9; e++) {
				this.addSlotToContainer(new Slot(playerInv, (i+1)*9 +e, e*18 +18, i*18 +109));
			}
		}

		// hot-bar
		for (int i=0; i<9; i++) {
			this.addSlotToContainer(new Slot(playerInv, i, 	i*18 +18,  167));
		}

	}

	@Override
	public void onCraftGuiClosed(EntityPlayer player){
		super.onCraftGuiClosed(player);
        ItemStack current = player.inventory.getCurrentItem();
        if( current != null ) { // this should always be the case.
            chipCase.saveContentsTo(current);
            current.setItemDamage(0);
        }
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		Slot slot = (Slot) inventorySlots.get(slotID);

		if( slot == null || !slot.getHasStack() )
			return null;

		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		if( slotID < 30 ) {
			if (!mergeItemStack(stackInSlot, 30, inventorySlots.size(), false))
				return null;
		} else {
			if (!mergeItemStack(stackInSlot, 0, 30, false))
				return null;
		}

		if ( stackInSlot.stackSize == 0 )
			slot.putStack(null);

		slot.onSlotChanged();
		return stack;
	}

}
