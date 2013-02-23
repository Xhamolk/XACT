package xk.xact.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MixedInventory implements IInventory, IInventoryNBT {

	/**
	 * Creates a new Inventory with the specified size and name.
	 *
	 * @param size the size of this inventory.
	 * @param name the name that describes this inventory.
	 */
	public MixedInventory(int size, String name) {
		this.mainInventory = new Inventory( size, name );
	}

	private IInventoryNBT mainInventory;
	private IInventoryNBT secondaryInventory;


	public IInventory getSecondaryInventory() {
		return secondaryInventory;
	}


	///////////////
	///// NBT

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		mainInventory.readFromNBT( compound );
		secondaryInventory.readFromNBT( compound );
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		mainInventory.writeToNBT( compound );
		secondaryInventory.writeToNBT( compound );
	}


	///////////////
	///// IInventory (the main, public inventory)


	@Override
	public int getSizeInventory() {
		return mainInventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return mainInventory.getStackInSlot( slot );
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return mainInventory.decrStackSize( slot, amount );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return mainInventory.getStackInSlotOnClosing( slot );
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
		mainInventory.setInventorySlotContents( slot, itemStack );
	}

	@Override
	public String getInvName() {
		return mainInventory.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return mainInventory.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged() {
		mainInventory.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return mainInventory.isUseableByPlayer( player );
	}

	@Override
	public void openChest() {
		mainInventory.openChest();
	}

	@Override
	public void closeChest() {
		mainInventory.closeChest();
	}

}
