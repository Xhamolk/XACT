package xk.xact.core;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.util.Inventory;

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
		internalInventory.writeToNBT(itemStack.stackTagCompound);
	}

}
