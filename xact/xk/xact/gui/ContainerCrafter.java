package xk.xact.gui;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.core.items.ItemChip;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.recipes.CraftManager;
import xk.xact.util.Utils;

import java.util.*;

/**
 * The container used for the Crafter's GUI.
 */
@ChestContainer
public class ContainerCrafter extends ContainerXACT implements InteractiveCraftingContainer {

	public TileCrafter crafter;

	private EntityPlayer player;

	private int gridFirstSlot;

	/**
	 * Used to know what ingredients are missing for each recipe.
	 */
	public boolean[][] recipeStates;

	private final boolean clientSide; // used to know on what side this container is running.

	public ContainerCrafter(TileCrafter crafter, EntityPlayer player) {
		this.crafter = crafter;
		this.player = player;
		this.clientSide = crafter.worldObj.isRemote;
		this.recipeStates = new boolean[crafter.getRecipeCount()][9];
		buildContainer();
	}

	private void buildContainer() {
		// craft results
		for( int i = 0; i < 4; i++ ) {
			int x = 20 + (i % 2) * 120;
			int y = 20 + (i / 2) * 44;
			addSlotToContainer( new SlotCraft( crafter, crafter.results, player, i, x, y ) );
		}

		// circuits
		for( int i = 0; i < 4; i++ ) {
			int x = 20 + (i % 2) * 120;
			int y = 40 + (i / 2) * 44;

			addSlotToContainer( new Slot( crafter.circuits, i, x, y ) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return CraftManager.isValid( stack );
				}

				@Override
				public int getSlotStackLimit() {
					return 1;
				}
			} );
		}

		// crafting grid (62,17) 3x3 (18x18)
		gridFirstSlot = this.inventorySlots.size();
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 3; e++ ) {
				int x = 18 * e + 62, y = 18 * i + 17, index = e + i * 3;
				addSlotToContainer( new Slot( crafter.craftGrid, index, x, y ) );
			}
		}

		// grid's output (80,78)
		addSlotToContainer( new SlotCraft( crafter, crafter.results, player, 4, 80, 78 ) );


		// resources (8,107) 3x9 (18x18)
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 9; e++ ) {
				int x = 18 * e + 8, y = 18 * i + 107;
				addSlotToContainer( new Slot( crafter.resources, e + i * 9, x, y ) );
			}
		}

		// player's inventory (8,174) 3x9 (18x18)
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 9; e++ ) {
				int x = 18 * e + 8, y = 18 * i + 174;
				addSlotToContainer( new Slot( player.inventory, e + i * 9 + 9, x, y ) );
			}
		}
		// player's hot bar (8,232) 1x9 (18x18)
		for( int i = 0; i < 9; i++ ) {
			addSlotToContainer( new Slot( player.inventory, i, 18 * i + 8, 232 ) );
		}

		this.onCraftMatrixChanged( crafter.craftGrid );
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		Slot slot = (Slot) inventorySlots.get( slotID );

		if( slot == null || !slot.getHasStack() )
			return null;
		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		if( slot instanceof SlotCraft ) {
			if( player.worldObj.isRemote ) // the server should handle this.
				return null;

			if( !slot.canTakeStack( player ) )
				return null;
			// add to the resources buffer.
			stackInSlot = ((SlotCraft) slot).getCraftedStack();
			ItemStack copy = stackInSlot == null ? null : stackInSlot.copy();

			if( mergeCraftedStack( stackInSlot, 8 + 10, 8 + 10 + 27 ) ) {
				slot.onPickupFromSlot( player, copy );
				slot.onSlotChanged();
				return copy;
			}
			return null;
		}

		// From the crafter to the resources buffer.
		if( slotID < 8 ) {
			if( !mergeItemStack( stackInSlot, 8 + 10, 8 + 10 + 27, false ) )
				return null;

		} else if( slotID < 8 + 10 ) { // from the crafting grid.
			if( !mergeItemStack( stackInSlot, 8 + 10, inventorySlots.size(), false ) )
				return null;

		} else if( slotID < 8 + 10 + 27 ) { // from the resources buffer
			// chips first try to go to the chip slots.
			if( stackInSlot.getItem() instanceof ItemChip ) {
				if( !mergeItemStack( stackInSlot, 4, 8, false ) ) // try add to the chip slots.
					if( !mergeItemStack( stackInSlot, 8 + 10 + 27, inventorySlots.size(), false ) ) // add to the player's inv.
						return null;

				// prevent retrying by returning null.
				stack = null;

			} else { // any other item goes to the player's inventory.
				if( !mergeItemStack( stackInSlot, 8 + 10 + 27, inventorySlots.size(), false ) )
					return null;
			}

		} else { // From the player's inventory to the resources buffer.
			if( !mergeItemStack( stackInSlot, 8 + 10, 8 + 10 + 27, false ) )
				return null;
		}

		if( stackInSlot.stackSize == 0 ) {
			slot.putStack( null );
		}
		slot.onSlotChanged();

		return stack;
	}

	// Whether if the slot can accept dragged items.
	@Override
	public boolean canDragIntoSlot(Slot slot) {
		return slot != null && slot.inventory != crafter.results;
	}

	// Whether if the slot's contents can be taken on double click.
	@Override
	public boolean func_94530_a(ItemStack itemStack, Slot slot) {
		return !isCraftingGridSlot( slot.slotNumber ) && slot.inventory != crafter.results;
	}

	protected boolean mergeCraftedStack(ItemStack itemStack, int indexMin, int indexMax) {

		// First, check if the stack can fit.
		int missingSpace = itemStack.stackSize;
		int emptySlots = 0;

		for( int i = indexMin; i < indexMax && missingSpace > 0; i++ ) {
			Slot tempSlot = (Slot) this.inventorySlots.get( i );
			ItemStack stackInSlot = tempSlot.getStack();

			if( stackInSlot == null ) {
				emptySlots++;
				continue;
			}

			if( stackInSlot.itemID == itemStack.itemID
					&& itemStack.getItemDamage() == stackInSlot.getItemDamage()
					&& ItemStack.areItemStackTagsEqual( itemStack, stackInSlot ) ) {

				missingSpace -= Math.min( stackInSlot.getMaxStackSize(), tempSlot.getSlotStackLimit() ) - stackInSlot.stackSize;
			}
		}

		// prevent crafting if there is no space for the crafted item.
		if( missingSpace > 0 )
			if( emptySlots == 0 )
				return false;

		// Try to merge with existing stacks.
		if( itemStack.isStackable() ) {

			for( int i = indexMin; i < indexMax; i++ ) {
				if( itemStack.stackSize <= 0 )
					break;

				Slot targetSlot = (Slot) this.inventorySlots.get( i );
				ItemStack stackInSlot = targetSlot.getStack();

				if( stackInSlot == null )
					continue;

				if( stackInSlot.itemID == itemStack.itemID
						&& (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage())
						&& ItemStack.areItemStackTagsEqual( itemStack, stackInSlot ) ) {

					int sum = itemStack.stackSize + stackInSlot.stackSize;
					int maxStackSize = Math.min( stackInSlot.getMaxStackSize(), targetSlot.getSlotStackLimit() );

					if( sum <= maxStackSize ) {
						stackInSlot.stackSize = sum;
						targetSlot.onSlotChanged();
						return true;
					} else if( stackInSlot.stackSize < maxStackSize ) {
						itemStack.stackSize -= maxStackSize - stackInSlot.stackSize;
						stackInSlot.stackSize = maxStackSize;
						targetSlot.onSlotChanged();
					}
				}
			}
		}

		// Add to an empty slot.
		if( itemStack.stackSize > 0 ) {

			for( int i = indexMin; i < indexMax; i++ ) {

				Slot targetSlot = (Slot) this.inventorySlots.get( i );
				ItemStack stackInSlot = targetSlot.getStack();

				if( stackInSlot != null )
					continue;

				targetSlot.putStack( itemStack );
				targetSlot.onSlotChanged();
				return true;
			}
		}

		return true;
	}

	@Override
	protected boolean mergeItemStack(ItemStack itemStack, int indexMin, int indexMax, boolean reverse) {
		boolean retValue = false;
		int index = indexMin;

		if( reverse ) {
			index = indexMax - 1;
		}

		Slot slot;
		ItemStack stackInSlot;

		if( itemStack.isStackable() ) {
			while( itemStack.stackSize > 0 && (!reverse && index < indexMax || reverse && index >= indexMin) ) {
				slot = (Slot) this.inventorySlots.get( index );
				stackInSlot = slot.getStack();

				int maxStackSize = Math.min( itemStack.getMaxStackSize(), slot.getSlotStackLimit() );

				if( stackInSlot != null && stackInSlot.itemID == itemStack.itemID
						&& (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage())
						&& ItemStack.areItemStackTagsEqual( itemStack, stackInSlot ) ) {

					int sum = stackInSlot.stackSize + itemStack.stackSize;

					if( sum <= maxStackSize ) {
						itemStack.stackSize = 0;
						stackInSlot.stackSize = sum;
						slot.onSlotChanged();
						retValue = true;
					} else if( stackInSlot.stackSize < maxStackSize ) {
						itemStack.stackSize -= maxStackSize - stackInSlot.stackSize;
						stackInSlot.stackSize = maxStackSize;
						slot.onSlotChanged();
						retValue = true;
					}
				}

				if( reverse ) {
					--index;
				} else {
					++index;
				}
			}
		}

		if( itemStack.stackSize > 0 ) {
			if( reverse ) {
				index = indexMax - 1;
			} else {
				index = indexMin;
			}

			while( !reverse && index < indexMax || reverse && index >= indexMin ) {
				slot = (Slot) this.inventorySlots.get( index );
				stackInSlot = slot.getStack();
				int maxStackSize = Math.min( itemStack.getMaxStackSize(), slot.getSlotStackLimit() );

				if( stackInSlot == null ) {
					int remaining = 0;
					ItemStack tempStack = itemStack;

					if( itemStack.stackSize > maxStackSize ) {
						remaining = itemStack.stackSize - maxStackSize;
						tempStack = itemStack.splitStack( maxStackSize );
					}

					slot.putStack( tempStack.copy() );
					slot.onSlotChanged();
					itemStack.stackSize = remaining;
					retValue = true;
					break;
				}

				if( reverse ) {
					--index;
				} else {
					++index;
				}
			}
		}

		return retValue;
	}

	// --------------- Update information ---------------

	@Override
	public void addCraftingToCrafters(ICrafting iCrafting) {
		super.addCraftingToCrafters( iCrafting );
		syncClients( Arrays.asList( iCrafting ) );
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		if( clientSide ) return;

		syncClients( crafters );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int var, int value) {
		if( var < crafter.getRecipeCount() ) { // Update recipe states from server info.
			recipeStates[var] = Utils.decodeInt( value, 9 );
			crafter.craftableRecipes[var] = !Utils.anyOf( recipeStates[var] );
		}
	}

	private void syncClients(List<ICrafting> clients) {
		if( clients == null || clients.size() == 0 )
			return;

		int i;
		int statesCount = crafter.getRecipeCount(); // when needed, add more here.

		for( i = 0; i < statesCount; i++ ) { // Sync recipe states.
			if( !Arrays.equals( recipeStates[i], crafter.recipeStates[i] ) ) {
				recipeStates[i] = crafter.recipeStates[i];
				int encodedState = Utils.encodeInt( recipeStates[i] );

				for( ICrafting client : clients ) {
					client.sendProgressBarUpdate( this, i, encodedState );
				}
			}
		}
	}

	// InteractiveCraftingContainer
	@Override
	public void setStack(int slotID, ItemStack stack) {
		if( slotID == -1 ) { // Clear the grid
			clearCraftingGrid();
			return;
		}

		Slot slot = getSlot( slotID );
		if( slot != null ) {
			slot.putStack( stack );
		}
	}

	///////////////
	///// ContainerXACT

	@Override
	protected boolean isCraftingGridSlot(int slotID) {
		return slotID >= gridFirstSlot && slotID < gridFirstSlot + 9;
	}

	@Override
	protected void clearCraftingGrid() {
		for( int i = 0; i < 9; i++ ) {
			Slot gridSlot = getSlot( i + gridFirstSlot );
			gridSlot.inventory.setInventorySlotContents( i, null );
		}
		crafter.craftGrid.onInventoryChanged();
	}

	@Override
	protected boolean isUpdateRequired() {
		return false;
	}

	// -------------------- Compatibility with Inventory Tweaks --------------------

	@ContainerSectionCallback
	@SuppressWarnings({ "unchecked", "unused" })
	public java.util.Map<ContainerSection, List<Slot>> getContainerSections() {
		Map<ContainerSection, List<Slot>> map = new HashMap<ContainerSection, List<Slot>>();
		List<Slot> slots = inventorySlots;

		map.put( ContainerSection.CRAFTING_OUT, getSlots( 0, 1, 2, 3, 17 ) ); // output slots
		map.put( ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList( 4, 17 ) ); // crafting grid and chips.
		map.put( ContainerSection.CHEST, slots.subList( 18, 18 + 27 ) ); // the resources buffer
		return map;
	}

	private List<Slot> getSlots(int... indexes) {
		List<Slot> slots = new ArrayList<Slot>();
		for( int index : indexes ) {
			slots.add( getSlot( index ) );
		}
		return slots;
	}

}
