package xk.xact.gui;


import net.minecraft.src.*;


public abstract class ContainerMachine extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return true;
	}

	@Override
	public abstract ItemStack transferStackInSlot(EntityPlayer player, int slot);


}
