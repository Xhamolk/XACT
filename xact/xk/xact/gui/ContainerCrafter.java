package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.core.items.ItemChip;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.recipes.CraftManager;


/**
 * The container used for the Crafter's GUI.
 */
public class ContainerCrafter extends ContainerXACT implements InteractiveCraftingContainer {

	public TileCrafter crafter;

	private EntityPlayer player;

	private int gridFirstSlot;

	public ContainerCrafter(TileCrafter crafter, EntityPlayer player) {
		this.crafter = crafter;
		this.player = player;
		buildContainer();
		crafter.updateRecipes();
		crafter.updateStates();
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
	public boolean func_94531_b(Slot slot) {
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
}
