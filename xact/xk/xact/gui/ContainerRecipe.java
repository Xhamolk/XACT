package xk.xact.gui;


import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import xk.xact.api.InteractiveCraftingContainer;

public class ContainerRecipe extends Container implements InteractiveCraftingContainer {


	private EntityPlayer player;
	public final ItemStack target; // todo: this is the objective.


	public ContainerRecipe(EntityPlayer player, ItemStack target) {
		this.player = player;
		this.target = target;
		buildContainer();
	}

	private void buildContainer() {
		// grid: 44, 24
		// output: 110, 35
		// inv: 7, 97
		// hot-bar: 7, 155


		// output slot
		this.addSlotToContainer(new Slot(null, 0, 110, 35) { // todo: replace null with IInventory
			@Override
			public boolean isItemValid(ItemStack item){
				return false;
			}
			@Override
			public boolean canTakeStack(EntityPlayer player) {
				return false;
			}
		});

		// grid
		for (int i=0; i<3; i++) {
			for (int e=0; e<3; e++) {
				this.addSlotToContainer(new Slot(null, i*3 + e, e*18 +44, i*18 +24) { // todo: replace null with IInventory
					@Override
					public boolean canTakeStack(EntityPlayer player) {
						return false;
					}
					@Override
					public void onSlotChanged() {
						// todo: update the output slot.
							// if the recipe is valid, then
					}
				});
			}
		}

		// main player inv
		for (int i=0; i<3; i++) {
			for (int e=0; e<9; e++) {
				this.addSlotToContainer(new Slot(player.inventory, (i+1)*9 +e, e*18 +8, i*18 +98));
			}
		}

		// hot-bar
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, 	i*18 +8,  156));
		}
	}



	@Override
	public ItemStack slotClick(int slotID, int buttomPressed, int flag, EntityPlayer player) {

		// Special handle for the grid slots.
		if( 1 <= slotID && slotID < 10 ) {
			Slot slot = ((Slot)this.inventorySlots.get(slotID));

			if( flag == 0 ) { // regular clicking.
				ItemStack playerStack = player.inventory.getItemStack();
				if( buttomPressed == 0 || playerStack == null ){
					slot.putStack(null);
				} else if( buttomPressed == 1 ){
					ItemStack copy = playerStack.copy();
					copy.stackSize = 1;
					slot.putStack(copy);
				}
				slot.onSlotChanged();
				return null;
			}

			if( flag == 1 )
				return null; // do nothing on shift click.

			if( flag == 2 ) { // interact with the hot-bar
				ItemStack invStack = player.inventory.getStackInSlot(buttomPressed);
				if( invStack != null ) {
					ItemStack copy = invStack.copy();
					copy.stackSize = 1;

					slot.putStack(copy);
					slot.onSlotChanged();

					return invStack;
				}

			}

			return null;
		}

		return super.slotClick(slotID, buttomPressed, flag, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return null; // disable shift-clicking.
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return true;
	}

	@Override
	public void setStack(int slotID, ItemStack stack) {
		Slot slot = (Slot) this.inventorySlots.get(slotID);
		if( slot != null ) {
			slot.putStack(stack);
		}
	}


}
