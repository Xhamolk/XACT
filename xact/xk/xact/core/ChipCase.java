package xk.xact.core;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xk.xact.util.InvSlot;
import xk.xact.util.Inventory;
import xk.xact.util.InventoryUtils;

public class ChipCase {

	private Inventory internalInventory;

	public boolean isInUse = false;

	public boolean inventoryChanged = false;

	public ChipCase(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.stackTagCompound = new NBTTagCompound();
		this.internalInventory = new Inventory(30, "libraryStorage") {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				if( isInUse ) {
					inventoryChanged = true;
				}
			}
		};
		internalInventory.readFromNBT(itemStack.getTagCompound());
	}

	public IInventory getInternalInventory(){
		return internalInventory;
	}

	public void saveContentsTo(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.setTagCompound(new NBTTagCompound());
		itemStack.getTagCompound().setInteger("chipCount", getChipsCount());
		internalInventory.writeToNBT(itemStack.stackTagCompound);
	}

	private int getChipsCount(){
		int count = 0;
		for(InvSlot current : InventoryUtils.inventoryIterator(internalInventory) ){
			if( current != null && !current.isEmpty() ){
				count += current.stack.stackSize;
			}
		}
		return count;
	}

}
