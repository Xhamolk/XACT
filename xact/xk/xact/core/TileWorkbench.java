package xk.xact.core;


import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import xk.xact.util.FakeCraftingInventory;
import xk.xact.util.Inventory;

// the TE for the vanilla crafting table
public class TileWorkbench extends TileEntity {

	public Inventory craftingGrid; // size 9
	public Inventory outputInv; // size 1

	public TileWorkbench() {
		this.craftingGrid = new Inventory(9, "craftingGrid"){
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				updateOutputSlot();
			}
		};
		this.outputInv = new Inventory(1, "outputInv");
	}

	// Updates the contents of the output slot every time the grid is changed (and when the container is built).
	public void updateOutputSlot() {
		FakeCraftingInventory grid = FakeCraftingInventory.emulateContents( craftingGrid.getContents() );
		ItemStack result = CraftingManager.getInstance().findMatchingRecipe( grid, worldObj );
		outputInv.setInventorySlotContents(0, result);
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
