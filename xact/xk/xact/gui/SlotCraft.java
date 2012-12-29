package xk.xact.gui;


import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
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
	private EntityPlayer player;

	public SlotCraft(ICraftingDevice device, IInventory displayInventory, EntityPlayer player, int index, int x, int y) {
		super(displayInventory, index, x, y);
		this.player = player;
		this.device = device;
		this.handler = device.getHandler();
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return false;
	}

	public ItemStack getCraftedStack() {
		CraftRecipe recipe = getRecipe();
		if( recipe == null )
			return null;

		FakeCraftingInventory grid = handler.generateTemporaryCraftingGridFor(recipe, player, false);
		ItemStack craftedItem = handler.getRecipeResult(recipe, grid);

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
		CraftRecipe recipe = getRecipe();
		if( recipe != null ) {
			if( handler.canCraft(recipe, player) )
				return true;
		}
		return false;
	}


	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack craftedItem) {
		if( player.capabilities.isCreativeMode || craftedItem == null )
			return;

		CraftRecipe recipe = getRecipe();
		if( recipe == null ) return;

		FakeCraftingInventory craftMatrix = handler.generateTemporaryCraftingGridFor(recipe, player, true);

		craftedItem.onCrafting(player.worldObj, player, craftedItem.stackSize);
		GameRegistry.onItemCrafted(player, craftedItem, craftMatrix);

		handler.consumeIngredients(craftMatrix, player);
	}

	@Override
	public void onSlotChanged() {
		CraftRecipe recipe = getRecipe();
		ItemStack item = recipe == null ? null : recipe.getResult();
		this.inventory.setInventorySlotContents(getSlotIndex(), item);
		super.onSlotChanged();
	}

	private CraftRecipe getRecipe() {
		try{
			return device.getRecipe(getSlotIndex());
		}catch(Exception e) {
			return null;
		}
	}

}
