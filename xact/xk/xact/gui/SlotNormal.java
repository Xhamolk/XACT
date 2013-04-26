package xk.xact.gui;


import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public abstract class SlotNormal extends Slot {

	public SlotNormal(IInventory inventory, int index, int x, int y) {
		super( inventory, index, x, y );
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		onChange();
	}

	public abstract void onChange();
}
