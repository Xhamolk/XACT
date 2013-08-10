package xk.xact.plugin.appliedenergistics.inventory;

import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.me.tiles.ITileInterfaceApi;
import appeng.api.me.util.IMEInventory;
import net.minecraft.item.ItemStack;
import xk.xact.api.IInventoryAdapter;
import xk.xact.api.IInventoryAdapterProvider;

import java.util.Iterator;

/**
 * Used to handle IMEInventory from Applied Energistics.
 *
 * @author Xhamolk_
 */
public class AEInventory implements IInventoryAdapter {

	private IMEInventory inventory;

	public AEInventory(IMEInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public ItemStack placeItem(ItemStack item) {
		if( item != null ) {
			return convert( inventory.addItems( convert( item ) ) );
		}
		return null;
	}

	@Override
	public ItemStack takeItem(ItemStack item, int quantity) {
		ItemStack temp = item.copy();
		temp.stackSize = quantity;

		return convert( inventory.extractItems( convert( temp ) ) );
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return new AEIterator();
	}

	private ItemStack convert(IAEItemStack item) {
		if( item != null ) {
			return new ItemStack( item.getItemID(), (int) item.getStackSize(), item.getItemDamage() );
		}
		return null;
	}

	private IAEItemStack convert(ItemStack item) {
		if( item != null ) {
			return Util.createItemStack( item );
		}
		return null;
	}


	private class AEIterator implements Iterator<ItemStack> {

		private Iterator<IAEItemStack> iterator = inventory.getAvailableItems().iterator();

		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public ItemStack next() {
			return convert( iterator.next() );
		}

		@Override
		public void remove() {
		}

	}

	public static class Provider implements IInventoryAdapterProvider {

		@Override
		public IInventoryAdapter createInventoryAdapter(Object inventory) {
			return new AEInventory( ((ITileInterfaceApi) inventory).getApiArray() );
		}
	}

}
