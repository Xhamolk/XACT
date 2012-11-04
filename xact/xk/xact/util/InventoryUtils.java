package xk.xact.util;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

public class InventoryUtils {

	/**
	 * Generates an Iterator for the specified IInventory.
	 * This is helpful to iterate through every slot on the inventory.
	 * 
	 * @param inventory the inventory that holds the items.
	 * @return an iterable representation of the IInventory. 
	 */
	public static Iterable<InvSlot> inventoryIterator(IInventory inventory) {
		if( inventory != null )
			return new InvSlotIterator(inventory);
		return null; 
	}

	/**
	 * Whether if two stacks contain the same kind of items.
	 *
	 * @param stack1 one of the stacks.
	 * @param stack2 one of the stacks.
	 * @return true if the stacks contain the same kind of items.
	 */
	public static boolean similarStacks(ItemStack stack1, ItemStack stack2) {
		if( stack1 == null || stack2 == null )
			return false; // this scenario is not meant to happen. 
		
		if( stack1.itemID != stack2.itemID )
			return false;
		
		if( stack1.getHasSubtypes() ) {
			if( stack1.getItemDamage() != stack2.getItemDamage() )
                return false;
		}
        // Compare stacks tags.
        if( stack1.hasTagCompound() )
            return stack2.hasTagCompound() && stack1.getTagCompound().equals(stack2.getTagCompound());

        return true;
	}

	/**
	 * Determines the available space in target to fit the contents of itemStack.
	 * In other words, the amount of itemStack that could be merged into target.
	 *
	 * The accuracy of this method relays on if both stacks hold the same kind of items.
	 * Note: the return value may be higher than itemStack.stackSize
	 *
	 * @param target the stack where the contents of itemStack should fit.
	 * @param itemStack the stack that's meant to be merged.
	 * @return the amount of itemStack that would fit on target. Or -1, if the stacks is aren't of the same kind. 
	 * @see InventoryUtils#similarStacks
	 */
	public static int getSpaceInStackFor(ItemStack target, ItemStack itemStack){
		if( !similarStacks(target, itemStack))
			return -1;

		if( !target.isStackable() )
			return 0;
		
		int max = Math.min( target.getMaxStackSize(), itemStack.getMaxStackSize() );
		return max - target.stackSize;
	}

	/**
	 * Tries to add a stack on a inventory,
	 * and will return the remaining part that couldn't fit (if any).
	 *
	 * Note: the stack passed is manipulated (it's stack size is reduced).
	 *
	 * @param stack the ItemStack that's meant to be added to the inventory.
	 * @param inv the IInventory where the item should be added.
	 * @param ignoreEmpty if empty slots should be ignored.
	 * @return the remaining part of the passed stack. or null, if it was successfully added entirely.
	 */
	public static ItemStack addStackToInventory(ItemStack stack, IInventory inv, boolean ignoreEmpty) {
		if( stack == null )
			return null; // kinda like success... isn't it?

		/*
			Possible scenarios:
			1) the slot is empty (it might be ignored, if: ignoreEmpty == true)

			2) the slot has no space fot this items (probably because is not of the same kind).

			3) the slot has some available space for this item.
				a) there is enough space for it to fit.
				b) there is not enough space for it to fit entirely.
					So, split and find the next available slot.
			 */

		int remaining = stack.stackSize;
		for( InvSlot slot : inventoryIterator(inv)) {
            if( slot == null )
                continue;

			if( slot.isEmpty() ) {
				// there are a few scenarios where you'd want to keep empty slots empty.
				if( ignoreEmpty ) continue;

				// the slot is empty, so feel free to add it.
				inv.setInventorySlotContents(slot.slotIndex, stack);
				inv.onInventoryChanged();
				stack.stackSize = 0;
				return null; // success
			}

			// how much of stack can fit on the slot?
			int space = slot.getSpaceFor(stack);
			if( space <= 0 )
				continue; // can't fit here.

			if( space >= stack.stackSize ) { // fits entirely
				slot.stack.stackSize += remaining;
				inv.onInventoryChanged();
				stack.stackSize = 0;
				return null; // success

			} else  { // fits partially
				slot.stack.stackSize = slot.stack.getMaxStackSize();
				remaining -= space;
				inv.onInventoryChanged();
			}
		}
		if( remaining == 0 )
			return null; // success

		stack.stackSize = remaining;
		return stack;
	}

    /**
     * The description of the ItemStack passed.
     * Includes the stack size and the 'display' name.
     *
     * Example: 64x Redstone
     *
     * @param stack the item stack.
     * @return the description of the stack's contents. Or "null" if the stack is null.
     */
    public static String stackDescription(ItemStack stack) {
        if( stack == null )
            return "null";

        return stack.stackSize +"x "+ stack.getItem().getItemDisplayName(stack);
    }

}
