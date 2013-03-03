package xk.xact.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.inventory.ICustomInventory;
import net.minecraftforge.inventory.IDynamicInventory;
import net.minecraftforge.inventory.IInventoryHandler;
import xk.xact.XActMod;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.gui.ContainerCrafter;
import xk.xact.gui.CraftingGui;
import xk.xact.gui.GuiCrafter;
import xk.xact.inventory.Inventory;
import xk.xact.inventory.TransitionInventory;
import xk.xact.inventory.TransitionInventoryHandler;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.Utils;

import java.util.ArrayList;

/**
 * @author Xhamolk_
 */
public class TileCrafter extends TileMachine implements IInventory, ICraftingDevice, ICustomInventory, TransitionInventory {

	/*
	Available Inventories:
		This should be able to pull items from adjacent chests.

	Crafting mechanism:
		The CraftRecipe object knows the ingredients and their position on the crafting grid.
		If the required items are present on the resources buffer, then
		 (per operation) each ingredient will be placed into a fake crafting grid.
		 The recipe will be crafted.
		 Finally, the remaining items on the crafting grid will be placed back on the resources.
	 */

	/**
	 * Holds the recipes' outputs
	 */
	public final Inventory results;

	/**
	 * The inventory that holds the circuits.
	 */
	public final Inventory circuits; // size = 4

	/**
	 * The inventory that holds the crafting grid's contents.
	 */
	public final Inventory craftGrid;

	/**
	 * The resources inventory.
	 * You can access this inventory through pipes/tubes.
	 */
	public final Inventory resources; // size = 3*9 = 27

	public boolean contentsChanged = false;

