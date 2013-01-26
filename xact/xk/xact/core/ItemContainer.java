package xk.xact.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.gui.ContainerItem;

/**
 * To be used by any item that can open a GUI.
 *
 * @author Xhamolk_
 */
public abstract class ItemContainer extends Item {

	public ItemContainer(int itemID) {
		super( itemID );
	}

	/**
	 * Whether if the <code>openContainer</code> is the one used for this item.
	 *
	 * @param openContainer the player's openContainer.
	 * @return true if <code>openContainer</code> is the one used for this item.
	 * @see EntityPlayer#openContainer
	 */
	public abstract boolean containerMatchesItem(Container openContainer);


	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int indexInInventory, boolean isCurrentItem) {
		if( world.isRemote || !isCurrentItem )
			return;
		if( ((EntityPlayer) entity).openContainer == null || ((EntityPlayer) entity).openContainer instanceof ContainerPlayer )
			return;

		if( containerMatchesItem( ((EntityPlayer) entity).openContainer ) ) {
			ContainerItem container = (ContainerItem) ((EntityPlayer) entity).openContainer;

			if( container.isInUse && container.hasInventoryChanged() ) {
				container.saveContentsToNBT( itemStack );
				container.onContentsStored( itemStack );
			}
		}
	}


}
