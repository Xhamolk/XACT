package xk.xact.core.tileentities;


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

	public abstract GuiContainer getGuiContainerFor(EntityPlayer player);

}
