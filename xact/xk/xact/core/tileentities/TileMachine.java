package xk.xact.core.tileentities;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;

/**
 *
 *
 */
public abstract class TileMachine extends TileEntity {

	public abstract ArrayList<ItemStack> getDropItems();

	public abstract Container getContainerFor(EntityPlayer player);

	@SideOnly(Side.CLIENT)
	public abstract GuiContainer getGuiContainerFor(EntityPlayer player);

}
