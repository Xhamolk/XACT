package xk.xact.inventory;


import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

public interface IInventoryNBT extends IInventory {

	public void readFromNBT(NBTTagCompound compound);

	public void writeToNBT(NBTTagCompound compound);

}
