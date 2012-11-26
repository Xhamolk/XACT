package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.recipes.CraftManager;

public class ContainerLibrary extends Container {


	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return true;
	}

	// storage: 78, 9 (slots 6x5)
	// inv: 17, 108
	// hot-bar: 17, 166

	private void buildContainer(IInventory storage, IInventory playerInv){

		// storage
		for (int i=0; i<5; i++) {
			for (int e=0; e<6; e++) {
				this.addSlotToContainer(new Slot(storage, i*6 +e, e*18 +78, i*18 +9){
					@Override
					public boolean isItemValid(ItemStack stack){
						return CraftManager.isEncoded(stack);
					}

					@Override
					public int getSlotStackLimit() {
						return 1;
					}
				});
			}
		}

		// main player inv
		for (int i=0; i<3; i++) {
			for (int e=0; e<9; e++) {
				this.addSlotToContainer(new Slot(playerInv, (i+1)*9 +e, e*18 +17, i*18 +108));
			}
		}

		// hot-bar
		for (int i=0; i<9; i++) {
			this.addSlotToContainer(new Slot(playerInv, i, 	i*18 +17,  166));
		}

	}


}
