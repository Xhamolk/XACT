package xk.xact.util;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class StackReference {

    private int itemID;
    private int itemDamage;
    private boolean hasSubtypes;
    private NBTTagCompound tag;
    public ItemStack stack;

    public int amount = 0;

    public StackReference(ItemStack itemStack) {
        this.itemID = itemStack.itemID;
        this.itemDamage = itemStack.getItemDamage();
        this.hasSubtypes = itemStack.getHasSubtypes();
        this.tag = itemStack.hasTagCompound() ? itemStack.getTagCompound() : null;

        this.stack = itemStack.copy();
    }


    public boolean equals(Object o) {
        if( o instanceof StackReference )
            return this.equals((StackReference) o);
        return o instanceof ItemStack && this.equals(new StackReference((ItemStack) o));
    }

    public boolean equals(StackReference reference) {
        // Compare Item ID.
        if( this.itemID != reference.itemID )
            return false;
        // If has subtypes, compare item damage.
        if( this.hasSubtypes )
            if( this.itemDamage != reference.itemDamage )
                return false;

        // Compare stacks tags.
        if( this.tag == null )
            return reference.tag == null;
        return this.tag.equals(reference.tag);
    }


    public static ItemStack[] toItemStacks(StackReference[] array){
        ItemStack[] retValue = new ItemStack[array.length];
        for(int i=0; i<array.length; i++) {
            retValue[i] = array[i].stack;
            retValue[i].stackSize = array[i].amount;
        }

        return retValue;
    }

}
