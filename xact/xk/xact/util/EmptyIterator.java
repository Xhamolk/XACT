package xk.xact.util;

import java.util.Iterator;

/**
 * My own empty iterator implementation.
 * Just because Collections.emptyIterator() is not available in java 1.6
 *
 * @author Xhamolk_
 */
public class EmptyIterator<T> implements Iterator<T> {

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		return null;
	}

	@Override
	public void remove() { }

}
