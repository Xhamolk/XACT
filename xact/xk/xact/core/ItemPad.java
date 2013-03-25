package xk.xact.core;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.config.Textures;
import xk.xact.gui.ContainerPad;

import java.util.List;

public class ItemPad extends ItemContainer {

	@SideOnly(Side.CLIENT)
	private Icon inUseIcon;

	public ItemPad(int itemID) {
		super( itemID );
		this.setUnlocalizedName( "craftPad" );
		this.setMaxStackSize( 1 );
		this.setCreativeTab( XActMod.xactTab );
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

		String loadedRecipe = itemStack.getTagCompound().getString( "loadedRecipe" );
		if( loadedRecipe != null && !loadedRecipe.equals( "" ) )
			list.add( "Recipe: " + loadedRecipe );
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		itemStack.setItemDamage( 1 );
		if( !world.isRemote )
			player.openGui( XActMod.instance, 3, world, 0, 0, 0 );
		return itemStack;
	}

	@Override
	public Icon getIconFromDamage(int itemDamage) {
		if( itemDamage == 1 )
			return inUseIcon;
		return iconIndex;
	}

	@Override
	@SideOnly(Side.CLIENT) // Item Texture
	public void updateIcons(IconRegister iconRegister) {
		this.iconIndex = iconRegister.registerIcon( Textures.ITEM_PAD_OFF );
		this.inUseIcon = iconRegister.registerIcon( Textures.ITEM_PAD_ON );
	}

}
