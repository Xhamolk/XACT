package xk.xact.gui;


import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import xk.xact.util.FakeCraftingInventory;
import xk.xact.util.Inventory;

public class SlotVanillaCraft extends Slot {

	private EntityPlayer player;
	private IInventory craftMatrix;
	private int amountCrafted = 0;

	public SlotVanillaCraft(EntityPlayer player, IInventory gridInv, int posX, int posY) {
		super(gridInv, 9, posX, posY);
		this.player = player;
		this.craftMatrix = gridInv;
	}

	public boolean isItemValid(ItemStack itemStack) {
		return false;
	}

	@Override
	public void onSlotChanged() {
		FakeCraftingInventory grid = FakeCraftingInventory.emulateContents(((Inventory) craftMatrix).getContents());
		ItemStack result = CraftingManager.getInstance().findMatchingRecipe( grid, player.worldObj );
		this.inventory.setInventorySlotContents( getSlotIndex(), result );
	}

	protected void onCrafting(ItemStack itemStack) {
		itemStack.onCrafting(this.player.worldObj, this.player, amountCrafted);
		this.amountCrafted = 0;

		if (itemStack.itemID == Block.workbench.blockID) {
			this.player.addStat(AchievementList.buildWorkBench, 1);
		} else if (itemStack.itemID == Item.pickaxeWood.shiftedIndex) {
			this.player.addStat(AchievementList.buildPickaxe, 1);
		} else if (itemStack.itemID == Block.stoneOvenIdle.blockID) {
			this.player.addStat(AchievementList.buildFurnace, 1);
		} else if (itemStack.itemID == Item.hoeWood.shiftedIndex) {
			this.player.addStat(AchievementList.buildHoe, 1);
		} else if (itemStack.itemID == Item.bread.shiftedIndex) {
			this.player.addStat(AchievementList.makeBread, 1);
		} else if (itemStack.itemID == Item.cake.shiftedIndex) {
			this.player.addStat(AchievementList.bakeCake, 1);
		} else if (itemStack.itemID == Item.pickaxeStone.shiftedIndex) {
			this.player.addStat(AchievementList.buildBetterPickaxe, 1);
		} else if (itemStack.itemID == Item.swordWood.shiftedIndex) {
			this.player.addStat(AchievementList.buildSword, 1);
		} else if (itemStack.itemID == Block.enchantmentTable.blockID) {
			this.player.addStat(AchievementList.enchantments, 1);
		} else if (itemStack.itemID == Block.bookShelf.blockID) {
			this.player.addStat(AchievementList.bookcase, 1);
		}
	}

	protected void onCrafting(ItemStack itemStack, int amount) {
		this.amountCrafted += amount;
		this.onCrafting(itemStack);
	}

	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {

		GameRegistry.onItemCrafted(player, itemStack, craftMatrix);
		this.onCrafting(itemStack);

		for( int i = 0; i < 9; i++ ) {
			ItemStack stackInSlot = this.craftMatrix.getStackInSlot(i);
			if( stackInSlot == null )
				continue;

			this.craftMatrix.decrStackSize(i, 1);

			if (stackInSlot.getItem().hasContainerItem()) {
				ItemStack containerItem = stackInSlot.getItem().getContainerItemStack(stackInSlot);

				if (containerItem.isItemStackDamageable() && containerItem.getItemDamage() > containerItem.getMaxDamage()) {
					MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, containerItem));
					containerItem = null;
				}

				if ( containerItem != null && (!stackInSlot.getItem().doesContainerItemLeaveCraftingGrid(stackInSlot)
						|| !player.inventory.addItemStackToInventory(containerItem)) ) {
					if (this.craftMatrix.getStackInSlot(i) == null) {
						this.craftMatrix.setInventorySlotContents(i, containerItem);
					} else {
						player.dropPlayerItem(containerItem);
					}
				}
			}
		}
	}

}
