package xk.xact.util;


import net.minecraft.item.ItemStack;

import java.util.HashMap;

import static xk.xact.util.ItemsReference.wrap;

public class ItemsMap<V> extends HashMap<ItemsReference, V> {

	public V put(ItemStack itemStack, V v) {
		return this.put( wrap( itemStack ), v );
	}

	public boolean containsKey(ItemStack itemStack) {
		return this.containsKey( wrap( itemStack ) );
	}

	public V get(ItemStack itemStack) {
		return this.get( wrap( itemStack ) );
	}

	public V remove(ItemStack itemStack) {
		return this.remove( wrap( itemStack ) );
	}
}
