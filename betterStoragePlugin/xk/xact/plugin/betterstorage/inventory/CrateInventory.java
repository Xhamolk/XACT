package xk.xact.plugin.betterstorage.inventory;

import net.mcft.copy.betterstorage.api.ICrateStorage;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import xk.xact.api.IInventoryAdapter;
import xk.xact.api.IInventoryAdapterProvider;
import xk.xact.util.EmptyIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Used to handler ICrateStorage inventories.
 *
 * @author Xhamolk_
 */
public class CrateInventory implements IInventoryAdapter {

	private final ICrateStorage crate;

	public CrateInventory(ICrateStorage crate) {
		this.crate = crate;
	}

	@Override
	public ItemStack placeItem(ItemStack item) {
		return crate.insertItems( unusedDirection, item );
	}

	@Override
	public ItemStack takeItem(ItemStack item, int quantity) {
		item = item.copy();
		item.stackSize = Math.min( item.stackSize, quantity );
		return crate.extractItems( unusedDirection, item );
	}

	@Override
	public Iterator<ItemStack> iterator() {
		try {
			List<ItemStack> contents = crate.getContents( unusedDirection );
			if( contents != null )
				return contents.iterator();
		} catch( NullPointerException npe ) {
			// Ignore it...
		}
		return new EmptyIterator<ItemStack>();
	}

	private final static ForgeDirection unusedDirection = ForgeDirection.UP; // ignored.

	public static class Provider implements IInventoryAdapterProvider {

		@Override
		public IInventoryAdapter createInventoryAdapter(Object inventory) {
			if( inventory instanceof ICrateStorage ) {
				return new CrateInventory( (ICrateStorage) inventory );
			}
			return null;
		}

	}

}
