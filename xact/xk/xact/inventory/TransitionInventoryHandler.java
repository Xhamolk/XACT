package xk.xact.inventory;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.inventory.DefaultInventoryHandler;

import java.util.ArrayList;

public class TransitionInventoryHandler extends DefaultInventoryHandler {

	public static final TransitionInventoryHandler INSTANCE = new TransitionInventoryHandler();


	public ArrayList<ItemStack> listItemsInInventory(IInventory inventory, ForgeDirection side) {
		return super.listItemsInInventory( ((TransitionInventory) inventory).getMixedInventory(), side );
	}

	public ItemStack takeItemFromInventory(IInventory inventory, ItemStack item, ForgeDirection side) {
		return super.takeItemFromInventory( ((TransitionInventory) inventory).getMixedInventory(), item, side );
	}

	public ItemStack takeItemFromInventory(IInventory inventory, ItemStack item, int quantity, ForgeDirection side) {
		return super.takeItemFromInventory( ((TransitionInventory) inventory).getMixedInventory(), item, quantity, side );
	}

	public ItemStack takeItemFromInventorySlot(IInventory inventory, int slotIndex, int quantity) {
		return super.takeItemFromInventorySlot( ((TransitionInventory) inventory).getMixedInventory(), slotIndex, quantity );
	}

	public int getItemCountInInventory(IInventory inventory, ItemStack itemStack, ForgeDirection side) {
		return super.getItemCountInInventory( ((TransitionInventory) inventory).getMixedInventory(), itemStack, side );
	}

}
