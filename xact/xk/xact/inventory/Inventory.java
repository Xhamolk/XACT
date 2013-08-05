package xk.xact.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import xk.xact.util.Utils;

/**
 * Simple implementation of IInventory
 *
 * @author Xhamolk_
 */
public class Inventory implements IInventory, IInventoryNBT {

	/**
	 * The size of this IInventory.
	 * Consider this as the amount of stacks that can fit inside.
	 */
	protected int size;

	/**
	 * The name that describes this IInventory.
	 */
	private final String name;

	/**
	 * The array of stacks contained in this IInventory.
	 */
	private ItemStack[] internalInv;

	/**
	 * The maximum number of items that can fit on an empty stack.
	 */
	protected int maxStackSize = 64;


	/**
	 * Creates a new Inventory with the specified size and name.
	 *
	 * @param size the size of this inventory.
	 * @param name the name that describes this inventory.
	 */
	public Inventory(int size, String name) {
		this.size = size;
		this.name = name;
		this.internalInv = new ItemStack[size];
	}

	/**
	 * Sets the new contents on the inventory, replacing the previous ones.
	 *
	 * @param contents the array containing all the contents.
	 */
	public void setContents(ItemStack... contents) {
		this.size = contents.length;
		this.internalInv = contents;
		this.onInventoryChanged();
	}

	public ItemStack[] getContents() {
		return this.internalInv.clone();
	}

	public boolean isEmpty() {
		for( int i = 0; i < this.size; i++ ) {
			if( internalInv[i] != null )
				return false;
		}
		return true;
	}

	/**
	 * Tries to add the stack into this inventory.
	 * First will try to merge with the not-full stacks, and the remaining will be placed on the first empty slot.
	 * <p/>
	 * Note: the stack will be manipulated.
	 *
	 * @param stack the stack to add to this inventory. if null, will return true.
	 * @return true if the stack was added entirely, false otherwise.
	 */
	public boolean addStack(ItemStack stack) {
		if( stack == null )
			return true;

		// Merge with existing stacks.
		ItemStack remaining = InventoryUtils.addStackToInventory( stack, this, true );
		if( remaining == null ) {
			stack.stackSize = 0;
			return true;
		}

		// Add to the first empty slot available.
		remaining = InventoryUtils.addStackToInventory( remaining, this, false );
		if( remaining == null ) {
			stack.stackSize = 0;
			return true;
		}

		stack.stackSize = remaining.stackSize;
		return false;
	}

	@Override
	public int getSizeInventory() {
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if( 0 <= slot && slot < size )
			return internalInv[slot];
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		if( internalInv[slot] == null )
			return null;
		ItemStack retValue;
		if( internalInv[slot].stackSize > count ) {
			retValue = internalInv[slot].splitStack( count );
		} else {
			retValue = internalInv[slot];
			internalInv[slot] = null;
		}
		this.onInventoryChanged();
		return retValue;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		ItemStack retValue = getStackInSlot( slot );
		setInventorySlotContents( slot, null );
		return retValue;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
		if( 0 <= slot && slot < size )
			internalInv[slot] = itemStack;
	}

	@Override
	public String getInvName() {
		return name;
	}

    @Override
    public boolean isInvNameLocalized() {
        return false;
    }

    @Override
	public int getInventoryStackLimit() {
		return maxStackSize;
	}

	@Override
	public void onInventoryChanged() {
		for( int i = 0; i < this.size; i++ ) {
			ItemStack stack = this.getStackInSlot( i );
			if( stack != null && stack.stackSize == 0 )
				this.setInventorySlotContents( i, null );
		}
	}


	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
        return true;
    }


    ///////////////
	///// NBT

	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound c = ((NBTTagCompound) compound.getTag( "inv." + name ));
		if( c == null ) return;

		NBTTagList list = c.getTagList( "inventoryContents" );
		for( int i = 0; i < list.tagCount() && i < internalInv.length; i++ ) {
			NBTTagCompound tag = (NBTTagCompound) list.tagAt( i );
			int index = tag.getInteger( "index" );
			internalInv[index] = Utils.readStackFromNBT( tag );
		}
	}

	public void writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for( int i = 0; i < internalInv.length; i++ ) {
			if( internalInv[i] != null && internalInv[i].stackSize > 0 ) {
				NBTTagCompound tag = new NBTTagCompound();
				list.appendTag( tag );
				tag.setInteger( "index", i );
				internalInv[i].writeToNBT( tag );
			}
		}
		NBTTagCompound ownTag = new NBTTagCompound();
		ownTag.setTag( "inventoryContents", list );
		compound.setTag( "inv." + name, ownTag );
	}
}