	public TileCrafter() {
		this.results = new CrafterOutputInventory( getRecipeCount(), "Results" );
		this.circuits = new Inventory( 4, "Encoded Recipes" ) {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateRecipes();
				TileCrafter.this.updateStates();
				contentsChanged = true;
			}
		};
		this.craftGrid = new Inventory( 9, "CraftingGrid" ) {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateRecipes();
				TileCrafter.this.updateStates();
				contentsChanged = true;
			}
		};
		this.resources = new Inventory( 3 * 9, "Resources" ) {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateStates();
			}
		};
	}

	@Override // the items to be dropped when the block is destroyed.
	public ArrayList<ItemStack> getDropItems() {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for( int i = 0; i < this.circuits.getSizeInventory(); i++ ) {
			ItemStack stack = circuits.getStackInSlotOnClosing( i );
			if( stack != null )
				list.add( stack );
		}
		for( int i = 0; i < this.resources.getSizeInventory(); i++ ) {
			ItemStack stack = resources.getStackInSlotOnClosing( i );
			if( stack != null )
				list.add( stack );
		}
		return list;
	}

	public Container getContainerFor(EntityPlayer player) {
		return new ContainerCrafter( this, player );
	}

	public GuiContainer getGuiContainerFor(EntityPlayer player) {
		return new GuiCrafter( this, player );
	}

	///////////////
	///// Current State (requires updates)

	private boolean[] canCraftRecipes = new boolean[getRecipeCount()];

	private CraftRecipe[] recipes = new CraftRecipe[getRecipeCount()];

	public boolean[] missingIngredients = new boolean[9];

	// whether if the recipe's state must be red.
	public boolean isRedState(int i) {
		return canCraftRecipes[i];
	}

	// Gets the recipe's result.
	public ItemStack getRecipeResult(int slot) {
		CraftRecipe recipe = this.getRecipe( slot );
		return recipe == null ? null : recipe.getResult();
	}

	// updates the stored recipe results.
	public void updateRecipes() {
		for( int i = 0; i < getRecipeCount(); i++ ) {
			if( i == 4 ) {
				recipes[i] = RecipeUtils.getRecipe( craftGrid.getContents(), this.worldObj );
				if( recipes[i] != null ) {
					if( this.worldObj.isRemote ) { // client-side only
						notifyClientOfRecipeChanged();
					}
				}
				this.missingIngredients = getHandler().getMissingIngredientsArray( recipes[i] );
			} else {
				ItemStack stack = this.circuits.getStackInSlot( i );
				if( stack == null )
					recipes[i] = null;
				else
					recipes[i] = RecipeUtils.getRecipe( stack, this.worldObj );
			}
		}

		for( int i = 0; i < getRecipeCount(); i++ ) {
			ItemStack stack = recipes[i] == null ? null : recipes[i].getResult();
			results.setInventorySlotContents( i, stack );
		}
	}

	public void updateStates() {
		for( int i = 0; i < getRecipeCount(); i++ ) {
			// if there are not enough ingredients, color is red.
			canCraftRecipes[i] = (recipes[i] != null) && !getHandler().canCraft( this.getRecipe( i ), null );
		}
		this.missingIngredients = getHandler().getMissingIngredientsArray( recipes[4] );
	}

	///////////////
	///// ICraftingDevice

	/**
	 * Gets all the available inventories.
	 * In other words, all the inventories from which this crafter can pull resources.
	 * Adjacent chests (except on the top) will be included.
	 *
	 * @return an array of all the available IInventories.
	 */
	public IInventory[] getAvailableInventories() {
		return new Inventory[] {resources}; // todo: add adjacent chests.
	}

	@Override
	public int getRecipeCount() {
		return 5;
	}

	@Override
	public CraftRecipe getRecipe(int index) {
		if( index < 0 || index > getRecipeCount() )
			return null;

		if( recipes[index] == null ) {
			if( index == 4 ) {
				recipes[index] = RecipeUtils.getRecipe( craftGrid.getContents(), this.worldObj );
			} else {
				ItemStack stack = this.circuits.getStackInSlot( index );
				if( stack != null )
					recipes[index] = RecipeUtils.getRecipe( stack, this.worldObj );
			}
		}
		return recipes[index];
	}


	private CraftingHandler handler;

	@Override
	public CraftingHandler getHandler() {
		if( handler == null )
			handler = CraftingHandler.createCraftingHandler( this );
		return handler;
	}

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	///////////////
	///// IInventory: provide access to the resources inventory


	@Override
	public int getSizeInventory() {
		return resources.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return resources.getStackInSlot( var1 );
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return resources.decrStackSize( var1, var2 );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return resources.getStackInSlotOnClosing( var1 );
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		resources.setInventorySlotContents( var1, var2 );
	}

	@Override
	public String getInvName() {
		return resources.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	public void onInventoryChanged() { // this is called when adding stuff through pipes/tubes/etc 
		resources.onInventoryChanged();
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	///////////////
	///// NBT

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT( compound );
		resources.readFromNBT( compound );
		circuits.readFromNBT( compound );
		craftGrid.readFromNBT( compound );
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT( compound );
		resources.writeToNBT( compound );
		circuits.writeToNBT( compound );
		craftGrid.writeToNBT( compound );
	}

	//////////
	///// Recipe Deque

	@SideOnly(Side.CLIENT)
	private void notifyClientOfRecipeChanged() {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if( screen != null && screen instanceof CraftingGui ) {
			((CraftingGui) screen).pushRecipe( recipes[4] );
		}
	}

	//////////
	///// ICustomInventory

	@Override
	public IInventoryHandler getInventoryHandler() {
		return TransitionInventoryHandler.INSTANCE;
	}

	//////////
	///// TransitionInventory

	@Override
	public IInventory getHiddenInventory() {
		return results;
	}

	// This prevents exposing the craftable items to non-standardized inventory manipulators.
	class CrafterOutputInventory extends Inventory implements IDynamicInventory {

		public CrafterOutputInventory(int size, String name) {
			super( size, name );
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if( 0 <= slot && slot < getRecipeCount() )
				return Utils.copyOf( getRecipeResult( slot ) );
			return null;
		}

		@Override
		public void onInventoryChanged() {
			updateStates();
		}

		//////////
		///// IDynamicInventory

		@Override
		public int getSlotCapacityForItem(ItemStack itemStack, int slot) {
			return 0;
		}

		@Override
		public int getItemAvailabilityInSlot(int slot) {
			if( slot < 4 && !canCraftRecipes[slot] && recipes[slot] != null )
				return recipes[slot].getResult().stackSize;
			else
				return 0;
		}

		@Override
		public void onItemPlaced(ItemStack itemStack, int slot) {
		}

		@Override
		public void onItemTaken(ItemStack itemStack, int slot) {
			if( slot < 4 && itemStack != null ) {
				getHandler().doCraft( recipes[slot], fakePlayer(), itemStack );
			}
		}
	}

	private EntityPlayer fakePlayer() {
		return XActMod.proxy.getFakePlayer( getWorld(), xCoord, yCoord, zCoord );
	}

}
