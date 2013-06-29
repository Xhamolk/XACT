package xk.xact.api;

/**
 * Used for creating custom IInventoryAdapters.
 *
 * @author Xhamolk_
 */
public interface IInventoryAdapterProvider {

	public IInventoryAdapter createInventoryAdapter(Object inventory);

}
