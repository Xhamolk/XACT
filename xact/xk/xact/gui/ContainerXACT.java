package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import xk.xact.util.Utils;

import java.util.HashSet;

public abstract class ContainerXACT extends Container {

	protected int dragState;
	protected int dragButton;
	protected HashSet<Slot> draggedSlots = new HashSet<Slot>(); // the IDs of the slots dragged.

	protected abstract boolean isCraftingGridSlot(int slotID);

	protected abstract void clearCraftingGrid();

	protected abstract boolean isUpdateRequired();

	protected boolean isGhostSlot(Slot slot) {
		return isCraftingGridSlot( slot.slotNumber );
	}

	protected int determineSpecialCase(int slotID, int flag, int data) {
		if( flag == 5 ) { // dragging stuff
			return 5;
		}

		if( dragState != 0 )
			this.func_94533_d();

		if( isCraftingGridSlot( slotID ) ) { // crafting grid slots
			if( flag == 0 || flag == 1 ) {
				// regular clicking
				return 1;
			}
			if( flag == 2 ) { // interacting with the hotkeys
				return 2;
			}
			if( flag == 4 ) { // pressing the DROP key
				return 4;
			}
			if( flag == 6 ) { // double clicking.
				return 6;
			}
			return 0;
		}


		if( slotID >= 0 && getSlot( slotID ) instanceof SlotCraft ) {
			if( flag == 0 ) {
				if( data == 1 ) { // redirect the right click into a left click.
					return 10;    // because we don't want to split crafted stacks.
				}
				return 11; // regular click on an output slot.
			}
			if( flag == 2 ) { // hotkeys interaction.
				return 12;
			}
			if( flag == 4 ) { // dropping from an output slot.
				return 14;
			}
		}

		return 0; // normal behavior.
	}

	public void handleDragging(int slotID, int data, InventoryPlayer playerInventory) {
		int lastAction = this.dragState;
		this.dragState = data & 3;

		if( (lastAction != 1 || this.dragState != 2) && lastAction != this.dragState ) {
			this.func_94533_d(); // clear everything.
		} else if( playerInventory.getItemStack() == null ) {
			this.func_94533_d();

		} else if( this.dragState == 0 ) { // prepare everything.
			this.dragButton = (data >> 2) & 3;
			if( func_94528_d( this.dragButton ) ) { // checks if dragButton is 0 or 1
				this.dragState = 1;
				this.draggedSlots.clear();
			} else {
				this.func_94533_d();
			}

		} else if( this.dragState == 1 ) { // add slot to the list.
			Slot slot = (Slot) this.inventorySlots.get( slotID );
			if( slot == null )
				return;

			if( func_94527_a( slot, playerInventory.getItemStack(), true ) && slot.isItemValid( playerInventory.getItemStack() )
					&& playerInventory.getItemStack().stackSize > this.draggedSlots.size() && this.canDragIntoSlot( slot ) ) {

				this.draggedSlots.add( slot );
			}

		} else if( this.dragState == 2 ) {
			if( !this.draggedSlots.isEmpty() ) {
				ItemStack playerStack = playerInventory.getItemStack().copy();
				int stackSize = playerInventory.getItemStack().stackSize;
				int trimmedSize = getTrimmedStackSize( playerStack );

				for( Slot slot : draggedSlots ) {
					if( slot == null )
						continue;

					if( func_94527_a( slot, playerInventory.getItemStack(), true ) && slot.isItemValid( playerStack )
							&& playerStack.stackSize >= this.draggedSlots.size() && this.canDragIntoSlot( slot ) ) {

						ItemStack itemStack = playerStack.copy();
						if( isGhostSlot( slot ) ) {
							itemStack.stackSize = 1;
							slot.putStack( itemStack );
							continue;
						}

						int slotStackSize = slot.getHasStack() ? slot.getStack().stackSize : 0;
						itemStack.stackSize = trimmedSize + slotStackSize;

						int max = Math.min( itemStack.getMaxStackSize(), slot.getSlotStackLimit() );
						if( itemStack.stackSize > max ) {
							itemStack.stackSize = max;
						}

						stackSize -= itemStack.stackSize - slotStackSize;
						slot.putStack( itemStack );
					}
				}

				playerStack.stackSize = stackSize;

				if( playerStack.stackSize <= 0 ) {
					playerStack = null;
				}

				playerInventory.setItemStack( playerStack );
			}

			this.func_94533_d();
		} else {
			this.func_94533_d();
		}
	}

	private int getTrimmedStackSize(ItemStack itemStack) {
		int realSlots = 0;
		for( Slot slot : draggedSlots ) {
			if( !isGhostSlot( slot ) )
				realSlots++;
		}

		int size = 0;
		if( dragButton == 0 ) { // left.
			size = MathHelper.floor_float( (float) itemStack.stackSize / (float) realSlots );
		} else if( dragButton == 1 ) { // right
			size = 1;
		}

		return size;
	}

