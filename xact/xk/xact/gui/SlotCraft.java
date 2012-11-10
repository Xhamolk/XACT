package xk.xact.gui;


import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import xk.xact.TileCrafter;
import xk.xact.recipes.CraftRecipe;

/**
 * The slot used to display the recipe's output on TileCrafter.
 * @author Xhamolk_
 */
public class SlotCraft extends Slot {


	private final int slotIndex;
	private TileCrafter crafter;


	public SlotCraft(TileCrafter crafter, int index, int x, int y) {
		super(crafter.results, index, x, y);
		this.slotIndex = index;
		this.crafter = crafter;
	}


	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return false;
	}


	@Override
	public ItemStack getStack() {
		try {
			return getRecipe().getResult();
		}catch(Exception e) {
			return null;
		}
	}


	@Override
	public ItemStack decrStackSize(int amount) {
		return this.getStack();
	}


	@Override
	public boolean canTakeStack(EntityPlayer player) {
		CraftRecipe recipe = crafter.getRecipeAt(slotIndex);
		if( recipe == null )
			return false;
		if( !crafter.canCraftRecipe(slotIndex) ){
			if(!player.worldObj.isRemote) {
				String missing = crafter.getMissingIngredients(recipe);
				player.sendChatToPlayer("Can't craft "+recipe+". Missing: "+ missing);
			}
			return false;
		}
		return true;
	}


	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {
		CraftRecipe recipe = getRecipe();
		if( recipe == null ) return;

		InventoryCrafting craftMatrix = crafter.generateTemporaryCraftingGridFor(recipe);
		if( craftMatrix == null ) {
			player.sendChatToPlayer("Can't craft: "+recipe+". Missing: "+recipe.ingredientsToString());
			return; // when will this happen?
		}

		// crafting event
		itemStack.onCrafting(player.worldObj, player, itemStack.stackSize); // i don't know what the last number is.
		GameRegistry.onItemCrafted(player, itemStack, craftMatrix);

		// consuming the items.
		for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
			ItemStack stackInSlot = craftMatrix.getStackInSlot(i);
			if( stackInSlot == null )
				continue;
			craftMatrix.decrStackSize(i, 1);

			if (stackInSlot.getItem().hasContainerItem()) {
				ItemStack containerItemStack = stackInSlot.getItem().getContainerItemStack(stackInSlot);

				if (containerItemStack.isItemStackDamageable()
						&& containerItemStack.getItemDamage() > containerItemStack.getMaxDamage()) {
					MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, containerItemStack));
					containerItemStack = null;
				}

				if (containerItemStack != null) {
					if( stackInSlot.getItem().doesContainerItemLeaveCraftingGrid(stackInSlot) )
						if( player.inventory.addItemStackToInventory(containerItemStack) )
							continue;

					if( !crafter.resources.addStack(containerItemStack) )
						player.dropPlayerItem(containerItemStack);
				}
			}

			stackInSlot = craftMatrix.getStackInSlot(i);
			if( stackInSlot != null ) {
				// add to the resources buffer.
				if(!crafter.resources.addStack(stackInSlot))
					// if that doesn't work, add the stack to the player's inv.
					if( !player.inventory.addItemStackToInventory(stackInSlot) )
						// last option: drop it
						player.dropPlayerItem(stackInSlot);
			}

		}

		crafter.resources.onInventoryChanged();
	}


	private CraftRecipe getRecipe() {
		try{
			return crafter.getRecipeAt(slotIndex);
		}catch(Exception e) {
			return null;
		}
	}

}
