package xk.xact.gui;


import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.core.CraftPad;
import xk.xact.core.items.ItemChip;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChestContainer
public class ContainerPad extends ContainerItem implements InteractiveCraftingContainer {

	public CraftPad craftPad;

	public EntityPlayer player;

	private int gridFirstSlot;

	private int heldItemSlot;

	public ContainerPad(CraftPad pad, EntityPlayer player, int heldItemSlot) {
		super( player );
		this.craftPad = pad;
		this.player = player;
		this.heldItemSlot = heldItemSlot;
		buildContainer();
		super.isInUse = true;
		craftPad.updateRecipe();
		craftPad.updateState();
	}

	private void buildContainer() {
		// grid: 24, 24
		// output: 90, 35
		// chip: 137, 40

		// inv: 8, 98
		// hot-bar: 8, 156


		// output slot
		this.addSlotToContainer( new SlotCraft( craftPad, craftPad.outputInv, player, 0, 90, 35 ) );

		// grid
		gridFirstSlot = inventorySlots.size();
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 3; e++ ) {
				this.addSlotToContainer( new Slot( craftPad.gridInv, i * 3 + e, e * 18 + 24, i * 18 + 24 ) {
					@Override
					public boolean canTakeStack(EntityPlayer player) {
						return false;
					}
				} );
			}
		}

		// chip
		this.addSlotToContainer( new Slot( craftPad.chipInv, 0, 137, 40 ) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack != null && stack.getItem() instanceof ItemChip;
			}

			@Override
			public void onSlotChanged() {
				onChipChanged( this );
				super.onSlotChanged();
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}

		} );

		// main player inv
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 9; e++ ) {
				this.addSlotToContainer( new Slot( player.inventory, (i + 1) * 9 + e, e * 18 + 8, i * 18 + 98 ) );
			}
		}

		// hot-bar
		for( int i = 0; i < 9; ++i ) {
			this.addSlotToContainer( new Slot( player.inventory, i, i * 18 + 8, 156 ) );
		}
	}

	@Override
	public void onTickUpdate(EntityPlayer player) {
		super.onTickUpdate( player );

		// Make sure the internal state is always updated.
		if( player.inventory.inventoryChanged ) {
			craftPad.updateState();
			player.inventory.inventoryChanged = false;
		}
	}

	// Once the chip is taken/placed/changed, this will be called to update the current recipe.
	private void onChipChanged(Slot slot) {
		// Placing an encoded chips will replace the current recipe, by design

		if( slot.getHasStack() ) { // placed a chip
			CraftRecipe recipe = RecipeUtils.getRecipe( slot.getStack(), player.worldObj );

			if( recipe != null ) { // placed an encoded chip
				// update the crafting grid
				craftPad.gridInv.setContents( recipe.getIngredients() );

			} else { // placing a blank chip
				// Automatically clear invalid chips.
				if( CraftManager.isEncoded( slot.getStack() ) ) {
					slot.putStack( new ItemStack( XActMod.itemRecipeBlank ) );
				}
			}

		}
	}

	@Override
	public void setStack(int slotID, ItemStack stack) {
		if( slotID == -1 ) { // Clear the grid
			clearCraftingGrid();
			return;
		}

		Slot slot = (Slot) this.inventorySlots.get( slotID );
		if( slot != null ) {
			slot.putStack( stack );
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		// only the output slot and any slot with a chip on it will respond to shift-clicking

		Slot slot = (Slot) inventorySlots.get( slotID );
		if( slot == null || !slot.getHasStack() )
			return null;

		ItemStack stackInSlot = slot.getStack();
		ItemStack retValue = stackInSlot.copy();

		// output's slot
		if( slot instanceof SlotCraft ) {
			stackInSlot = ((SlotCraft) slot).getCraftedStack();
			ItemStack copy = stackInSlot == null ? null : stackInSlot.copy();

			if( mergeCraftedStack( stackInSlot, 11, inventorySlots.size() ) ) {
				slot.onPickupFromSlot( player, stackInSlot );
				slot.onSlotChanged();
				return copy;
			}
			return null;
		}

		// Special treatment for chips.
		if( stackInSlot.getItem() instanceof ItemChip ) {
			if( slotID == 10 ) { // chip slot
				// try add to player's inventory
				if( !mergeItemStack( stackInSlot, 11, inventorySlots.size(), false ) )
					return null;
			} else if( slotID >= 11 ) { // slot on player's inv
				ItemStack currentChip = craftPad.chipInv.getStackInSlot( 0 );

				if( currentChip == null ) { // empty chip slot
					// add to the chip's slot
					if( !mergeItemStack( stackInSlot.splitStack( 1 ), 10, 11, false ) )
						return null;

				} else if( CraftManager.isEncoded( currentChip ) && CraftManager.isEncoded( stackInSlot ) ) {
					// swap the chips.
					slot.putStack( currentChip );

					Slot chipSlot = (Slot) inventorySlots.get( 10 );
					chipSlot.putStack( stackInSlot );
					chipSlot.onSlotChanged();
				}
			}
		}

		if( stackInSlot.stackSize == 0 ) {
			slot.putStack( null );
		}

		slot.onPickupFromSlot( player, retValue );
		slot.onSlotChanged();

		return retValue;
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
					&& (!itemStack.getHasSubtypes() || itemStack.getItemDamage() == stackInSlot.getItemDamage())
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
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed( player );
		ItemStack current = getParentItem();
		if( current != null ) {
			current.setItemDamage( 0 );
		}
	}

	// Whether if the slot's contents can be taken on double click.
	@Override
	public boolean func_94530_a(ItemStack itemStack, Slot slot) {
		return !isCraftingGridSlot( slot.slotNumber ) && slot.inventory != craftPad.outputInv;
	}

	///////////////
	///// ContainerItem

	@Override
	public boolean hasInventoryChanged() {
		return craftPad.inventoryChanged;
	}

	@Override
	public void saveContentsToNBT(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.setTagCompound( new NBTTagCompound() );
		craftPad.writeToNBT( itemStack.stackTagCompound );
	}

	@Override
	public void onContentsStored(ItemStack itemStack) {
		craftPad.inventoryChanged = false;
	}

	@Override
	public int getHeldItemSlotIndex() {
		return heldItemSlot;
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
		this.craftPad.gridInv.onInventoryChanged();
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
		int i = 0;
		List<Slot> slots = inventorySlots;

		map.put( ContainerSection.CRAFTING_OUT, slots.subList( i, i += 1 ) ); // output slot
		map.put( ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList( i, i += 9 ) ); // crafting grid.
		map.put( ContainerSection.CHEST, slots.subList( i, i += 1 ) ); // chip slot
		return map;
	}

}
