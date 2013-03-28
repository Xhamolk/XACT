package xk.xact.util;


import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;

public class ItemsList implements Iterable<ItemsReference> {


	private ArrayList<ItemsReference> list = new ArrayList<ItemsReference>();

	public void addStack(ItemStack stack) {
		if( stack != null )
			addStack( stack, stack.stackSize );
	}

	public void addStack(ItemStack stack, int amount) {
		if( stack != null ) {
			ItemsReference reference = getOrCreateReference( stack );
			reference.amount += amount;
		}
	}

	public ItemsReference getOrCreateReference(ItemStack stack) {
		ItemsReference reference = ItemsReference.wrap( stack );
		int index = list.indexOf( reference );
		if( index == -1 ) {
			list.add( reference );
		} else {
			reference = list.get( index );
		}
		return reference;
	}

	public ItemStack[] toArray() {
		int size = list.size();
		ItemStack[] retValue = new ItemStack[size];
		for( int i = 0; i < size; i++ ) {
			retValue[i] = list.get(i).toItemStack();
		}
		return retValue;
	}

	@Override
	public Iterator<ItemsReference> iterator() {
		return new Iterator<ItemsReference>() {
			private int iteratorIndex = 0;

			@Override
			public boolean hasNext() {
				return iteratorIndex < list.size();
			}

			@Override
			public ItemsReference next() {
				return list.get( iteratorIndex++ );
			}

			@Override
			public void remove() {
			}
		};
	}

}
