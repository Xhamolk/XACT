package xk.xact.util;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;

import java.util.ArrayList;

/**
 * Used to compare an array of ingredients the with the recipes.
 */
public class FakeCraftingInventory extends InventoryCrafting { // todo: javadoc

	public FakeCraftingInventory() {
		super(new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer var1) {
				return false;
			}
		}, 3, 3);
		
	}

	/**
	 * Fills the crafting grid with the specified ingredients.
	 * Note: fill order is right then down.
	 *
	 * @param ingredients a ItemStack[9] containing all the ingredients.
	 */
	public void setContents(ItemStack[] ingredients){
		for(int i=0; i<9; i++) {
			setInventorySlotContents(i, ingredients.length > i ? ingredients[i] : null );
		}
	}

	/**
	 * Clears the crafting grid.
	 */
	public void cleanContents() {
		for(int i=0; i<9; i++) {
			setInventorySlotContents(i, null);
		}
	}

	/**
	 * Creates a fake crafting grid with the specified ingredients,
	 * so it can be compared with the IRecipe.
	 * @param ingredients the
	 * @return a FakeCraftingInventory containing the specified ingredients.
	 */
	public static FakeCraftingInventory emulateContents(ItemStack[] ingredients){
		FakeCraftingInventory fake = new FakeCraftingInventory();
			fake.setContents(ingredients);
		return fake;
	}

	@Override
	public void onInventoryChanged(){
		super.onInventoryChanged();
		for(int i=0; i<9; i++) {
			ItemStack stack = this.getStackInSlot(i);
			if( stack != null && stack.stackSize == 0 )
				this.setInventorySlotContents(i, null);
		}
	}


	public ItemStack[] getContents() {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(int i=0; i<9; i++) {
			ItemStack temp = this.getStackInSlot(i);
			if( temp != null )
				list.add(temp);
		}

		return list.toArray(new ItemStack[list.size()]);
	}
}
