package xk.xact.gui;


import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

/**
 * This class provides the methods required by ItemContainer.
 */
public abstract class ContainerItem extends Container {

	/**
	 * Whether if the container is in use.
	 *
	 * Usually you mark this a true after your Container is built and it's slots are ready.
	 * Most likely you won't need to set this back to false after closing the GUI.
	 */
	public boolean isInUse = false;

	/**
	 * Whether the internal content's have changed.
	 * Checked by ItemContainer.onUpdate() before saving the contents to the item's NBT.
	 *
	 * Note: if <code>isInUse</code> is false, this will be ignored by ItemContainer.onUpdate()
	 * Suggestion: make this return true after IInventory.onInventoryChanged() is invoked.
	 * @return true if the inventory's contents have changed.
	 * @see net.minecraft.inventory.IInventory#onInventoryChanged()
	 * @see xk.xact.core.ItemContainer#onUpdate(net.minecraft.item.ItemStack, net.minecraft.world.World, net.minecraft.entity.Entity, int, boolean)
	 */
	public abstract boolean hasInventoryChanged();

	/**
	 * Saves the inventory's contents to the specified item's NBT.
	 * It's called by ItemContainer.onUpdate() when <code>inventoryChanged</code> is true.
	 *
	 * @param itemStack the ItemStack that holds the inventory for this ContainerItem
	 * @see xk.xact.core.ItemContainer#onUpdate(net.minecraft.item.ItemStack, net.minecraft.world.World, net.minecraft.entity.Entity, int, boolean)
	 */
	public abstract void saveContentsToNBT(ItemStack itemStack);

	/**
	 * Called after saving the contents to the item's NBT.
	 *
	 * Suggestion: you might want to make <code>hasInventoryChanged()</code> return false.
	 * @param itemStack the item to which the contents where saved to.
	 * @see xk.xact.core.ItemContainer#onUpdate(net.minecraft.item.ItemStack, net.minecraft.world.World, net.minecraft.entity.Entity, int, boolean)
	 */
	public abstract void onContentsStored(ItemStack itemStack);

}
