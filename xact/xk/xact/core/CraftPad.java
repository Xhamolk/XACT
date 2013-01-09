package xk.xact.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
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

	public final Inventory chipInv;
	public final Inventory gridInv;
	public final Inventory outputInv;

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

	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound tagCraftPad = (NBTTagCompound)compound.getTag("craftPad");
		if( tagCraftPad == null )
			return;

		chipInv.readFromNBT(tagCraftPad);
		gridInv.readFromNBT(tagCraftPad);
		outputInv.readFromNBT(tagCraftPad);
	}

	public void writeToNBT(NBTTagCompound compound) {
		if( compound == null )
			return;

		NBTTagCompound tagCraftPad = new NBTTagCompound();

		chipInv.writeToNBT(tagCraftPad);
		gridInv.writeToNBT(tagCraftPad);
		outputInv.writeToNBT(tagCraftPad);

		compound.setTag("craftPad", tagCraftPad);
	}

	/*
		NBT Structure:

		main tag:
			"craftPad":
				chipInv
				gridInv
				outputInv
		 */

}
