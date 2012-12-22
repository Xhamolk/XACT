package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;


public abstract class ContainerMachine extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return true;
	}

	@Override
	public abstract ItemStack transferStackInSlot(EntityPlayer player, int slot);


}
