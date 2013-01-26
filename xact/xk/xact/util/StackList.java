package xk.xact.util;


import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;

public class StackList implements Iterable<StackReference> {


	private ArrayList<StackReference> list = new ArrayList<StackReference>();

	public void addStack(ItemStack stack) {
		if( stack != null )
			addStack( new StackReference( stack ), stack.stackSize );
	}

	public void addStack(ItemStack stack, int amount) {
		if( stack != null )
			addStack( new StackReference( stack ), amount );
	}

	public void addStack(StackReference reference, int increment) {
		for( StackReference current : list ) {
			if( current != null && current.equals( reference ) ) {
				current.amount += increment;
				return;
			}
		}
		reference.amount = increment;
		list.add( reference );
	}

	public ItemStack[] toArray() {
		return StackReference.toItemStacks( list.toArray( new StackReference[list.size()] ) );
	}

	@Override
	public Iterator<StackReference> iterator() {
		return new Iterator<StackReference>() {
			private int iteratorIndex = 0;

			@Override
			public boolean hasNext() {
				return iteratorIndex < list.size();
			}

			@Override
			public StackReference next() {
				return list.get( iteratorIndex++ );
			}

			@Override
			public void remove() {
			}
		};
	}

}
