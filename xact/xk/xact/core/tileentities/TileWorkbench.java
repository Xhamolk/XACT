package xk.xact.core.tileentities;


import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import xk.xact.api.OverriddenBlock;
import xk.xact.inventory.Inventory;
import xk.xact.inventory.InventoryUtils;
import xk.xact.util.Utils;

import java.util.HashMap;
import java.util.Map;

// the TE for the vanilla crafting table
public class TileWorkbench extends TileEntity implements ISidedInventory, OverriddenBlock {

	public Inventory craftingGrid; // size 9
	public Inventory outputInv; // size 1
	public Inventory subGrid; // size 9;

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
		this.subGrid = new Inventory( 9, "subGrid" );
	}

	///////////////
	///// NBT

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT( compound );

		craftingGrid.readFromNBT( compound );
		outputInv.readFromNBT( compound );
		subGrid.readFromNBT( compound );

		// Extra Fields
		NBTTagList list = compound.getTagList( "extraFields" );
		if( list != null ) {
			int count = list.tagCount();
			for(int i=0; i<count; i++) {
				NBTBase tag = list.tagAt( i );
				if( tag != null ) {
					Object field = Utils.readFieldFromNBT( tag );
					String name = tag.getName();
					if( field != null ) {
						extraFields.put( name, field );
					}
				}
			}
		}
	}

	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT( compound );

		craftingGrid.writeToNBT( compound );
		outputInv.writeToNBT( compound );
		subGrid.writeToNBT( compound );

		// Extra Fields
		NBTTagList list = new NBTTagList( "extraFields" );
		for(String key : extraFields.keySet()) {
			Utils.appendFieldToNBTList( list, key, extraFields.get( key ) );
		}
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
		if( packet.actionType == 0 )
			readFromNBT( packet.data );
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT( nbt );
		return new Packet132TileEntityData( xCoord, yCoord, zCoord, 0, nbt );
	}

	/* ********** Automation ********** *

	The bottom side is for extraction, all other sides are for insertion.

	If a recipe is set:
		* the input slots will accept items if they match the items on the grid. (Insert only)
		* the output slot may allow extraction if canCraft returns true.

	If a recipe is not set:
		* the input slots will NOT accept items, and will be available from the bottom for extraction (Extract only).
		* the output slot will NOT be available.
	*/

	/**
	 * Whether if there is a valid recipe set in the crafting grid.
	 */
	private boolean hasRecipe() {
		return outputInv.getStackInSlot( 0 ) != null;
	}

	/**
	 * Whether if automation can craft the current recipe in the crafting grid.
	 */
	private boolean canCraft() {
		if( !hasRecipe() )
			return false;
		InventoryCrafting grid = InventoryUtils.simulateCraftingInventory( null, subGrid );
		ItemStack result = CraftingManager.getInstance().findMatchingRecipe( grid, worldObj );
		return InventoryUtils.similarStacks( outputInv.getStackInSlot( 0 ), result, true );
	}

	/**
	 * Will consume the ingredients and handle the crafting events.
	 * Only used by automation.
	 *
	 * @param result the result of the recipe.
	 */
	private void handleExtraction(ItemStack result) {
		GameRegistry.onItemCrafted( Utils.getFakePlayerFor( this ), result, subGrid );

		for( int i = 0; i < subGrid.getSizeInventory(); ++i ) {
			ItemStack stack = subGrid.getStackInSlot( i );

			if( stack != null ) {
				subGrid.decrStackSize( i, 1 );

				if( stack.getItem().hasContainerItem() ) {
					ItemStack itemContainer = stack.getItem().getContainerItemStack( stack );

					if( itemContainer.isItemStackDamageable() && itemContainer.getItemDamage() > itemContainer.getMaxDamage() ) {
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( Utils.getFakePlayerFor( this ), itemContainer ) );
						itemContainer = null;
					}

					if( itemContainer != null && (!stack.getItem().doesContainerItemLeaveCraftingGrid( stack )) ) {
						if( subGrid.getStackInSlot( i ) == null ) {
							subGrid.setInventorySlotContents( i, itemContainer );
						} else {
							Utils.dropItemAsEntity( worldObj, xCoord, yCoord, zCoord, itemContainer );
						}
					}
				}
			}
		}
	}


	private static final int outputSide = 0;
	private static final int inputSide = 1;
	private static final int[][] slots = {
			new int[] { 0 }, // result slot
			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 } // grid slots.
	};

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if( side == 0 ) { // bottom
			if( hasRecipe() )
				return slots[outputSide];
		}
		return slots[inputSide];

		// if there is recipe set, bottom side is only the output slot; else, the input slots.
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack item, int side) {
		if( side == 0 )
			return false; // Bottom side is NOT for inserting.
		if( slot < 0 || slot > 9 )
			return false; // out of bounds.
		if( slot == 0 || !hasRecipe() )
			return false; // only insert on grid slots when the recipe is set.

		// Logic reminder: at this point, the recipe is set and its a grid slot.
		ItemStack gridStack = craftingGrid.getStackInSlot( slot - 1 );
		if( gridStack == null )
			return false;

		// Must check for NBT: the vanilla workbench must be the most reliable (strict) device.
		return InventoryUtils.similarStacks( gridStack, item, true );
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack item, int side) {
		if( side != 0 )
			return false; // May only extract from the bottom.
		if( slot < 0 || slot > 9 )
			return false; // out of bounds.
		if( slot == 0 ) { // output slot.
			return canCraft();
		}
		return !hasRecipe(); // can only extract from input slots if there is no recipe set.
	}

	@Override
	public int getSizeInventory() {
		return 10;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if( slot == 0 ) {
			if( canCraft() )
				return Utils.copyOf( outputInv.getStackInSlot( 0 ) );
			return null;
		}
		return subGrid.getStackInSlot( slot - 1 );
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if( slot == 0 ) { // output slot.
			if( hasRecipe() ) {
				ItemStack result = getStackInSlot( 0 );
				if( result != null ) {
					handleExtraction( result );
				}
				return result;
			}
			return null;
		} else {
			return subGrid.decrStackSize( slot - 1, amount );
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		if( slot == 0 ) {
			if( hasRecipe() ) {
				ItemStack result = getStackInSlot( 0 );
				if( result != null && itemstack == null ) { // trying to clear the slot by placing null?
					handleExtraction( result );
				}
			}
		} else {
			subGrid.setInventorySlotContents( slot-1, itemstack );
		}
	}

	@Override
	public String getInvName() {
		return "workbench";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		if( slot == 0 )
			return false;

		// Temp fix: Vanilla Hopper won't obey IInventory.getInventoryStackLimit()
		if( subGrid.getStackInSlot( slot - 1 ) != null )
			return false;

		ItemStack gridStack = craftingGrid.getStackInSlot( slot - 1 );
		return InventoryUtils.similarStacks( gridStack, item, true );
	}


	// ----- OverriddenBlock -----

	private Map<String, Object> extraFields = new HashMap<String, Object>();

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getField(Class<T> clazz, String name) {
		return (T) extraFields.get( name );
	}

	@Override
	public <T> void setField(Class<T> clazz, String name, T value) {
		extraFields.put( name, value );
	}

}
