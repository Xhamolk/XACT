package xk.xact.core;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xk.xact.inventory.InvSlot;
import xk.xact.inventory.InvSlotIterator;
import xk.xact.inventory.Inventory;

public class ChipCase {

	private Inventory internalInventory;

	public boolean inventoryChanged = false;

	public ChipCase(ItemStack itemStack) {
		this.internalInventory = new Inventory( 30, "libraryStorage" ) {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				inventoryChanged = true;
			}
		};

		// Load contents from NBT
		if( !itemStack.hasTagCompound() )
			itemStack.stackTagCompound = new NBTTagCompound();
		readFromNBT( itemStack.getTagCompound() );
	}

	public IInventory getInternalInventory() {
		return internalInventory;
	}

	private int getChipsCount() {
		int count = 0;
		for( InvSlot current : InvSlotIterator.createNewFor( internalInventory ) ) {
			if( current != null && !current.isEmpty() ) {
				count += current.stack.stackSize;
			}
		}
		return count;
	}

	////////////
	/// NBT

	public void readFromNBT(NBTTagCompound compound) {
		if( compound == null )
			return;

		internalInventory.readFromNBT( compound );
	}

	public void writeToNBT(NBTTagCompound compound) {
		if( compound == null )
			return;

		internalInventory.writeToNBT( compound );
		compound.setInteger( "chipCount", getChipsCount() );
	}

}
