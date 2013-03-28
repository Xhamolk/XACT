package xk.xact.util;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemsReference {

	public final int itemID;
	public final int itemDamage;
	private NBTTagCompound tag;

	public int amount = 0;

	private ItemsReference(ItemStack itemStack) {
		this.itemID = itemStack.itemID;
		this.itemDamage = itemStack.getItemDamage();
		this.tag = itemStack.hasTagCompound() ? itemStack.getTagCompound() : null;
	}

	@Override
	public int hashCode() {
//		return itemDamage ^ (tag == null ? 1 : tag.hashCode()) ^ itemID);
		int hash = 17;
		hash = 31 * hash + itemID;
		hash = 31 * hash + itemDamage;
		hash = 31 * hash + (tag == null ? 0 : tag.hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if( o == null )
			return false;
		if( o == this )
			return true;
		if( o instanceof ItemsReference )
			return this.isEqualTo( (ItemsReference) o );
		return o instanceof ItemStack && this.isEqualTo( new ItemsReference( (ItemStack) o ) );
	}

	public boolean isEqualTo(ItemsReference reference) {
		// Compare Item ID.
		if( this.itemID != reference.itemID )
			return false;
		// Compare Item's damage value.
		if( this.itemDamage != reference.itemDamage )
			return false;

		// Compare stacks tags.
		if( this.tag == null )
			return reference.tag == null;
		return this.tag.equals( reference.tag );
	}

	public ItemStack toItemStack() {
		ItemStack itemStack = new ItemStack( itemID, amount, itemDamage );
		if( tag != null )
			itemStack.setTagCompound( tag );
		return itemStack;
	}

	public static ItemsReference wrap(ItemStack itemStack) {
		return new ItemsReference( itemStack );
	}
}
