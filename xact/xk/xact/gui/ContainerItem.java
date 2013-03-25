package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * This class provides the methods required by ItemContainer.
 */
public abstract class ContainerItem extends ContainerXACT {

	protected final EntityPlayer player;

	public ContainerItem(EntityPlayer player) {
		this.player = player;
	}

	/**
	 * Whether if the container is in use.
	 * <p/>
	 * Usually you mark this a true after your Container is built and it's slots are ready.
	 * Most likely you won't need to set this back to false after closing the GUI.
	 */
	public boolean isInUse = false;

	/**
	 * Whether the internal content's have changed.
	 * Checked by ItemContainer.onUpdate() before saving the contents to the item's NBT.
	 * <p/>
	 * Note: if <code>isInUse</code> is false, this will be ignored by ItemContainer.onUpdate()
	 * Suggestion: make this return true after IInventory.onInventoryChanged() is invoked.
	 *
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
	 * <p/>
	 * Suggestion: you might want to make <code>hasInventoryChanged()</code> return false.
	 *
	 * @param itemStack the item to which the contents where saved to.
	 * @see xk.xact.core.ItemContainer#onUpdate(net.minecraft.item.ItemStack, net.minecraft.world.World, net.minecraft.entity.Entity, int, boolean)
	 */
	public abstract void onContentsStored(ItemStack itemStack);

	/**
	 * Whether if <code>slot</code> contains the currently held item.
	 *
	 * @param slot   the Slot being checked.
	 * @param player the EntityPlayer that is using the ContainerItem
	 * @return true if the <code>slot.isSlotInInventory(player.inventory, player.inventory.currentItem)</code> is true.
	 * @see Slot#isSlotInInventory(net.minecraft.inventory.IInventory, int)
	 */
	protected boolean slotContainsHeldItem(Slot slot, EntityPlayer player) {
		return slot != null && slot.isSlotInInventory( player.inventory, player.inventory.currentItem );
	}

	/**
	 * Called when the player tries to move/pick up the item in use.
	 * By default, just send the "cannot move" notification to chat.
	 *
	 * @param player    the EntityPlayer
	 * @param itemStack the ItemStack in use.
	 * @param slot      the Slot that contains the itemStack.
	 */
	protected void onPickupPrevented(EntityPlayer player, ItemStack itemStack, Slot slot) {
		if( !(player instanceof EntityPlayerMP) ) { // send the chat message client-side.
			String itemName = itemStack.getDisplayName();
			player.sendChatToPlayer( "Cannot move <" + itemName + "> while it's in use." );
		}
	}

	@Override
	public final ItemStack slotClick(int slotID, int buttonPressed, int flag, EntityPlayer player) {
		if( !checkSlot( slotID, buttonPressed, flag, player ) )
			return null;

		return super.slotClick( slotID, buttonPressed, flag, player );
	}

	@SuppressWarnings("unchecked")
	private Slot getSlotWithHeldItem(EntityPlayer player) {
		for( Slot slot : (List<Slot>) this.inventorySlots ) {
			if( slotContainsHeldItem( slot, player ) )
				return slot;
		}
		return null;
	}

	protected boolean checkSlot(int slotID, int buttonPressed, int flag, EntityPlayer player) {
		if( slotID >= 0 && slotID < inventorySlots.size() ) {
			Slot slot = getSlot( slotID );
			if( flag == 0 || flag == 1 || flag == 4 ) {
				if( slot != null && slotContainsHeldItem( slot, player ) ) {
					onPickupPrevented( player, slot.getStack(), slot );
					return false;
				}
			}
			if( flag == 2 && buttonPressed == player.inventory.currentItem ) {
				onPickupPrevented( player, player.inventory.getCurrentItem(), getSlotWithHeldItem( player ) );
				return false;
			}
		}
		return true;
	}

}
