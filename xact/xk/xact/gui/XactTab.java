package xk.xact.gui;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemStack;
import xk.xact.XActMod;


public class XactTab extends CreativeTabs {

    public XactTab() {
        super("xact");
    }

	@Override
    public ItemStack getIconItemStack() {
        return new ItemStack(XActMod.blockMachine, 1, 1);
    }
}
