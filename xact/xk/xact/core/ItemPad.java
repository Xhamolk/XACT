package xk.xact.core;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.XActMod;

import java.util.List;

public class ItemPad extends Item {

    public ItemPad(int itemID) {
        super(itemID);
        this.setItemName("craftPad");
        this.setMaxStackSize(1);
        this.setTextureFile(XActMod.TEXTURE_ITEMS);
        this.setCreativeTab(XActMod.xactTab);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {

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
