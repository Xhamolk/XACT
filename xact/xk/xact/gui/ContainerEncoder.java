package xk.xact.gui;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import xk.xact.ItemRecipe;
import xk.xact.TileEncoder;
import xk.xact.recipes.CraftManager;

public class ContainerEncoder extends ContainerMachine {

	private TileEncoder encoder;
	private EntityPlayer player;

	public ContainerEncoder(TileEncoder encoder, EntityPlayer player) {
		this.encoder = encoder;
		this.player = player;
		buildContainer();
	}

	private void buildContainer() {
		// Note: the crafting result is drawn by GuiEncoder

		// crafting grid
		for(int i=0; i<3; i++){
			for(int e=0; e<3; e++){
				int x = (26 + 18*e), y = (18 + 18*i);
				addSlotToContainer(new Slot(encoder.craftingGrid, i*3 + e, x, y));
			}
		}

		// circuit slot
		addSlotToContainer(new SlotRestricted(encoder.circuitInv, 0, 133, 49) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return CraftManager.isValid(stack);
			}
		});

		// player's inventory
		for(int i=0; i<3; i++) {
			for(int e=0; e<9; e++){
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
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID){
		Slot slot = (Slot) inventorySlots.get(slotID);
		
		if( slot == null || !slot.getHasStack() )
			return null;
		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		// From the encoder to the player's inventory.
		if( slotID < 10 ) {
			if (!mergeItemStack(stackInSlot, 10, inventorySlots.size(), true)) 
				return null;
		} else { // From the player's inventory to the encoder.

			// Chips by default go to the chip slot.
			if( stackInSlot.getItem() instanceof ItemRecipe ){
                // TODO: make sure only one chip is added.

				if (!mergeItemStack(stackInSlot, 9, 10, false))
					return null;
			} else { // any other item goes to the crafting grid.
				if (!mergeItemStack(stackInSlot, 0, 9, false))
					return null;
			}
		}

		if ( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		} else {
			slot.onSlotChanged();
		}
		
		
		return stack;
	}

}
