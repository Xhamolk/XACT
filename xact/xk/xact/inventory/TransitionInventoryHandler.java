package xk.xact.inventory;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.inventory.*;

import java.util.ArrayList;

public class TransitionInventoryHandler extends DefaultInventoryHandler {

	public static final TransitionInventoryHandler INSTANCE = new TransitionInventoryHandler();


	public ArrayList<ItemStack> listItemsInInventory(IInventory inventory, ForgeDirection side) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.listItemsInInventory( inventory, side );
	}

	public ItemStack takeItemFromInventory(IInventory inventory, ItemStack item, ForgeDirection side) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.takeItemFromInventory( inventory, item, side );
	}

	public ItemStack takeItemFromInventory(IInventory inventory, ItemStack item, int quantity, ForgeDirection side) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.takeItemFromInventory( inventory, item, quantity, side );
	}

	public ItemStack takeItemFromInventorySlot(IInventory inventory, int slotIndex, int quantity) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.takeItemFromInventorySlot( inventory, slotIndex, quantity );
	}

	public int getItemCountInInventory(IInventory inventory, ItemStack itemStack, ForgeDirection side) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.getItemCountInInventory( inventory, itemStack, side );
	}

	public int getItemCountInSlot(IInventory inventory, int slotIndex) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.getItemCountInSlot( inventory, slotIndex );
	}

	public int getItemCountInSlot(IInventory inventory, int slotIndex, ItemStack itemStack) {
		if( inventory instanceof TransitionInventory )
			inventory = ((TransitionInventory) inventory).getHiddenInventory();
		return super.getItemCountInSlot( inventory, slotIndex, itemStack );
	}

}
