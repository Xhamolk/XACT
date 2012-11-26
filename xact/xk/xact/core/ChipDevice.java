package xk.xact.core;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.XActMod;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.util.Inventory;

/**
*
*
*/

// Used by the GUI
public class ChipDevice implements ICraftingDevice {

	private CraftRecipe recipe = null;
	private CraftingHandler handler;
	private EntityPlayer player;

	public int buttonID = -1;

	public final Inventory outputInv;
	public final Inventory gridInv;

	public ChipDevice(ItemStack stack, EntityPlayer player) {
		this.player = player;

		if( !stack.hasTagCompound() )
			stack.stackTagCompound = new NBTTagCompound();

		this.recipe = CraftRecipe.readFromNBT(stack.getTagCompound());

		this.outputInv = new Inventory(1, "outputInv");
		this.gridInv = new Inventory(9, "gridInv"){
			@Override
			public void onInventoryChanged(){
				super.onInventoryChanged();
				updateContents();
				updateButtonID();
			}
		};

		if( recipe != null ) {
			outputInv.setInventorySlotContents(0, recipe.getResult());
			gridInv.setContents(recipe.getIngredients());
		}

		this.handler = CraftingHandler.createCraftingHandler(this);
	}


	public ItemStack getResultingStack() {
		ItemStack temp = recipe == null ? null : CraftManager.encodeRecipe(recipe);
		if( temp == null )
		    return new ItemStack(XActMod.itemRecipeBlank);
		return temp;
	}

	public void buttonPressedBy(EntityPlayer player) {
		ItemStack stack;
		switch (buttonID) {
			case -1:
				return; // do nothing.

			case 0: // encode:
				stack = recipe == null ? null : CraftManager.encodeRecipe(recipe);
				if( stack != null ) {
					giveStackToPlayer(stack, player);
					buttonID = 1;
				}
				break;
			case 1: // clear grid
				for( int i=0; i<9; i++)
					gridInv.setInventorySlotContents(i, null);
				gridInv.onInventoryChanged();

				stack = new ItemStack(XActMod.itemRecipeBlank);
				giveStackToPlayer(stack, player);
				buttonID = recipe == null ? -1 : 0;
		}
		player.inventory.onInventoryChanged();
	}

	public void updateContents() {
		recipe = CraftManager.generateRecipe(gridInv.getContents(), Minecraft.getMinecraft().theWorld);

		if( recipe == null ){
			outputInv.setInventorySlotContents(0, null);
		} else {
			outputInv.setInventorySlotContents(0, recipe.getResult());
		}
		updateButtonID();
	}

	private void updateButtonID() {
		if( recipe != null ) {
			buttonID = 0;
		} else {
			buttonID = gridInv.isEmpty() ? -1 : 1;
		}
	}

	private void giveStackToPlayer(ItemStack stack, EntityPlayer player){
		player.inventory.mainInventory[player.inventory.currentItem] = stack;
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
		return recipe;
	}

	@Override
	public CraftingHandler getHandler() {
		return handler;
	}


}
