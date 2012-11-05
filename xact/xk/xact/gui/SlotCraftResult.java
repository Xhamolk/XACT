package xk.xact.gui;

import net.minecraft.src.*;
import xk.xact.TileCrafter;

/**
* The slot used to display the recipe's output on TileCrafter.
 * @author Xhamolk_
*/
public abstract class SlotCraftResult extends Slot implements SpecialSlot {

	protected int recipeIndex;
	private TileCrafter crafter;

	public SlotCraftResult(TileCrafter crafter, int recipeIndex, int x, int y) {
		super(crafter.results, recipeIndex, x, y);
		this.crafter = crafter;
		this.recipeIndex = recipeIndex;
	}

	@Override
	public ItemStack getStack() {
		if( super.getStack() != null )
			return super.getStack().copy();

//		ItemStack stack = crafter.getRecipeResult(recipeIndex);
//		if( stack != null )
//			return stack.copy();
		return null;
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return false; // don't allow any item to be placed in here.
	}

	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
		// client side?
		if( player instanceof EntityClientPlayerMP )
			return; // this should only be handled server-side.

		// handle crafting event.
		fireCraftEvent(stack);

		// Replace the stack.
		super.putStack(crafter.getRecipeResult(recipeIndex));
        // todo: maybe call the update?
	}

	public abstract void fireCraftEvent(ItemStack stack);


	///////////////
	///// SpecialSlot

	@Override
	public boolean allowPickUp() {
//		return !crafter.isRedState(recipeIndex); // todo: re-implement.
		return crafter.canCraftRecipe(recipeIndex);
	}


}
