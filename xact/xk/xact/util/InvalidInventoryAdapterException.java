package xk.xact.util;

/**
 * @author Xhamolk_
 */
public class InvalidInventoryAdapterException extends RuntimeException {

	public InvalidInventoryAdapterException(Class c) {
		super(c.getName());
	}

}
