package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.inventory.Inventory;
import xk.xact.inventory.InventoryUtils;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

import java.util.Arrays;

public class ContainerRecipe extends Container implements InteractiveCraftingContainer {


	private EntityPlayer player;

	public Inventory internalInventory = new Inventory( 10, "recipeInv" );

	public ContainerRecipe(EntityPlayer player) {
		this.player = player;
		buildContainer();
	}

	private void buildContainer() {
		// grid: 44, 24
		// output: 110, 35
		// inv: 8, 98
		// hot-bar: 8, 156


		// output slot
		this.addSlotToContainer( new Slot( internalInventory, 9, 110, 35 ) {
			@Override
			public boolean isItemValid(ItemStack item) {
				return false;
			}

			@Override
			public boolean canTakeStack(EntityPlayer player) {
				return false;
			}
		} );

		// grid
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 3; e++ ) {
				this.addSlotToContainer( new Slot( internalInventory, i * 3 + e, e * 18 + 44, i * 18 + 24 ) {
					@Override
					public boolean canTakeStack(EntityPlayer player) {
						return false;
					}

					@Override
					public void onSlotChanged() {
						updateOutputSlot();
					}
				} );
			}
		}

		// main player inv
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 9; e++ ) {
				this.addSlotToContainer( new Slot( player.inventory, (i + 1) * 9 + e, e * 18 + 8, i * 18 + 98 ) );
			}
		}

		// hot-bar
		for( int i = 0; i < 9; i++ ) {
			this.addSlotToContainer( new Slot( player.inventory, i, i * 18 + 8, 156 ) );
		}
	}


	@Override
	public ItemStack slotClick(int slotID, int buttomPressed, int flag, EntityPlayer player) {

		// Special handle for the grid slots.
		if( 1 == slotID && slotID < 10 ) {
			Slot slot = ((Slot) this.inventorySlots.get( slotID ));

			if( flag == 0 ) { // regular clicking.
				ItemStack playerStack = player.inventory.getItemStack();
				if( buttomPressed == 0 || playerStack == null ) {
					slot.putStack( null );
				} else if( buttomPressed == 1 ) {
					ItemStack copy = playerStack.copy();
					copy.stackSize = 1;
					slot.putStack( copy );
				}
				slot.onSlotChanged();
				return null;
			}

			if( flag == 1 )
				return null; // do nothing on shift click.

			if( flag == 2 ) { // interact with the hot-bar
				ItemStack invStack = player.inventory.getStackInSlot( buttomPressed );
				if( invStack != null ) {
					ItemStack copy = invStack.copy();
					copy.stackSize = 1;

					slot.putStack( copy );
					slot.onSlotChanged();

					return invStack;
				}

			}

			return null;
		}

		return super.slotClick( slotID, buttomPressed, flag, player );
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return null; // disable shift-clicking.
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return true;
	}

	private void updateOutputSlot() {
		ItemStack[] gridContents = Arrays.copyOf( internalInventory.getContents(), 9 );
		CraftRecipe recipe = RecipeUtils.getRecipe( gridContents, player.worldObj );

		Slot outputSlot = (Slot) inventorySlots.get( 0 );
		ItemStack item = null;
		if( recipe != null ) {
			InventoryCrafting grid = InventoryUtils.simulateCraftingInventory( gridContents );
			item = recipe.getRecipePointer().getOutputFrom( grid );
		}
		outputSlot.putStack( item );

		int notify = item == null ? 1 : 0;
		// todo: notify the GuiRecipe that the recipe has changed. 0 means has recipe, 1 means no recipe. (packet 0x05)
	}

	@Override
	public void setStack(int slotID, ItemStack stack) {
		if( slotID == -1 ) { // Clear the grid
			for( int i = 0; i < 9; i++ ) {
				Slot slot = (Slot) this.inventorySlots.get( i + 1 );
				slot.putStack( null );
			}
			return;
		}

		Slot slot = (Slot) this.inventorySlots.get( slotID );
		if( slot != null ) {
			slot.putStack( stack );
		}
	}


}
