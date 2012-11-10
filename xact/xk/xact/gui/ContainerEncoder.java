package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.ItemRecipe;
import xk.xact.TileEncoder;

public class ContainerEncoder extends ContainerMachine {

	private TileEncoder encoder;
	private EntityPlayer player;

	public ContainerEncoder(TileEncoder encoder, EntityPlayer player) {
		this.encoder = encoder;
		this.player = player;
		buildContainer();
	}

	private void buildContainer() {
		// crafting grid
		for(int i=0; i<3; i++){
			for(int e=0; e<3; e++){
				int x = (26 + 18*e), y = (18 + 18*i);
				addSlotToContainer(new Slot(encoder.craftingGrid, i*3 + e, x, y));
			}
		}

		// circuit slot
		addSlotToContainer(new SlotEncode(encoder, 133, 49));

		// output slot.
		addSlotToContainer(new SlotCrafting(player, encoder.craftingGrid, encoder.outputInv, 0, 98, 21){
			@Override
			public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
				super.onPickupFromSlot(player, stack);
				encoder.craftingGrid.onInventoryChanged();
			}
		});

		// player's inventory
		for(int i=0; i<3; i++) {
			for(int e=0; e<9; e++) {
				int x = 18*e + 8, y = 18*i + 84;
				addSlotToContainer(new Slot(player.inventory, e + i*9 + 9, x, y));
			}
		}
		// player's hot bar
		for(int i=0; i<9; i++){
			addSlotToContainer(new Slot(player.inventory, i, 18*i + 8, 142));
		}

		encoder.updateRecipe();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		Slot slot = (Slot) inventorySlots.get(slotID);
		
		if( slot == null || !slot.getHasStack() )
			return null;
		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		if( slot instanceof SlotCrafting ) {
			// add to the player's inventory.
			if (mergeItemStack(stackInSlot, 11, inventorySlots.size(), false)) {
				slot.onPickupFromSlot(player, stack);
				slot.onSlotChanged();
				return stack;
			}
			return null;
		}

		if( slot instanceof SlotEncode ) {
			if (mergeItemStack(stackInSlot, 11, inventorySlots.size(), false)) {
				slot.onPickupFromSlot(player, stackInSlot);
				slot.onSlotChanged();
			}
			return null;
		}

		// From the encoder to the player's inventory.
		if( slotID < 11 ) {
			if (!mergeItemStack(stackInSlot, 11, inventorySlots.size(), false))
				return null;
		} else { // From the player's inventory to the encoder.

			// Chips by default go to the chip slot.
			if( stackInSlot.getItem() instanceof ItemRecipe ){
				if( encoder.circuitInv.getStackInSlot(0) != null )
					return null; // don't let the chips to stack.

                if( stackInSlot.stackSize > 1 ) {
					ItemStack s = stackInSlot.copy();
					s.stackSize = 1;
					if (!mergeItemStack(s, 9, 10, false))
						return null;
					slot.decrStackSize(1);
				} else {
					if (!mergeItemStack(stackInSlot, 9, 10, false))
						return null;
				}
			} else { // any other item goes to the crafting grid.
				if (!mergeItemStack(stackInSlot, 0, 9, false))
					return null;
			}
		}

		if ( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		}
		slot.onSlotChanged();
		
		return stack;
	}

	@Override
	protected void retrySlotClick(int slotID, int mouseButtom, boolean shift, EntityPlayer player) {
		Slot slot = (Slot) this.inventorySlots.get(slotID);
		if( slot == null )
			return;
		if( slot instanceof SlotCrafting ) {
			if( mouseButtom == 1 ) // right clicking.
				return;
			// left clicking will craft a stack.
		} else  if( slot.getHasStack() && slot.getStack().getItem() instanceof ItemRecipe )
			return;
		this.slotClick(slotID, mouseButtom, 1, player);
	}

}
