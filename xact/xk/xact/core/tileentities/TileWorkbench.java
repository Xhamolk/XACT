package xk.xact.core.tileentities;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import xk.xact.inventory.Inventory;

// the TE for the vanilla crafting table
public class TileWorkbench extends TileEntity {

	public Inventory craftingGrid; // size 9
	public Inventory outputInv; // size 1

	public TileWorkbench() {
		this.craftingGrid = new Inventory( 9, "craftingGrid" );
		this.outputInv = new Inventory( 1, "outputInv" ) {

			@Override
			public ItemStack decrStackSize(int slotID, int amount) {
				ItemStack stackInSlot = getStackInSlot( slotID );
				if( stackInSlot != null ) {
					setInventorySlotContents( slotID, null );
				}
				return stackInSlot;
			}
		};
	}

	///////////////
	///// NBT

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT( compound );

		craftingGrid.readFromNBT( compound );
		outputInv.readFromNBT( compound );
	}

	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT( compound );

		craftingGrid.writeToNBT( compound );
		outputInv.writeToNBT( compound );
	}


}
