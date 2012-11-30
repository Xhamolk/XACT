package xk.xact.core;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.util.InvSlot;
import xk.xact.util.Inventory;
import xk.xact.util.InventoryUtils;

public class ChipCase {

	private Inventory internalInventory;

	public ChipCase(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.stackTagCompound = new NBTTagCompound();
		this.internalInventory = new Inventory(30, "libraryStorage");
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
			if( current != null && !current.isEmpty() )
				count++;
		}
		return count;
	}

}
