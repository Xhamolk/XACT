package xk.xact.core;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.XActMod;

import java.util.List;


public class ItemCase extends Item {

	public ItemCase(int itemID) {
		super(itemID);
		this.setItemName("chipCase");
		this.setMaxStackSize(1);
		this.setTextureFile(XActMod.TEXTURE_ITEMS);
		this.setCreativeTab(XActMod.xactTab);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
	    // Show how many chips are stored.
		if( itemStack == null || itemStack.stackTagCompound == null )
			return;

		Integer count = itemStack.getTagCompound().getInteger("chipCount");
		if (count != null && count > 0)
			list.add("Stored " + count +" chips.");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        itemStack.setItemDamage(1);
		if( !world.isRemote )
        	player.openGui(XActMod.instance, 1, world, 0, 0, 0);
		return itemStack;
	}

    @Override
    public int getIconFromDamage(int itemDamage) {
        return 16;
    }

}
