package xk.xact.gui;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import xk.xact.ItemRecipe;
import xk.xact.TileCrafter;
import xk.xact.event.CraftEvent;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;


/**
 * The container used for the Crafter's GUI.
 */
public class ContainerCrafter extends ContainerMachine {

	private TileCrafter crafter;
	
//	private Inventory recipes = new Inventory(4, "Recipes");
	private EntityPlayer player;

	public ContainerCrafter(TileCrafter crafter, EntityPlayer player) {
		this.crafter = crafter;
		this.player = player;
		buildContainer();
	}


	private void buildContainer() {
		// craft results
		for(int i=0; i<4; i++) {
			addSlotToContainer(new SlotCraftResult(crafter, i, 26 + 36*i, 25){
				@Override
				public void fireCraftEvent(ItemStack stack) { // this only gets called server-side
					CraftRecipe recipe = crafter.getRecipeAt(this.recipeIndex);
					CraftEvent event = new CraftEvent(player, recipe, stack);
					crafter.handleEvent(event);
					crafter.updateRecipes();
					crafter.updateStates();
				}
			});
		}

		// circuits
		for(int i=0; i<4; i++){
			addSlotToContainer(new SlotRestricted(crafter.circuits, i, 26 + 36*i, 47) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return CraftManager.isEncoded(stack);
				}

			});
		}

		// resources
		for(int i=0; i<2; i++){
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 71;
				addSlotToContainer(new Slot(crafter.resources, e + i*9, x, y));
			}
		}
		
		// player's inventory
		for(int i=0; i<3; i++) {
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 117;
				addSlotToContainer(new Slot(player.inventory, e + i*9 + 9, x, y));
			}
		}
		// player's hot bar
		for(int i=0; i<9; i++){
			addSlotToContainer(new Slot(player.inventory, i, 18*i + 8, 175));
		}
		
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID){
		Slot slot = (Slot) inventorySlots.get(slotID);

		if( slot == null || !slot.getHasStack() )
			return null;
		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		// From the crafter to the resources buffer.
		if( slotID < 8 ) {
			if (!mergeItemStack(stackInSlot, 8, 18+8, false))
				return null;
			
		} else if( slotID < 8+18 ){ // from the resources buffer
			// chips first try to go to the chip slots.
			if( stackInSlot.getItem() instanceof ItemRecipe){
				if (!mergeItemStack(stackInSlot, 4, 8, false)) // try add to the chip slots.
					if (!mergeItemStack(stackInSlot, 8+18, inventorySlots.size(), false)) // add to the player's inv.
						return null;

			} else { // any other item goes to the player's inventory.
				if (!mergeItemStack(stackInSlot, 8+18, inventorySlots.size(), false))
					return null;
			}

		} else { // From the player's inventory to the resources buffer.
			if (!mergeItemStack(stackInSlot, 8, 8+18, false))
				return null;
		}

		if ( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		}
        slot.onSlotChanged();

		return stack;
	}
}
