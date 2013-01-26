package xk.xact.gui;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import xk.xact.XActMod;


public class CreativeTabXACT extends CreativeTabs {

	public CreativeTabXACT() {
		super( "xact" );
	}

	@Override
	public ItemStack getIconItemStack() {
		return new ItemStack( XActMod.blockMachine, 1, 1 );
	}

}
