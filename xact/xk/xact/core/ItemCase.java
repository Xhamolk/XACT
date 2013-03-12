package xk.xact.core;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.config.Textures;
import xk.xact.gui.ContainerCase;

import java.util.List;


public class ItemCase extends ItemContainer {

	public ItemCase(int itemID) {
		super( itemID );
		this.setUnlocalizedName( "chipCase" );
		this.setMaxStackSize( 1 );
		this.setCreativeTab( XActMod.xactTab );
	}

	@Override
	public boolean containerMatchesItem(Container openContainer) {
		return openContainer instanceof ContainerCase;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		// Show how many chips are stored.
		if( itemStack == null || itemStack.stackTagCompound == null )
			return;

		Integer count = itemStack.getTagCompound().getInteger( "chipCount" );
		if( count != null && count > 0 )
			list.add( "Stored " + count + " chips." );
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		itemStack.setItemDamage( 1 );
		if( ! world.isRemote )
			player.openGui( XActMod.instance, 1, world, 0, 0, 0 );
		return itemStack;
	}

	@Override
	@SideOnly(Side.CLIENT) // Item Texture
	public void func_94581_a(IconRegister iconRegister) {
		this.iconIndex = iconRegister.func_94245_a( Textures.ITEM_CASE );
	}

}
