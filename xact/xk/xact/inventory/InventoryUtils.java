package xk.xact.inventory;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.ForgeDirection;
import xk.xact.api.IInventoryAdapter;
import xk.xact.inventory.adapter.LinearInventory;
import xk.xact.plugin.PluginManager;
import xk.xact.util.InvalidInventoryAdapterException;
import xk.xact.util.Utils;

public class InventoryUtils {

	/**
	 * Whether if two stacks contain the same kind of items.
	 *
	 * @param stack1     one of the stacks.
	 * @param stack2     one of the stacks.
	 * @param compareNBT if the item's NBT must also match.
	 * @return true if the stacks contain the same kind of items.
	 */
	public static boolean similarStacks(ItemStack stack1, ItemStack stack2, boolean compareNBT) {
		if( stack1 == null || stack2 == null )
			return false; // this scenario is not meant to happen. 

		if( stack1.itemID != stack2.itemID )
			return false;

		if( stack1.getHasSubtypes() ) {
			if( stack1.getItemDamage() != stack2.getItemDamage() )
				return false;
		}
		if( !compareNBT )
			return true;

		// Compare stacks tags.
		return !stack1.hasTagCompound() || stack2.hasTagCompound() && stack1.getTagCompound().equals( stack2.getTagCompound() );

	}

	/**
	 * Determines the available space in target to fit the contents of itemStack.
	 * In other words, the amount of itemStack that could be merged into target.
	 * <p/>
	 * The accuracy of this method relays on if both stacks hold the same kind of items.
	 * Note: the return value may be higher than itemStack.stackSize
	 *
	 * @param target    the stack where the contents of itemStack should fit.
	 * @param itemStack the stack that's meant to be merged.
	 * @return the amount of itemStack that would fit on target. Or -1, if the stacks is aren't of the same kind.
	 * @see InventoryUtils#similarStacks
	 */
	public static int getSpaceInStackFor(ItemStack target, ItemStack itemStack) {
		if( !similarStacks( target, itemStack, true ) )
			return -1;

		if( !target.isStackable() )
			return 0;

		int max = Math.min( target.getMaxStackSize(), itemStack.getMaxStackSize() );
		return max - target.stackSize;
	}

	/**
	 * Tries to add a stack on a inventory,
	 * and will return the remaining part that couldn't fit (if any).
	 * <p/>
	 * Note: the stack passed is manipulated (it's stack size is reduced).
	 *
	 * @param stack       the ItemStack that's meant to be added to the inventory.
	 * @param inv         the IInventory where the item should be added.
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
		boolean isSided = inv instanceof ISidedInventory;
		if( isSided && !(inv instanceof SidedInventory) ) { // My implementation of ISided is the only one allowed past here.
			Utils.log( "Attempting to add stack to unregulated ISidedInventory... skipping! Class: %s", inv.getClass() );
			return stack;
		}
		int remaining = stack.stackSize;
		for( InvSlot slot : InvSlotIterator.createNewFor( inv ) ) {
			if( slot == null )
				continue;

			// the side parameter is ignored - make sure only my SidedInventory reaches here.
			if( isSided && !((ISidedInventory )inv).canInsertItem( slot.slotIndex, stack, -1 ))
				continue;

			if( slot.isEmpty() ) {
				// there are a few scenarios where you'd want to keep empty slots empty.
				if( ignoreEmpty ) continue;

				// the slot is empty, so feel free to add it.
				inv.setInventorySlotContents( slot.slotIndex, stack.copy() );
				inv.onInventoryChanged();
				stack.stackSize = 0;
				return null; // success
			}

			// how much of stack can fit on the slot?
			int space = slot.getSpaceFor( stack );
			if( space <= 0 )
				continue; // can't fit here.

			if( space >= stack.stackSize ) { // fits entirely
				slot.stack.stackSize += remaining;
				inv.onInventoryChanged();
				stack.stackSize = 0;
				return null; // success

			} else { // fits partially
				slot.stack.stackSize = slot.stack.getMaxStackSize();
				remaining -= space;
				// inv.onInventoryChanged();
			}
		}
		if( remaining == 0 )
			return null; // success

		stack.stackSize = remaining;
		return stack;
	}

	public static ItemStack[] getContents(IInventory inventory) {
		int size = inventory.getSizeInventory();
		ItemStack[] contents = new ItemStack[size];

		for( int i = 0; i < size; i++ )
			contents[i] = inventory.getStackInSlot( i );

		return contents;
	}

	/**
	 * Gets the IInventory instance for the specified TileEntity.
	 * The <code>tileEntity</code> might or not be an IInventory instance,
	 * but some tile entities are special-cased, like modded ones.
	 * <p/>
	 * Note: the returned IInventory may just be an adapter of the original inventory.
	 *
	 * @param tileEntity the TileEntity from which the IInventory will accessed.
	 * @param side the side from which the TileEntity is being accessed.
	 * @return an IInventory instance that represents the accessible inventory on the TileEntity.
	 */
	public static IInventory getInventoryFrom(TileEntity tileEntity, ForgeDirection side) {
		IInventory inventory = null;
		if( tileEntity instanceof TileEntityChest ) {
			TileEntityChest chest = (TileEntityChest) tileEntity;
			TileEntityChest chest2 = getChestAdjacentTo( chest );
			if( chest2 != null ) {
				inventory = new InventoryLargeChest( "", chest, chest2 );
			} else {
				inventory = chest;
			}
		} else if( tileEntity instanceof ISidedInventory ) {
			inventory = new SidedInventory( (ISidedInventory) tileEntity, side );
		} else if( tileEntity instanceof IInventory ) {
			inventory = (IInventory) tileEntity;
		}
		return inventory;
	}

