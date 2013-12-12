package xk.xact.core.tileentities;

import appeng.api.IItemList;
import appeng.api.WorldCoord;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.tiles.INonSignalBlock;
import appeng.api.me.tiles.IStorageAware;
import appeng.api.me.util.IGridInterface;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.mcft.copy.betterstorage.api.ICrateStorage;
import net.mcft.copy.betterstorage.api.ICrateWatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.client.gui.GuiCrafter;
import xk.xact.client.gui.GuiCrafting;
import xk.xact.config.ConfigurationManager;
import xk.xact.gui.ContainerCrafter;
import xk.xact.inventory.Inventory;
import xk.xact.inventory.InventoryUtils;
import xk.xact.plugin.PluginManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xhamolk_
 */
public class TileCrafter extends TileMachine implements IInventory, ICraftingDevice, ICrateWatcher, IStorageAware, INonSignalBlock, IGridTileEntity {

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

	// Used to trigger state updates on the next tick.
	private boolean stateUpdatePending = false;

	// Whether if the neighbors have changed, so this machine should update on the next tick.
	private boolean neighborsUpdatePending = false;

	// Used by GuiCrafter to update it's internal state.
	// Should only be accessed client-side for rendering purposes.
	public boolean recentlyUpdated = false;

	public TileCrafter() {
		this.results = new Inventory( getRecipeCount(), "Results" );
		this.circuits = new Inventory( 4, "Encoded Recipes" ) {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateRecipes();
				stateUpdatePending = true;
				recentlyUpdated = true;
			}
		};
		this.craftGrid = new Inventory( 9, "CraftingGrid" ) {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.updateRecipes();
				stateUpdatePending = true;
				recentlyUpdated = true;
			}
		};
		this.resources = new Inventory( 3 * 9, "Resources" ) {
			@Override
			public void onInventoryChanged() {
				TileCrafter.this.onInventoryChanged();
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

	@SideOnly(Side.CLIENT)
	public GuiContainer getGuiContainerFor(EntityPlayer player) {
		return new GuiCrafter( this, player );
	}

	@Override
	public void onBlockUpdate(int type) {
		switch( type ) {
			case 0: // Block update
				neighborsUpdatePending = true;
				break;
			case 1: // Tile change
				stateUpdatePending = true;
				break;
		}
	}

	@Override
	public void validate() {
		super.validate();
		neighborsUpdatePending = true;
		fireLoadEventAE();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for( int i = 0; i < adjacentCrates.length; i++ ) {
			ICrateStorage crate = adjacentCrates[i];
			if( crate != null ) {
				crate.unregisterCrateWatcher( this );
				adjacentCrates[i] = null;
			}
		}
		fireUnloadEventAE();
	}

	///////////////
	///// Current State (requires updates)

	public boolean[] craftableRecipes = new boolean[getRecipeCount()];

	private CraftRecipe[] recipes = new CraftRecipe[getRecipeCount()];

	/**
	 * The current state of the recipes loaded into this machine.
	 */
	public boolean[][] recipeStates = new boolean[getRecipeCount()][9];

	@Override
	public void updateEntity() { // It was 5!
		if( worldObj.getWorldTime() % 40 != 0 ) { // 4 checks per second might be enough.
			return;
		}

		updateIfChangesDetected();
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
					if( this.worldObj != null && this.worldObj.isRemote ) { // client-side only
						notifyClientOfRecipeChanged();
					}
				}
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

	// Updates the states of the recipes.
	public void updateState() {
		if( worldObj.isRemote ) {
			return; // don't do this client-side.
		}
		for( int i = 0; i < getRecipeCount(); i++ ) {
			// if the recipe can be crafted.
			craftableRecipes[i] = (recipes[i] != null) && getHandler().canCraft( this.getRecipe( i ), null );
			recipeStates[i] = getHandler().getMissingIngredientsArray( recipes[i] );
		}
	}

	// Used to trigger updates if changes have been detected.
	private void updateIfChangesDetected() {
		if( neighborsUpdatePending && !worldObj.isRemote ) {
			checkForAdjacentCrates();
			neighborsUpdatePending = false;
		}

		if( stateUpdatePending ) {
			updateState();
			stateUpdatePending = false;
		}
	}

	///////////////
	///// ICraftingDevice

	@Override
	@SuppressWarnings( "unchecked" )
	public List getAvailableInventories() {
		// Pulling from adjacent inventories
		List list = Utils.getAdjacentInventories( this.worldObj, this.xCoord, this.yCoord, this.zCoord );
		// The internal inventory is always the top priority.
		list.add( 0, resources );
		return list;
//		List<IInventory> list = Utils.getAdjacentInventories( worldObj, xCoord, yCoord, zCoord );
//		list.add( 0, resources );
//		return list.toArray( new IInventory[0] );
//		return Arrays.asList( resources );
	}

	@Override
	public int getRecipeCount() {
		return 5;
	}

	@Override
	public boolean canCraft(int index) {
		if( index < 0 || index > getRecipeCount() )
			return false;
		updateIfChangesDetected();
		return craftableRecipes[index];
	}

	@Override
	public CraftRecipe getRecipe(int index) {
		if( index < 0 || index > getRecipeCount() )
			return null;

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
	public boolean isInvNameLocalized() {
		return false;  // Not a clue what this does.
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public void onInventoryChanged() { // this is called when adding stuff through pipes/tubes/etc
		super.onInventoryChanged();
		stateUpdatePending = true;
		recentlyUpdated = true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return true;  // Whether if an item can be placed at the slot
	}

	///////////////
	///// NBT

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT( compound );
		resources.readFromNBT( compound );
		circuits.readFromNBT( compound );
		craftGrid.readFromNBT( compound );
		updateRecipes();
		stateUpdatePending = true;
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
		if( screen != null && screen instanceof GuiCrafting ) {
			((GuiCrafting) screen).pushRecipe( recipes[4] );
		}
	}

	// ---------- Pulling from Adjacent Inventories ---------- //

	@SuppressWarnings("unchecked")
	public static List getAdjacentInventories(TileCrafter machine) {
		List<TileEntity> tiles = Utils.getAdjacentTileEntities( machine.worldObj, machine.xCoord, machine.yCoord, machine.zCoord );
		List<Object> adjacentInventories = new ArrayList<Object>();
		for( TileEntity tile : tiles ) {
			if( tile != null && InventoryUtils.isValidInventory( tile ) )
				adjacentInventories.add( tile );
		}
		return adjacentInventories;
	}

	// ---------- Better Storage integration ---------- //

	private ICrateStorage[] adjacentCrates = new ICrateStorage[6];

	@Override
	public void onCrateItemsModified(ItemStack stack) {
		stateUpdatePending = true;
	}

	private void checkForAdjacentCrates() {
		if( !ConfigurationManager.ENABLE_BETTER_STORAGE_PLUGIN )
			return;

		boolean foundChanges = false;

		for( int i = 0; i < 6; i++ ) {
			int x = xCoord + ForgeDirection.VALID_DIRECTIONS[i].offsetX;
			int y = yCoord + ForgeDirection.VALID_DIRECTIONS[i].offsetY;
			int z = zCoord + ForgeDirection.VALID_DIRECTIONS[i].offsetZ;

			TileEntity tile = worldObj.getBlockTileEntity( x, y, z );
			if( tile != null && tile instanceof ICrateStorage ) {
				if( adjacentCrates[i] == null ) {
					adjacentCrates[i] = (ICrateStorage) tile;
					adjacentCrates[i].registerCrateWatcher( this );
					foundChanges = true;
				}
			} else if( adjacentCrates[i] != null  ) {
				adjacentCrates[i] = null;
				foundChanges = true;
			}
		}
		if( foundChanges )
			stateUpdatePending = true;
	}

	// ---------- Applied Energistics integration ---------- //

	@Override
	public WorldCoord getLocation() {
		return new WorldCoord( xCoord, yCoord, zCoord );
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void setPowerStatus(boolean hasPower) { }

	@Override
	public boolean isPowered() {
		return true;
	}

	@Override
	public IGridInterface getGrid() {
		return null;
	}

	@Override
	public void setGrid(IGridInterface gi) { }

	@Override
	public void onNetworkInventoryChange(IItemList iss) {
		stateUpdatePending = true;
	}

	// Connectivity events

	private void fireLoadEventAE() {
		if( PluginManager.aeProxy != null ) {
			PluginManager.aeProxy.fireTileLoadEvent( this );
		}
	}

	private void fireUnloadEventAE() {
		if( PluginManager.aeProxy != null ) {
			PluginManager.aeProxy.fireTileUnloadEvent( this );
		}
	}

}
