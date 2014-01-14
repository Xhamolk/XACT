package xk.xact.api;

/**
 * Implemented by the TileEntities of overridden blocks.
 *
 * @author Xhamolk_
 */
public interface OverriddenBlock {

	public <T> T getField(Class<T> fieldType, String name);

	public <T> void setField(Class<T> fieldType, String name, T value);

}