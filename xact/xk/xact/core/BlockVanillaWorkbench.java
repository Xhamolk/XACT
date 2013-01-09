package xk.xact.core;


import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.util.Utils;


// the block that replaces the vanilla crafting table
public class BlockVanillaWorkbench extends BlockContainer {


	private BlockVanillaWorkbench(int blockID) {
		super(blockID, Material.wood);
		this.blockIndexInTexture = 59;
		this.setHardness(2.5F);
		this.setStepSound(soundWoodFootstep);
		this.setBlockName("workbench");
		this.setCreativeTab(CreativeTabs.tabDecorations);
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
	public int getBlockTextureFromSide(int side) {
		switch ( side ) {
			case 0:
				return Block.planks.getBlockTextureFromSide(0);
			case 1:
				return this.blockIndexInTexture - 16;
			default:
				if( side == 2 || side == 4 ) {
					return this.blockIndexInTexture +1;
				} else {
					return this.blockIndexInTexture;
				}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offX, float offY, float offZ) {
		if( !world.isRemote ) {
			player.openGui(XActMod.instance, 2, world, x, y , z);
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		TileWorkbench workbench = (TileWorkbench) world.getBlockTileEntity(x, y, z);
		if( workbench != null ) {
			ItemStack[] inventoryContents = workbench.craftingGrid.getContents();
			for( ItemStack current : inventoryContents ) {
				if( current == null )
					continue;
				Utils.dropItemAsEntity(world, x, y , z, current);
			}
		}
		super.breakBlock( world, x, y, z, par5, par6 );
	}

}
