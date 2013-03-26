package xk.xact.inventory;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class UpdateContainer extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public ItemStack slotClick(int slotID, int button, int flag, EntityPlayer player) {
		InventoryPlayer playerInventory = player.inventory;


		// I don't know what is this.
		if( flag == 5 ) {
			handleDragging( player, slotID, button );
			return null;
		}

		// Clicking outside of the GUI area.
		if( slotID == -999 && ( flag == 0 || flag == 1 ) ) {
			if( playerInventory.getItemStack() != null ) {

				if( button == 0 ) { // Drop the entire stack
					player.dropPlayerItem( playerInventory.getItemStack() );
					playerInventory.setItemStack( null );
				}

				if( button == 1 ) { // Drop just one item.
					player.dropPlayerItem( playerInventory.getItemStack().splitStack( 1 ) );

					if( playerInventory.getItemStack().stackSize == 0 ) {
						playerInventory.setItemStack( null );
					}
				}
			}
			return null;
		}

		if( this.sentMode_flag5 != 0 ) { // ???
			this.clearFlag5();
			return null;
		}

		// Regular Clicking.
		if( flag == 0 ) {
			return handleRegularClick( player, slotID, button );
		}

		// Shift-clicking
		if( flag == 1 ) {
			return handleShiftClick( player, slotID, button );
		}

		// Hotkeys
		if( flag == 2 && button >= 0 && button < 9 ) {
			handleHotKeyClick( player, slotID, button );
			return null;
		}
		// Creative stuff?
		if( flag == 3 && player.capabilities.isCreativeMode && playerInventory.getItemStack() == null && slotID >= 0 ) {
			handleCreativeClick( player, slotID, button );
			return null;
		}

		// Drops items when Q is pressed
		if( flag == 4 && playerInventory.getItemStack() == null && slotID >= 0 ) {
			handledFlag4( player, slotID, button == 1 ); // button == 1 means is control pressed.
			return null;
		}


		// I don't know what is this.
		if( flag == 6 && slotID >= 0 ) {
			handleFlag6( player, slotID, button );
		}

		return null;
	}

	// Flag 0: Regular left/right clicks.
	private ItemStack handleRegularClick(EntityPlayer player, int slotID, int button) {
		if( slotID < 0 ) return null;

		Slot slot = (Slot) this.inventorySlots.get( slotID );
		if( slot == null ) return null;

		ItemStack returnStack = null;
		InventoryPlayer playerInventory = player.inventory;
		ItemStack slotStack = slot.getStack();
		ItemStack playerStack = playerInventory.getItemStack();

		if( slotStack != null ) {
			returnStack = slotStack.copy();
		}

		if( slotStack == null ) {
			// Place item on the (empty) slot.
			if( playerStack != null && slot.isItemValid( playerStack ) ) {
				int count = button == 0 ? playerStack.stackSize : 1;

				if( count > slot.getSlotStackLimit() ) {
					count = slot.getSlotStackLimit();
				}

				slot.putStack( playerStack.splitStack( count ) );

				if( playerStack.stackSize == 0 ) {
					playerInventory.setItemStack( null );
				}
			}

		} else if( slot.canTakeStack( player ) ) {

			// Take the entire stack.
			if( playerStack == null ) {
				int count = button == 0 ? slotStack.stackSize : ( slotStack.stackSize + 1 ) / 2;
				ItemStack stackTaken = slot.decrStackSize( count );
				playerInventory.setItemStack( stackTaken );

				if( slotStack.stackSize == 0 ) {
					slot.putStack( null );
				}

				slot.onPickupFromSlot( player, playerInventory.getItemStack() );

				// Merge into the slot.
			} else if( slot.isItemValid( playerStack ) ) {

				// matching stacks - merge into slot
				if( slotStack.itemID == playerStack.itemID && slotStack.getItemDamage() == playerStack.getItemDamage() && ItemStack.areItemStackTagsEqual( slotStack, playerStack ) ) {

					int max = Math.min( slot.getSlotStackLimit(), playerStack.getMaxStackSize() );
					int count = button == 0 ? playerStack.stackSize : 1;
					count = Math.min( count, max - slotStack.stackSize );

					playerStack.splitStack( count );
					slotStack.stackSize += count;

					if( playerStack.stackSize == 0 )
						playerInventory.setItemStack( null );

					// Swap	stacks.
				} else if( playerStack.stackSize <= slot.getSlotStackLimit() ) {
					slot.putStack( playerStack );
					playerInventory.setItemStack( slotStack );
				}

			} else if( slotStack.itemID == playerStack.itemID && playerStack.getMaxStackSize() > 1 && ( !slotStack.getHasSubtypes() || slotStack.getItemDamage() == playerStack.getItemDamage() ) && ItemStack.areItemStackTagsEqual( slotStack, playerStack ) ) {
				int stackSize = slotStack.stackSize;

				// merge into the player's hand.
				if( stackSize > 0 && stackSize + playerStack.stackSize <= playerStack.getMaxStackSize() ) {
					playerStack.stackSize += stackSize;
					slotStack = slot.decrStackSize( stackSize );

					if( slotStack.stackSize == 0 ) {
						slot.putStack( null );
					}

					slot.onPickupFromSlot( player, playerInventory.getItemStack() );
				}
			}
		}
		slot.onSlotChanged();

		return returnStack;
	}

	// Flag 1: Shift click
	private ItemStack handleShiftClick(EntityPlayer player, int slotID, int buttonPressed) {
		if( slotID < 0 )
			return null;

		Slot slot = (Slot) this.inventorySlots.get( slotID );
		ItemStack returnStack = null;

		if( slot != null && slot.canTakeStack( player ) ) {
			ItemStack transferStack = this.transferStackInSlot( player, slotID );

			if( transferStack != null ) {
				int itemID = transferStack.itemID;
				returnStack = transferStack.copy();

				if( slot.getHasStack() && slot.getStack().itemID == itemID ) {
					this.retrySlotClick( slotID, buttonPressed, true, player );
				}
			}
		}
		return returnStack;
	}

	// Flag 2: HotKey pressed.
	private void handleHotKeyClick(EntityPlayer player, int slotID, int hotKey) { // 0, 9 // 10, 0

		Slot targetSlot = (Slot) this.inventorySlots.get( slotID );
		InventoryPlayer playerInventory = player.inventory;

		if( targetSlot.canTakeStack( player ) ) {
			ItemStack pickedStack = playerInventory.getStackInSlot( hotKey );

			boolean safe = pickedStack == null || targetSlot.inventory == playerInventory && targetSlot.isItemValid( pickedStack );
			int k1 = -1;

			if( !safe ) {
				k1 = playerInventory.getFirstEmptyStack();
				safe = k1 > -1;
			}

			if( targetSlot.getHasStack() && safe ) {
				ItemStack slotStack = targetSlot.getStack();
				playerInventory.setInventorySlotContents( hotKey, slotStack );

				if( ( targetSlot.inventory != playerInventory || !targetSlot.isItemValid( pickedStack ) ) && pickedStack != null ) {
					if( k1 > -1 ) {
						playerInventory.addItemStackToInventory( pickedStack );
						targetSlot.decrStackSize( slotStack.stackSize );
						targetSlot.putStack( null );
						targetSlot.onPickupFromSlot( player, slotStack );
					}
				} else {
					targetSlot.decrStackSize( slotStack.stackSize );
					targetSlot.putStack( pickedStack );
					targetSlot.onPickupFromSlot( player, slotStack );
				}
			} else if( !targetSlot.getHasStack() && pickedStack != null && targetSlot.isItemValid( pickedStack ) ) {
				playerInventory.setInventorySlotContents( hotKey, null );
				targetSlot.putStack( pickedStack );
			}

		}
	}

	// Flag 3: Creative click
	private void handleCreativeClick(EntityPlayer player, int slotID, int button) {
		Slot slot = (Slot) this.inventorySlots.get( slotID );

		if( slot != null && slot.getHasStack() ) {
			ItemStack tempStack = slot.getStack().copy();
			tempStack.stackSize = button == 0 ? tempStack.getMaxStackSize() : 1;
			player.inventory.setItemStack( tempStack );
		}
	}

	// Flag 4: Press Q to drop items.
	private void handledFlag4(EntityPlayer player, int slotID, boolean dropAll) {
		Slot slot = (Slot) this.inventorySlots.get( slotID );

		if( slot != null && slot.getHasStack() ) { // something like pick up.
			ItemStack tempStack = slot.decrStackSize( dropAll ? 1 : slot.getStack().stackSize );
			slot.onPickupFromSlot( player, tempStack );
			player.dropPlayerItem( tempStack );
		}
	}

	// Flag 5: Used for the hold-drag-click functionality
	private void handleDragging(EntityPlayer player, int slotID, int data) {
		InventoryPlayer playerInventory = player.inventory;

		int lastMode = this.sentMode_flag5;
		this.sentMode_flag5 = decompressMode( data );

		if( ( lastMode != 1 || this.sentMode_flag5 != 2 ) && lastMode != this.sentMode_flag5 ) {
			this.clearFlag5(); // corruption?

		} else if( playerInventory.getItemStack() == null ) {
			this.clearFlag5(); // corruption?

		} else if( this.sentMode_flag5 == 0 ) { // start the "drag"

			this.field_94535_f = decompressButton( data ); // get current button.

			if( func_94528_d( this.field_94535_f ) ) { // is legal button: 0 or 1.
				// Get ready to receive the "dragged" stacks.

				this.sentMode_flag5 = 1; // prepare for receiving the next instructions.
				this.field_94537_h.clear(); // clear the last set.
			} else {
				this.clearFlag5(); // Clear flag5 stuff
			}

		} else if( this.sentMode_flag5 == 1 ) { // receive a "dragged" stack.
			Slot slot = (Slot) this.inventorySlots.get( slotID );

			if( slot != null && canFitItemInSlot( slot, playerInventory.getItemStack(), true ) && slot.isItemValid( playerInventory.getItemStack() ) && playerInventory.getItemStack().stackSize > this.field_94537_h.size() && this.slotAcceptsDraggedItems( slot ) ) {
				this.field_94537_h.add( slot ); // add it to a list.
			}

		} else if( this.sentMode_flag5 == 2 ) { // actually take in the dragged stacks..

			if( !this.field_94537_h.isEmpty() ) {
				ItemStack originalStack = playerInventory.getItemStack().copy();
				int stackSize = playerInventory.getItemStack().stackSize;

				for( Slot currentSlot : (Set<Slot>) this.field_94537_h ) {
					if( currentSlot == null )
						continue;

					if( canFitItemInSlot( currentSlot, playerInventory.getItemStack(), true ) && currentSlot.isItemValid( playerInventory.getItemStack() ) ) {
						if( playerInventory.getItemStack().stackSize >= this.field_94537_h.size() && this.slotAcceptsDraggedItems( currentSlot ) ) {
							ItemStack temp = originalStack.copy();
							int j1 = currentSlot.getHasStack() ? currentSlot.getStack().stackSize : 0;
							trimStack( this.field_94537_h, this.field_94535_f, temp, j1 );

							if( temp.stackSize > temp.getMaxStackSize() ) {
								temp.stackSize = temp.getMaxStackSize();
							}

							if( temp.stackSize > currentSlot.getSlotStackLimit() ) {
								temp.stackSize = currentSlot.getSlotStackLimit();
							}

							stackSize -= temp.stackSize - j1;
							currentSlot.putStack( temp );
						}
					}
				}

				originalStack.stackSize = stackSize;

				if( originalStack.stackSize <= 0 ) {
					originalStack = null;
				}

				playerInventory.setItemStack( originalStack );
			}

			this.clearFlag5();
		} else {
			this.clearFlag5();
		}
	}

	// Flag 6: double click!
	private void handleFlag6(EntityPlayer player, int slotID, int button) {
		Slot slot = (Slot) this.inventorySlots.get( slotID ); // what is this slot?
		ItemStack playerStack = player.inventory.getItemStack();

		if( playerStack != null && ( slot == null || !slot.getHasStack() || !slot.canTakeStack( player ) ) ) {
			int startValue = button == 0 ? 0 : this.inventorySlots.size() - 1;
			int change = button == 0 ? 1 : -1;

			for( int i = 0; i < 2; ++i ) {
				for( int e = startValue; e >= 0 && e < this.inventorySlots.size() && playerStack.stackSize < playerStack.getMaxStackSize(); e += change ) {
					Slot otherSlot = (Slot) this.inventorySlots.get( e );

					if( otherSlot.getHasStack() && canFitItemInSlot( otherSlot, playerStack, true ) && otherSlot.canTakeStack( player ) && this.func_94530_a( playerStack, otherSlot ) && ( i != 0 || otherSlot.getStack().stackSize != otherSlot.getStack().getMaxStackSize() ) ) {
						int j2 = Math.min( playerStack.getMaxStackSize() - playerStack.stackSize, otherSlot.getStack().stackSize );
						ItemStack itemstack5 = otherSlot.decrStackSize( j2 );
						playerStack.stackSize += j2;

						if( itemstack5.stackSize <= 0 )
							otherSlot.putStack( null );

						otherSlot.onPickupFromSlot( player, itemstack5 );
					}
				}
			}
		}

		this.detectAndSendChanges();
	}

}
