package xk.xact.core;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.util.Inventory;

public class ChipCase {

	private Inventory internalInventory;
	private NBTTagCompound tag;

	public ChipCase(ItemStack itemStack) {
		this.internalInventory = new Inventory(30, "libraryStorage");

		if( !itemStack.hasTagCompound() )
			itemStack.stackTagCompound = new NBTTagCompound();
		this.tag = itemStack.stackTagCompound;
		internalInventory.readFromNBT(itemStack.getTagCompound());
	}

	public IInventory getInternalInventory(){
		return internalInventory;
	}

	public void saveContentsTo(ItemStack itemStack) {
		if( itemStack == null )
			internalInventory.writeToNBT(tag);
		else
			internalInventory.writeToNBT(itemStack.stackTagCompound);
	}

}
