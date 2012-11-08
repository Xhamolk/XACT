package xk.xact;


import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

import java.util.ArrayList;

/**
 * 
 * 
 */
public abstract class TileMachine extends TileEntity {
	
	public abstract ArrayList<ItemStack> getDropItems();

}
