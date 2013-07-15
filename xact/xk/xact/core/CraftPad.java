package xk.xact.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.client.gui.GuiCrafting;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

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

	// If true, the CraftPad's inventory has changed and it must be saved to NBT.
	public boolean inventoryChanged = false;

	// Used by GuiPad to update it's internal state.
	// Should only be accessed client-side for rendering purposes.
	public boolean recentlyUpdated = true;


	public CraftPad(ItemStack stack, EntityPlayer player) {
		this.player = player;
		this.outputInv = new Inventory( 1, "outputInv" ) {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				updateState();
			}
		};
		this.gridInv = new Inventory( 9, "gridInv" ) {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				inventoryChanged = true;
				updateRecipe();
				updateState();
			}
		};
		this.chipInv = new Inventory( 1, "chipInv" ) {
			@Override
			public void onInventoryChanged() {
				super.onInventoryChanged();
				inventoryChanged = true;
				updateRecipe();
				updateState();
			}
		};

		this.handler = CraftingHandler.createCraftingHandler( this );

		if( !stack.hasTagCompound() )
			stack.stackTagCompound = new NBTTagCompound();
		this.readFromNBT( stack.getTagCompound() );
	}

	////////////
	/// Current State

	public boolean[] getMissingIngredients() {
		return getHandler().getMissingIngredientsArray( lastRecipe );
	}

	public void updateRecipe() {
		lastRecipe = RecipeUtils.getRecipe( gridInv.getContents(), player.worldObj );
		if( getWorld().isRemote )
			notifyClient();

		ItemStack output = lastRecipe == null ? null : lastRecipe.getResult();
		outputInv.setInventorySlotContents( 0, output );
	}

	public void updateState() {
		recentlyUpdated = true;
	}

	////////////
	/// ICraftingDevice

	@Override
	public final List getAvailableInventories() {
		return Arrays.asList( player.inventory );
	}

	@Override
	public int getRecipeCount() {
		return 1;
	}

	@Override
	public boolean canCraft(int index) {
		return handler.canCraft( lastRecipe, null );
	}

	@Override
	public CraftRecipe getRecipe(int index) {
		return lastRecipe;
	}

	@Override
	public CraftingHandler getHandler() {
		return handler;
	}

	@Override
	public World getWorld() {
		return player.worldObj;
	}

	private void notifyClient() { // client-only
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if( screen != null && screen instanceof GuiCrafting ) {
			((GuiCrafting) screen).pushRecipe( lastRecipe );
		}
	}

	////////////
	/// NBT

	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound tagCraftPad = (NBTTagCompound) compound.getTag( "craftPad" );
		if( tagCraftPad == null )
			return;

		chipInv.readFromNBT( tagCraftPad );
		gridInv.readFromNBT( tagCraftPad );
		outputInv.readFromNBT( tagCraftPad );
	}

	public void writeToNBT(NBTTagCompound compound) {
		if( compound == null )
			return;

		NBTTagCompound tagCraftPad = new NBTTagCompound();

		chipInv.writeToNBT( tagCraftPad );
		gridInv.writeToNBT( tagCraftPad );
		outputInv.writeToNBT( tagCraftPad );

		String loadedRecipe = lastRecipe == null ? "" : lastRecipe.toString();
		compound.setString( "loadedRecipe", loadedRecipe );

		compound.setTag( "craftPad", tagCraftPad );
	}

	/*
		NBT Structure:

		main tag:
			"craftPad":
				chipInv
				gridInv
				outputInv
				"loadedRecipe"
		 */

}
