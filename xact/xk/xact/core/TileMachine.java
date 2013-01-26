package xk.xact.core;


import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;

/**
 *
 *
 */
public abstract class TileMachine extends TileEntity {

	public abstract ArrayList<ItemStack> getDropItems();

}