	@Override
	public ItemStack slotClick(int slotID, int buttonPressed, int flag, EntityPlayer player) {
		try {
			int specialCase = determineSpecialCase( slotID, flag, buttonPressed );
			if( specialCase == 0 )
				return super.slotClick( slotID, buttonPressed, flag, player );

			if( isUpdateRequired() )
				onContentsChanged();

			Slot slot = slotID < 0 ? null : getSlot( slotID );

			ItemStack stackInSlot;
			ItemStack playerStack;

			InventoryPlayer inventoryPlayer = player.inventory;

			switch( specialCase ) {
				case 1: // clicking on the crafting grid slots:
					if( flag == 1 ) { // clear on shift-clicking.
						slot.putStack( null );
						return null;
					}

					playerStack = inventoryPlayer.getItemStack();

					if( buttonPressed == 0 || playerStack == null ) {
						slot.putStack( null );

					} else if( buttonPressed == 1 ) {
						ItemStack copy = playerStack.copy();
						copy.stackSize = 1;
						slot.putStack( copy );
					}
					return null;

				case 2: // interacting with the hotkeys:
					ItemStack invStack = inventoryPlayer.getStackInSlot( buttonPressed );
					if( invStack != null ) {
						ItemStack copy = invStack.copy();
						copy.stackSize = 1;
						slot.putStack( copy );
					}
					return invStack;

				case 4: // pressing the DROP key.
					stackInSlot = slot.getStack();
					slot.putStack( null );
					return stackInSlot;

				case 5: // placing the dragged stuff.
					handleDragging( slotID, buttonPressed, player.inventory );

					this.detectAndSendChanges();
					break;

				case 6: // double click (clears the crafting grid).
					clearCraftingGrid();
					this.detectAndSendChanges();
					return null;

				case 10: // redirect the right click into a left click.
					return slotClick( slotID, 0, flag, player );

				case 11: // regular clicking on an output slot.

					if( !slot.getHasStack() || !slot.canTakeStack( player ) )
						return null;

					stackInSlot = ((SlotCraft) slot).getCraftedStack();
					playerStack = inventoryPlayer.getItemStack();

					if( playerStack == null ) { // Full extraction from slot.
						inventoryPlayer.setItemStack( stackInSlot );
						slot.onPickupFromSlot( player, inventoryPlayer.getItemStack() );
					} else {
						int sum = stackInSlot.stackSize + playerStack.stackSize;

						// Merge into player's hand.
						if( Utils.equalsStacks( stackInSlot, playerStack ) && sum <= stackInSlot.getMaxStackSize() ) {
							playerStack.stackSize = sum;
							slot.onPickupFromSlot( player, inventoryPlayer.getItemStack() );
						}
					}
					slot.onSlotChanged();
					return stackInSlot;

				case 12: // interacting with the hotkeys
					if( !slot.canTakeStack( player ) )
						return null;
					stackInSlot = ((SlotCraft) slot).getCraftedStack();

					if( stackInSlot == null )
					return null;

					playerStack = inventoryPlayer.getStackInSlot( buttonPressed );
					if( playerStack == null ) {
						inventoryPlayer.setInventorySlotContents( buttonPressed, stackInSlot );
						slot.onPickupFromSlot( player, stackInSlot );
					} else {
						int indx = inventoryPlayer.getFirstEmptyStack();
						if( indx > -1 ) {
							inventoryPlayer.setInventorySlotContents( buttonPressed, stackInSlot );
							inventoryPlayer.addItemStackToInventory( playerStack );
							slot.onPickupFromSlot( player, stackInSlot );
						}
					}
					return stackInSlot;

				case 14: // dropping from an output slot.
					if( slot.getHasStack() && slot.canTakeStack( player ) ) {
						ItemStack itemStack = ((SlotCraft) slot).getCraftedStack();
						slot.onPickupFromSlot( player, itemStack );
						player.dropPlayerItem( ((SlotCraft) slot).getCraftedStack() );
						return itemStack;
					}
					return null;
			}
			return null;
		} finally {
			onContentsChanged();
		}
	}

	private int counter = 0;
	private int counterMax = 128;
	private ItemStack lastItemStack = null;

	@Override
	protected void retrySlotClick(int slotID, int mouseClick, boolean flag, EntityPlayer player) {
		Slot slot = (Slot) this.inventorySlots.get( slotID );
		if( slot instanceof SlotCraft ) {
			if( mouseClick == 1 )
				return;
		}

		// Have to limit the amount of crafts that can be done in a single shit-click,
		// to prevent StackOverflowException.

		ItemStack stackInSlot = getSlot( slotID ).getStack();
		if( lastItemStack == null || !lastItemStack.isItemEqual( stackInSlot ) ) {
			counter = 0;
			lastItemStack = stackInSlot;
		}
		if( ++counter < counterMax ) {
			this.slotClick( slotID, mouseClick, 1, player );
		} else {
			counter = 0;
		}
	}

	@Override
	protected void func_94533_d() {
		super.func_94533_d();
		dragState = 0;
		draggedSlots.clear();
	}

	protected void onContentsChanged() { }

	/**
	 * Called from the GuiTickHandler on every PLAYER tick (both sides).
	 * This might be used to update the internal state.
	 *
	 * Of course, this is only called when this container is open.
	 *
	 * @param player the EntityPlayer ticking.
	 */
	public void onTickUpdate(EntityPlayer player) { }

}
