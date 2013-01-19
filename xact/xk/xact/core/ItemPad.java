package xk.xact.core;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.gui.ContainerPad;

import java.util.List;

public class ItemPad extends ItemContainer {

    public ItemPad(int itemID) {
        super(itemID);
        this.setItemName("craftPad");
        this.setMaxStackSize(1);
        this.setTextureFile(XActMod.TEXTURE_ITEMS);
        this.setCreativeTab(XActMod.xactTab);
    }

	@Override
	public boolean containerMatchesItem(Container openContainer) {
		return openContainer instanceof ContainerPad;
	}

	@SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		// Tell which is recipe is loaded on the grid.
		if( itemStack == null || itemStack.stackTagCompound == null )
			return;

		String loadedRecipe = itemStack.getTagCompound().getString("loadedRecipe");
		if( loadedRecipe != null && !loadedRecipe.equals("") )
			list.add("Recipe: "+loadedRecipe);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        itemStack.setItemDamage(1);
		if( !world.isRemote )
			player.openGui(XActMod.instance, 3, world, 0, 0, 0);
		return itemStack;
    }

	@Override
	public int getIconFromDamage(int itemDamage) {
		if( itemDamage == 1 )
			return 19;
		return 18;
	}



}
