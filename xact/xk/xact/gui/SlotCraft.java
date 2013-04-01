package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.CraftingHandler;
import xk.xact.api.ICraftingDevice;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.FakeCraftingInventory;
import xk.xact.util.Utils;

/**
 * The slot used to display the recipe's output on TileCrafter.
 *
 * @author Xhamolk_
 */
public class SlotCraft extends Slot {


	private CraftingHandler handler;
	private ICraftingDevice device;
	private EntityPlayer player;

	private CraftRecipe recipe;
	private boolean canCraft;

	public SlotCraft(ICraftingDevice device, IInventory displayInventory, EntityPlayer player, int index, int x, int y) {
		super( displayInventory, index, x, y );
		this.player = player;
		this.device = device;
		this.handler = device.getHandler();
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack getStack() {
		return Utils.copyOf( super.getStack() );
	}

	public ItemStack getCraftedStack() {
		if( recipe == null )
			return null;

		FakeCraftingInventory grid = handler.generateTemporaryCraftingGridFor( recipe, player, false );
		ItemStack craftedItem = handler.getRecipeResult( recipe, grid );

		return craftedItem == null ? null : craftedItem.copy();
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
		return canCraft;
//		return handler.canCraft( recipe, player );
	}


	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack craftedItem) {
		if( player.capabilities.isCreativeMode || craftedItem == null )
			return;

		if( recipe == null ) return;
		handler.doCraft( recipe, player, craftedItem );
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		updateSlot();
	}

	private CraftRecipe getRecipe() {
		try {
			return device.getRecipe( getSlotIndex() );
		} catch( Exception e ) {
			return null;
		}
	}

	public void updateSlot() {
		recipe = getRecipe();
		canCraft = recipe != null && handler.canCraft( recipe, player );
		ItemStack item = recipe == null ? null : recipe.getResult();
		this.inventory.setInventorySlotContents( getSlotIndex(), item );
	}

}
