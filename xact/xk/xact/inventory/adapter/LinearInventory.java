package xk.xact.inventory.adapter;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import xk.xact.api.IInventoryAdapter;
import xk.xact.inventory.InvSlot;
import xk.xact.inventory.InvSlotIterator;
import xk.xact.inventory.InventoryUtils;
import xk.xact.inventory.SidedInventory;
import xk.xact.util.Utils;

import java.util.Iterator;

/**
 *
 * @author Xhamolk_
 */
public class LinearInventory implements IInventoryAdapter {

	private final IInventory inventory;
	private boolean isSided;

	public LinearInventory(IInventory inventory) {
		this.inventory = inventory;
		this.isSided = inventory instanceof ISidedInventory;
		if( isSided && !(inventory instanceof SidedInventory) ) {
			// My implementation of ISided is the only one allowed past here.
			Utils.logException( "Attempting to use LinearInventory on an unregulated ISidedInventory.", new RuntimeException("Class: "+ inventory.getClass()), false );
		}
	}

	@Override
	public ItemStack placeItem(ItemStack item) {
		ItemStack stack = InventoryUtils.addStackToInventory( item, inventory, true );
		if( stack != null )
			return InventoryUtils.addStackToInventory( stack, inventory, false );
		return stack;
	}

	@Override
	public ItemStack takeItem(ItemStack item, int quantity) {
		ItemStack stack = null;
		for( InvSlot slot : InvSlotIterator.createNewFor( inventory ) ) {
			if( slot == null )
				continue;

			// Again, make sure only my implementation of ISidedInventory reaches this point.
			if( isSided && !((ISidedInventory)inventory).canExtractItem( slot.slotIndex, slot.stack, -1 ))
				continue;

			if( InventoryUtils.similarStacks( slot.stack, item, true ) ) {
				ItemStack toAdd;
				if( slot.stack.stackSize > quantity ) {
					toAdd = inventory.decrStackSize( slot.slotIndex, quantity );
					if( toAdd != null ) {
						quantity -= toAdd.stackSize;
					}
				} else {
					toAdd = slot.stack.copy();
					quantity -= slot.stack.stackSize;
					inventory.setInventorySlotContents( slot.slotIndex, null );
				}
				if( toAdd != null ) {
					inventory.onInventoryChanged();
					if( stack == null ) {
						stack = toAdd.copy();
					} else {
						stack.stackSize += toAdd.stackSize;
					}
				}
				if( quantity <= 0 )
					break;
			}
		}

		return stack;
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return new LinearInventoryIterator();
	}

	private class LinearInventoryIterator implements Iterator<ItemStack> {

		private int i = 0;

		@Override
		public boolean hasNext() {
			return i < inventory.getSizeInventory();
		}

		@Override
		public ItemStack next() {
			return inventory.getStackInSlot( i++ );
		}

		@Override
		public void remove() { }
	}
}
