package xk.xact;


import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import xk.xact.event.XactEvent;

import java.util.ArrayList;

/**
 * 
 * 
 */
public abstract class TileMachine extends TileEntity {
	
	public abstract ArrayList<ItemStack> getDropItems();

	public abstract void handleEvent(XactEvent event);

}
