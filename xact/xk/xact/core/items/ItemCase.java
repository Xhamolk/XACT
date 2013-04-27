package xk.xact.core.items;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.util.Textures;
import xk.xact.gui.ContainerCase;

import java.util.List;


public class ItemCase extends Item {

	public ItemCase(int itemID) {
		super( itemID );
		this.setUnlocalizedName( "chipCase" );
		this.setMaxStackSize( 1 );
		this.setCreativeTab( XActMod.xactTab );
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
	public void registerIcons(IconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon( Textures.ITEM_CASE );
	}

}
