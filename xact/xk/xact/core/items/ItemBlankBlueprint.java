package xk.xact.core.items;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.util.Textures;

public class ItemBlankBlueprint extends Item {


	public ItemBlankBlueprint(int itemID) {
		super( itemID );
		this.setUnlocalizedName( "blankBlueprint" );
		this.setMaxStackSize( 16 );
		this.setCreativeTab( XActMod.xactTab );
	}

	@Override
	@SideOnly(Side.CLIENT) // Item's Texture
	public void registerIcons(IconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon( Textures.ITEM_BLUEPRINT_BLANK );
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
