package xk.xact.core;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.XActMod;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.Inventory;

/**
*
*
*/

// Used by the GUI
public class CraftPad implements ICraftingDevice {

	private CraftRecipe lastRecipe = null;

	private CraftingHandler handler;
	private EntityPlayer player;

	public int buttonID = MODE_null;

	public final Inventory chipInv;
	public final Inventory gridInv;
	public final Inventory outputInv;

	public static final int MODE_null = -1;
	public static final int MODE_CLEAR = 0;
	public static final int MODE_WRITE = 1;
	public static final int MODE_ERASE = 2;

	public CraftPad(ItemStack stack, EntityPlayer player) {
		this.player = player;
		this.outputInv = new Inventory(1, "outputInv");
		this.gridInv = new Inventory(9, "gridInv");
		this.chipInv = new Inventory(1, "chipInv");

		this.handler = CraftingHandler.createCraftingHandler(this);

		if( !stack.hasTagCompound() )
			stack.stackTagCompound = new NBTTagCompound();
		this.readFromNBT(stack.getTagCompound());
	}

	public void buttonPressed() {
		switch (buttonID) {
			case MODE_null:
				return; // do nothing.

			case MODE_CLEAR: // clear grid
				for( int i=0; i<9; i++)
					gridInv.setInventorySlotContents(i, null);
				gridInv.onInventoryChanged();

				buttonID = MODE_null;
				break;

			case MODE_WRITE: // encode the current recipe
				ItemStack stack = lastRecipe == null ? null : CraftManager.encodeRecipe(lastRecipe);
				if( stack != null ) {
					chipInv.setInventorySlotContents(0, stack);
					chipInv.onInventoryChanged();
					buttonID = MODE_null;
				}
				break;

			case MODE_ERASE: // clear the chip
				chipInv.setInventorySlotContents(0, new ItemStack(XActMod.itemRecipeBlank));
				chipInv.onInventoryChanged();
				break;
		}
	}


	////////////
	/// ICraftingDevice

	@Override
	public final IInventory[] getAvailableInventories() {
		return new IInventory[]{ player.inventory };
	}

	@Override
	public int getRecipeCount() {
		return 1;
	}

	@Override
	public CraftRecipe getRecipe(int index) {
		return lastRecipe = CraftManager.generateRecipe(gridInv.getContents(), Minecraft.getMinecraft().theWorld);
	}

	@Override
	public CraftingHandler getHandler() {
		return handler;
	}

	////////////
	/// NBT

	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound tagCraftPad = (NBTTagCompound)compound.getTag("craftPad");
		if( tagCraftPad == null )
			return;

		buttonID = tagCraftPad.getInteger("buttonID");
		chipInv.readFromNBT(tagCraftPad);
		gridInv.readFromNBT(tagCraftPad);
		outputInv.readFromNBT(tagCraftPad);
	}

	public void writeToNBT(NBTTagCompound compound) {
		if( compound == null )
			return;

		NBTTagCompound tagCraftPad = new NBTTagCompound();
		tagCraftPad.setInteger("buttonID", buttonID);

		chipInv.writeToNBT(tagCraftPad);
		gridInv.writeToNBT(tagCraftPad);
		outputInv.writeToNBT(tagCraftPad);

		compound.setTag("craftPad", tagCraftPad);
	}

	/*
		NBT Structure:

		main tag:
			"craftPad":
				buttonID
				chipInv
				gridInv
				outputInv
		 */

}
