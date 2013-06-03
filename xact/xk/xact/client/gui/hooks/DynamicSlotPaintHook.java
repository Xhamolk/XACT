package xk.xact.client.gui.hooks;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Used for having dynamically-painted slots.
 *
 * @author Xhamolk_
 */
public interface DynamicSlotPaintHook {

	public boolean shouldDelegateSuperCall(Slot slot);

	public int getSlotUnderlayColor(Slot slot, boolean mouseOver);

	public int getSlotOverlayColor(Slot slot, boolean mouseOver);

	public ItemStack getStackToPaintInSlot(Slot slot, boolean mouseOver);

}
