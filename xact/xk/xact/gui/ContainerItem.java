package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * This class provides the methods required by ItemContainer.
 */
public abstract class ContainerItem extends Container {

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

	/**
	 * Basically is the same method as <code>slotClick()</code>.
	 * This is just a call to <code>super.slotClick()</code>, but you can override with no problem.
	 * <p/>
	 * If you mod overrides <code>slotClick()</code>, make sure to override this instead.
	 *
	 * @param slotID        the ID for the Slot clicked. Matches with Slot.slotNumber
	 * @param buttonPressed the button pressed. For mouse clicks: 0 is left click, 1 is right click.
	 * @param flag          usually, 0 is the regular behaviour, 1 is shift-clicking, 2 is keyboard input.
	 * @param player        the EntityPlayer accessing
	 * @see Container#slotClick(int, int, int, net.minecraft.entity.player.EntityPlayer)
	 * @see Slot#slotNumber
	 */
	protected ItemStack handleSlotClick(int slotID, int buttonPressed, int flag, EntityPlayer player) {
		return super.slotClick( slotID, buttonPressed, flag, player );
	}

	// If you mod overrides this, override handleSlotClick instead.
	@Override
	public final ItemStack slotClick(int slotID, int buttonPressed, int flag, EntityPlayer player) {
		Slot slot = 0 <= slotID && slotID < inventorySlots.size() ? (Slot) inventorySlots.get( slotID ) : null;

		// Prevent moving the "held item", for security reasons.
		if( slot != null && slotContainsHeldItem( slot, player ) ) {
			onPickupPrevented( player, slot.getStack(), slot );
			return slot.getStack();
		}
		if( flag == 2 && buttonPressed == player.inventory.currentItem ) {
			onPickupPrevented( player, player.inventory.getCurrentItem(), getSlotWithHeldItem( player ) );
			return slot != null && slot.getHasStack() ? slot.getStack() : null;
		}

		return handleSlotClick( slotID, buttonPressed, flag, player );
	}

	@SuppressWarnings("unchecked")
	private Slot getSlotWithHeldItem(EntityPlayer player) {
		for( Slot slot : (List<Slot>) this.inventorySlots ) {
			if( slotContainsHeldItem( slot, player ) )
				return slot;
		}
		return null;
	}

}
