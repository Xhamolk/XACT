package xk.xact.gui;


import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import xk.xact.core.tileentities.TileWorkbench;
import xk.xact.inventory.InventoryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChestContainer
public class ContainerVanillaWorkbench extends Container {

	private final TileWorkbench workbench;
	private final EntityPlayer player;
	private final InventoryCrafting grid;

	public ContainerVanillaWorkbench(TileWorkbench workbench, EntityPlayer player) {
		this.workbench = workbench;
		this.player = player;
		this.grid = InventoryUtils.simulateCraftingInventory( this, workbench.craftingGrid );
		buildContainer();
		this.onCraftMatrixChanged( grid );
	}

	private void buildContainer() {
		this.addSlotToContainer( new SlotCrafting( player, grid, workbench.outputInv, 0, 124, 35 ) );

		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 3; e++ ) {
				this.addSlotToContainer( new Slot( grid, e + i * 3, 30 + e * 18, 17 + i * 18 ) );
			}
		}

		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 9; e++ ) {
				this.addSlotToContainer( new Slot( player.inventory, e + i * 9 + 9, 8 + e * 18, 84 + i * 18 ) );
			}
		}

		for( int i = 0; i < 9; i++ ) {
			this.addSlotToContainer( new Slot( player.inventory, i, 8 + i * 18, 142 ) );
		}

		this.onCraftMatrixChanged( grid );
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		ItemStack result = CraftingManager.getInstance().findMatchingRecipe( grid, workbench.worldObj );
		workbench.outputInv.setInventorySlotContents( 0, result );
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player1, int slotID) {

		ItemStack retValue = null;
		Slot slot = (Slot) this.inventorySlots.get( slotID );

		if( slot != null && slot.getHasStack() ) {
			ItemStack slotStack = slot.getStack();
			retValue = slotStack.copy();

			if( slotID == 0 ) { // output slot
				if( !this.mergeItemStack( slotStack, 10, 46, false ) ) { // changed last param to false.
					return null;
				}

				slot.onSlotChange( slotStack, retValue );

			} else if( slotID >= 10 && slotID < 37 ) { // player's main inv
				if( !this.mergeItemStack( slotStack, 37, 46, false ) ) {
					return null;
				}

			} else if( slotID >= 37 && slotID < 46 ) { // player's hot bat
				if( !this.mergeItemStack( slotStack, 10, 37, false ) ) {
					return null;
				}

			} else if( !this.mergeItemStack( slotStack, 10, 46, false ) ) { // from the grid to the player's inv (both).
				return null;
			}

			if( slotStack.stackSize == 0 ) {
				slot.putStack( null );
			}
			slot.onSlotChanged();


			if( slotStack.stackSize == retValue.stackSize ) {
				return null;
			}

			slot.onPickupFromSlot( player1, slotStack );
		}

		return retValue;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player1) {
		return true;
	}

	// Whether if the slot can accept dragged items.
	@Override
	public boolean canDragIntoSlot(Slot slot) {
		return slot.inventory != workbench.outputInv;
	}

	// -------------------- Compatibility with Inventory Tweaks --------------------

	@ContainerSectionCallback
	@SuppressWarnings({ "unchecked", "unused" })
	public java.util.Map<ContainerSection, List<Slot>> getContainerSections() {
		Map<ContainerSection, List<Slot>> map = new HashMap<ContainerSection, List<Slot>>();
		List<Slot> slots = inventorySlots;

		map.put( ContainerSection.CRAFTING_OUT, slots.subList( 0, 1 ) ); // output slot
		map.put( ContainerSection.CRAFTING_IN, slots.subList( 1, 1 + 9 ) ); // crafting grid.

		return map;
	}

}
