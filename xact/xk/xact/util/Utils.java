package xk.xact.util;


import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import xk.xact.XActMod;

import java.util.Random;

public class Utils {

	public static void notifyPlayer(EntityPlayer player, String message) {
		if( player.worldObj.isRemote ) {
			player.sendChatToPlayer( message );
		}
	}

	public static void debug(String message) {
		if( XActMod.DEBUG_MODE )
			System.out.println( message );
	}

	/**
	 * The description of the ItemStack passed.
	 * Includes the stack size and the 'display' name.
	 * <p/>
	 * Example: 64x Redstone Dust
	 *
	 * @param stack the item stack.
	 * @return the description of the stack's contents. Or "null" if the stack is null.
	 */
	public static String stackDescription(ItemStack stack) {
		if( stack == null )
			return "null";

		return stack.stackSize + "x " + stack.getItem().getItemDisplayName( stack );
	}

	public static boolean equalsStacks(ItemStack stack1, ItemStack stack2) {
		return stack1.itemID == stack2.itemID
				&& (!stack1.getHasSubtypes() || stack1.getItemDamage() == stack2.getItemDamage())
				&& ItemStack.areItemStackTagsEqual( stack1, stack2 );
	}

	/**
	 * Drops an item on the world as an EntityItem.
	 *
	 * @param itemStack the ItemStack to drop. Shall not be null.
	 */
	public static void dropItemAsEntity(World world, int x, int y, int z, ItemStack itemStack) {
		Random random = new Random();
		float var10 = random.nextFloat() * 0.8F + 0.1F;
		float var11 = random.nextFloat() * 0.8F + 0.1F;
		EntityItem item;

		for( float var12 = random.nextFloat() * 0.8F + 0.1F; itemStack.stackSize > 0; world.spawnEntityInWorld( item ) ) {
			int var13 = random.nextInt( 21 ) + 10;

			if( var13 > itemStack.stackSize ) {
				var13 = itemStack.stackSize;
			}

			itemStack.stackSize -= var13;
			item = new EntityItem( world, (double) (x + var10), (double) (y + var11), (double) (z + var12), new ItemStack( itemStack.itemID, var13, itemStack.getItemDamage() ) );
			float var15 = 0.05F;
			item.motionX = (random.nextGaussian() * var15);
			item.motionY = (random.nextGaussian() * var15 + 0.2F);
			item.motionZ = (random.nextGaussian() * var15);

			if( itemStack.hasTagCompound() ) {
				item.getEntityItem().setTagCompound( (NBTTagCompound) itemStack.getTagCompound().copy() );
			}
		}
	}

	public static ItemStack[] copyArray(ItemStack... oldArray) {
		int length = oldArray.length;
		ItemStack[] newArray = new ItemStack[length];
		for( int i = 0; i < length; i++ ) {
			newArray[i] = oldArray[i] == null ? null : oldArray[i].copy();
		}
		return newArray;
	}

	public static void writeItemStackToNBT(NBTTagCompound compound, ItemStack item, String tagName) {
		NBTTagCompound itemTag = new NBTTagCompound();
		item.writeToNBT( itemTag );
		compound.setTag( tagName, itemTag );
	}

	public static ItemStack readStackFromNBT(NBTTagCompound nbt) {
		try {
			return ItemStack.loadItemStackFromNBT( nbt );
		} catch ( NullPointerException npe ) {
			return null;
		}

	}

}
