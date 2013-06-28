package xk.xact.api;

import net.minecraft.item.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * Used for accessing the contents of inventories from CraftingHandler
 *
 * @author Xhamolk_
 */
public interface IInventoryAdapter extends Iterable<ItemStack> {

	public ItemStack placeItem(ItemStack item);

	public ItemStack takeItem(ItemStack item, int quantity);

	@Override
	public Iterator<ItemStack> iterator();

}
