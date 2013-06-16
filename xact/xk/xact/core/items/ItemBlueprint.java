package xk.xact.core.items;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.util.Textures;

import java.util.List;

public class ItemBlueprint extends Item {

	public ItemBlueprint(int itemID) {
		super( itemID );
		this.setUnlocalizedName( "blueprint" );
		this.setMaxStackSize( 1 );
		this.setCreativeTab( XActMod.xactTab );
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		list.add( "it's something!" );
	}

	@Override
	@SideOnly(Side.CLIENT) // Item's Texture
	public void registerIcons(IconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon( Textures.ITEM_BLUEPRINT );
	}

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int slotNum, boolean isHeld) {
		// is there something we need to do every tick?
	}

	@Override
	public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
		// what to do when created? well.. i could init the NBT.
		itemStack.setTagCompound( new NBTTagCompound() );
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		int blockID = world.getBlockId( x, y, z );
		if( blockID == Block.workbench.blockID ) {
			// unimplemented functionality.
		}
		return false;
	}

}
