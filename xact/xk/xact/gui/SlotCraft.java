package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.recipes.CraftRecipe;
import xk.xact.api.ICraftingDevice;
import xk.xact.api.CraftingHandler;
import xk.xact.util.FakeCraftingInventory;

/**
 * The slot used to display the recipe's output on TileCrafter.
 * @author Xhamolk_
 */
public class SlotCraft extends Slot {


	private CraftingHandler handler;
	private ICraftingDevice device;

	private FakeCraftingInventory currentGrid = null;

	public SlotCraft(ICraftingDevice device, IInventory displayInventory, int index, int x, int y) {
		super(displayInventory, index, x, y);
		this.device = device;
		this.handler = device.getHandler();
	}


	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack getStack() { // this is only the one to show.
		try {
			return getRecipe().getResult();
		}catch(Exception e) {
			return null;
		}
	}

	public ItemStack getCraftedStack(EntityPlayer player) {
		CraftRecipe recipe = getRecipe();
		if( recipe == null )
			return null;

		currentGrid = handler.generateTemporaryCraftingGridFor(recipe, player);
		return handler.doCraft(recipe, player, currentGrid);
	}

	@Override
	public int getSlotStackLimit() {
		return 64;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		return this.getStack();
	}


	@Override
	public boolean canTakeStack(EntityPlayer player) {
		CraftRecipe recipe = getRecipe();
		if( recipe != null ) {
			if( handler.canCraft(recipe, player) )
				return true;
			if( player.worldObj.isRemote )
				player.sendChatToPlayer("Can't craft "+recipe+". Missing: "+handler.getMissingIngredientsString(recipe));
		}
		return false;
	}


	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {
		CraftRecipe recipe = getRecipe();
		if( recipe == null ) return;

		if( player.capabilities.isCreativeMode )
			currentGrid = null;

		if( currentGrid != null ) {
			handler.consumeIngredients(currentGrid, player);
			currentGrid = null;
		}
		// putStack(itemStack);
	}


	private CraftRecipe getRecipe() {
		try{
			return device.getRecipe(getSlotIndex());
		}catch(Exception e) {
			return null;
		}
	}

}
