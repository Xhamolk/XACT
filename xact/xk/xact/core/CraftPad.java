package xk.xact.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
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


	public boolean isInUse = false;

	public boolean inventoryChanged = false;


	public CraftPad(ItemStack stack, EntityPlayer player) {
		this.player = player;
		this.outputInv = new Inventory(1, "outputInv");
		this.gridInv = new Inventory(9, "gridInv") {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				inventoryChanged = true;
			}
		};
		this.chipInv = new Inventory(1, "chipInv") {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				inventoryChanged = true;
			}
		};

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

	@SideOnly(Side.CLIENT)
	public boolean[] getMissingIngredients() {
		return getHandler().getMissingIngredientsArray( lastRecipe );
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
		return lastRecipe = RecipeUtils.getRecipe(gridInv.getContents(), player.worldObj);
	}

	@Override
	public CraftingHandler getHandler() {
		return handler;
	}

	@Override
	public World getWorld() {
		return player.worldObj;
	}

	////////////
	/// NBT

	public void saveContentsTo(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.setTagCompound(new NBTTagCompound());
		writeToNBT(itemStack.stackTagCompound);
	}

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
