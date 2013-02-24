package xk.xact.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public abstract class MixedInventory implements IInventory {

	private ArrayList<IInventory> inventories;
	private int totalSize = 0;
	private String name;

	public MixedInventory(String name) {
		this.name = name;
		this.inventories = new ArrayList<IInventory>();
	}


	public void addInventory(IInventory inventory) {
		if( inventory == null )
			return;
		inventories.add( inventory );
		totalSize += inventory.getSizeInventory();
	}


	private boolean isSlotInBounds(int slot) {
		return slot >= 0 && slot < totalSize;
	}

	private InvPointer findInventoryBySlot(int slot) {
		if( isSlotInBounds( slot ) ) {
			int inventoriesCount = inventories.size();
			int offSet = 0;

			for( int i = 0; i < inventoriesCount; i++ ) {
				IInventory inv = inventories.get( i );
				int size = inv.getSizeInventory();
				if( slot - offSet < size ) {
					return new InvPointer( i, offSet );
				}
				offSet += size;
			}
		}
		return new InvPointer( -1, 0 );
	}

	///////////////
	///// IInventory

	@Override
	public int getSizeInventory() {
		return totalSize;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		InvPointer pointer = findInventoryBySlot( slot );
		IInventory inventory = pointer.getInventory();
		if( inventory != null ) {
			inventory.getStackInSlot( slot - pointer.offset );
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		InvPointer pointer = findInventoryBySlot( slot );
		IInventory inventory = pointer.getInventory();
		if( inventory != null ) {
			inventory.decrStackSize( slot - pointer.offset, amount );
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		InvPointer pointer = findInventoryBySlot( slot );
		IInventory inventory = pointer.getInventory();
		if( inventory != null ) {
			inventory.getStackInSlotOnClosing( slot - pointer.offset );
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
		InvPointer pointer = findInventoryBySlot( slot );
		IInventory inventory = pointer.getInventory();
		if( inventory != null ) {
			inventory.setInventorySlotContents( slot - pointer.offset, itemStack );
		}
	}

	@Override
	public String getInvName() {
		return "MixedInventory:" + name;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public abstract void onInventoryChanged();

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	private class InvPointer {
		final int index;
		final int offset;

		InvPointer(int index, int offset) {
			this.index = index;
			this.offset = offset;
		}

		IInventory getInventory() {
			if( index == -1 )
				return null;
			return inventories.get( index );
		}
	}

}
