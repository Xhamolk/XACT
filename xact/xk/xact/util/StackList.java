package xk.xact.util;



import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class StackList {


	private ArrayList<StackReference> list = new ArrayList<StackReference>();

	public void addStack(ItemStack stack) {
		if( stack != null )
			addStack(new StackReference(stack));
	}

	public void addStack(StackReference reference) {
		for (StackReference current : list) {
			if (current != null && current.equals(reference)) {
				current.amount++;
				return;
			}
		}
		reference.amount = 1;
		list.add(reference);
	}

	public ItemStack[] toArray() {
		return StackReference.toItemStacks(list.toArray(new StackReference[list.size()]));
	}


}