	private static TileEntityChest getChestAdjacentTo(TileEntityChest chest) {
		if( chest.adjacentChestZNeg != null ) {
			return chest.adjacentChestZNeg;
		}
		if( chest.adjacentChestZPosition != null ) {
			return chest.adjacentChestZPosition;
		}
		if( chest.adjacentChestXPos != null ) {
			return chest.adjacentChestXPos;
		}
		if( chest.adjacentChestXNeg != null ) {
			return chest.adjacentChestXNeg;
		}
		return null;
	}

	/**
	 * Simulates an InventoryCrafting instance based on an IInventory.
	 *
	 * @param container the Container to be updated when the contents change.
	 * @param inventory the IInventory that backs this InventoryCrafting.
	 */
	public static InventoryCrafting simulateCraftingInventory(Container container, IInventory inventory) {
		return new SimulatedInventoryCrafting( container, inventory );
	}

	/**
	 * Simulates an InventoryCrafting instance based on an array of ItemStack.
	 *
	 * @param items the array of items that will be initially on the inventory.
	 *              The length should be no higher than 9.
	 */
	public static InventoryCrafting simulateCraftingInventory(ItemStack[] items) {
		Inventory inventory = new Inventory( 9, "" );
		inventory.setContents( items );
		return simulateCraftingInventory( null, inventory );
	}

	/**
	 * Whether if the player has the specified item in it's hotbar.
	 *
	 * @param player    the player
	 * @param itemStack the item that must be on the player's hotbar.
	 * @return If found, returns the slot's index. Returns -1 otherwise.
	 */
	public static int checkHotbar(EntityPlayer player, ItemStack itemStack) {
		for( int i = 0; i < 9; i++ ) {
			ItemStack current = player.inventory.mainInventory[i];
			if( current != null && InventoryUtils.similarStacks( itemStack, current, false ) ) {
				return i;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public static IInventoryAdapter getInventoryAdapter(Object inventory) {
		if( inventory != null ) {
			for( Class adapterClass : PluginManager.getInventoryAdapters().keySet() ) {
				if( adapterClass != null && adapterClass.isAssignableFrom( inventory.getClass() ) )
					return PluginManager.getInventoryAdapters().get( adapterClass ).createInventoryAdapter( inventory );
			}
			if( inventory instanceof IInventory ) {
				return new LinearInventory( (IInventory) inventory );
			}

			Utils.logException( "Invalid inventory adapter", new InvalidInventoryAdapterException( inventory.getClass() ), false );
		}
		return null;
	}

	@SuppressWarnings( "unchecked" )
	public static boolean isValidInventory(Object inventory) {
		if( inventory != null ) {
			if( inventory instanceof TileEntityChest ) {
				return true;
			}
			if( inventory instanceof IInventory ) { // ISidedInventory is included here.
				return true;
			}

			for( Class invClass : PluginManager.getInventoryAdapters().keySet() ) {
				if( invClass != null && invClass.isAssignableFrom( inventory.getClass() ))
					return true;
			}
		}
		return false;
	}

}
