package xk.xact.gui;


import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xk.xact.core.ChipCase;
import xk.xact.core.items.ItemChip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChestContainer(rowSize = 6)
public class ContainerCase extends ContainerItem {

	public ChipCase chipCase;

	public ContainerCase(ChipCase chipCase, EntityPlayer player) {
		super( player );
		this.chipCase = chipCase;
		buildContainer( chipCase.getInternalInventory(), player.inventory );

		// mark the Chip Case "in use" so it will start tracking changes to it's inventory.
		super.isInUse = true;
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return true;
	}

	// storage: 78, 9 (slots 6x5)
	// inv: 17, 108
	// hot-bar: 17, 166

	private void buildContainer(IInventory storage, IInventory playerInv) {

		// storage (30 slots)
		for( int i = 0; i < 5; i++ ) {
			for( int e = 0; e < 6; e++ ) {
				this.addSlotToContainer( new Slot( storage, i * 6 + e, e * 18 + 79, i * 18 + 10 ) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return stack != null && stack.getItem() instanceof ItemChip;
					}
				} );
			}
		}

		// main player inv
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 9; e++ ) {
				this.addSlotToContainer( new Slot( playerInv, (i + 1) * 9 + e, e * 18 + 18, i * 18 + 109 ) );
			}
		}

		// hot-bar
		for( int i = 0; i < 9; i++ ) {
			this.addSlotToContainer( new Slot( playerInv, i, i * 18 + 18, 167 ) );
		}

	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed( player );

		// Reset the metadata value
		ItemStack itemStack = player.inventory.getCurrentItem();
		if( itemStack != null )
			itemStack.setItemDamage( 0 );
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		Slot slot = (Slot) inventorySlots.get( slotID );

		if( slot == null || !slot.getHasStack() )
			return null;

		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		if( slotID < 30 ) {
			if( !mergeItemStack( stackInSlot, 30, inventorySlots.size(), false ) )
				return null;
		} else if( stackInSlot.getItem() instanceof ItemChip ) {
			if( !mergeItemStack( stackInSlot, 0, 30, false ) )
				return null;
		} else {
			return null;
		}

		if( stackInSlot.stackSize == 0 )
			slot.putStack( null );

		slot.onSlotChanged();
		return stack;
	}

	///////////////
	///// ContainerItem

	@Override
	public boolean hasInventoryChanged() {
		return chipCase.inventoryChanged;
	}

	@Override
	public void saveContentsToNBT(ItemStack itemStack) {
		if( !itemStack.hasTagCompound() )
			itemStack.setTagCompound( new NBTTagCompound() );

		chipCase.writeToNBT( itemStack.getTagCompound() );
	}

	@Override
	public void onContentsStored(ItemStack itemStack) {
		chipCase.inventoryChanged = false;
	}

	@Override
	public int getHeldItemSlotIndex() {
		return player.inventory.currentItem;
	}

	///////////////
	///// ContainerXACT

	@Override
	protected boolean isCraftingGridSlot(int slotID) {
		return false;
	}

	@Override
	protected void clearCraftingGrid() { }

	@Override
	protected boolean isUpdateRequired() {
		return false;
	}

	// -------------------- Compatibility with Inventory Tweaks --------------------

	@ContainerSectionCallback
	public Map<ContainerSection, List<Slot>> getContainerSections() {
		Map<ContainerSection, List<Slot>> map = new HashMap<ContainerSection, List<Slot>>();
		List<Slot> slots = inventorySlots;

		map.put( ContainerSection.CHEST, slots.subList( 0, 30 ) ); // the storage slots
		return map;
	}

}
