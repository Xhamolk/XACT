package xk.xact.gui;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/**
 * Meant to be used to contain the circuits on the encoder and crafter.
 */
public class SlotRestricted extends Slot {

	/*
	Implementations:
		 Circuit slots: only 1 and only ItemRecipe items.
		 Encoder's Crafting grid: only accept one item
	 */


	public SlotRestricted(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack){
		return true;
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

}
