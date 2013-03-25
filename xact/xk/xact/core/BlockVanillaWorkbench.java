package xk.xact.core;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.util.Utils;


// the block that replaces the vanilla crafting table
public class BlockVanillaWorkbench extends BlockContainer {


	private BlockVanillaWorkbench(int blockID) {
		super( blockID, Material.wood );
		this.setHardness( 2.5F );
		this.setStepSound( soundWoodFootstep );
		this.setUnlocalizedName( "workbench" );
		this.setCreativeTab( CreativeTabs.tabDecorations );
	}

	public static BlockVanillaWorkbench createNew() {
		int id = Block.workbench.blockID;
		Block.blocksList[id] = null;
		return new BlockVanillaWorkbench( id );
	}


	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileWorkbench();
	}

	@Override
	public Icon getBlockTextureFromSideAndMetadata(int side, int metadata) {
		if( side == 0 )
			return TEXTURE_BOTTOM;
		else if( side == 1 )
			return TEXTURE_TOP;
		if( side == 2 || side == 4 )
			return TEXTURE_FRONT;
		else
			return TEXTURE_SIDE;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offX, float offY, float offZ) {
		if( !world.isRemote ) {
			player.openGui( XActMod.instance, 2, world, x, y, z );
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		TileWorkbench workbench = (TileWorkbench) world.getBlockTileEntity( x, y, z );
		if( workbench != null ) {
			ItemStack[] inventoryContents = workbench.craftingGrid.getContents();
			for( ItemStack current : inventoryContents ) {
				if( current == null )
					continue;
				Utils.dropItemAsEntity( world, x, y, z, current );
			}
		}
		super.breakBlock( world, x, y, z, par5, par6 );
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		this.TEXTURE_TOP = iconRegister.registerIcon( "workbench_top" );
		this.TEXTURE_FRONT = iconRegister.registerIcon( "workbench_front" );
		this.TEXTURE_SIDE = iconRegister.registerIcon( "workbench_side" );
		this.TEXTURE_BOTTOM = iconRegister.registerIcon( "wood_spruce" );
	}

	@SideOnly(Side.CLIENT)
	private Icon TEXTURE_TOP;

	@SideOnly(Side.CLIENT)
	private Icon TEXTURE_FRONT;

	@SideOnly(Side.CLIENT)
	private Icon TEXTURE_SIDE;

	@SideOnly(Side.CLIENT)
	private Icon TEXTURE_BOTTOM;

}
