package xk.xact.inventory.adapter;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import xk.xact.api.IInventoryAdapter;
import xk.xact.inventory.JointInventory;

import java.util.Iterator;

/**
 * Special case double chests, so both inventories can be available.
 *
 * @author Xhamolk_
 */
public class DoubleChestInventory extends LinearInventory implements IInventoryAdapter {

	public DoubleChestInventory(TileEntityChest chest) {
		super( new JointInventory( "", chest, getAdjacentChest( chest ) ) );
	}

	public static boolean isDoubleChest(TileEntityChest chest) {
		return chest != null && getAdjacentChest( chest ) != null;
	}

	private static TileEntityChest getAdjacentChest(TileEntityChest chest) {
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

}
