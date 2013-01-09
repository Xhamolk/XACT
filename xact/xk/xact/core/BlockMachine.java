package xk.xact.core;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.util.Utils;

import java.util.ArrayList;

/**
 * 
 * @author Xhamolk_
 */
public class BlockMachine extends BlockContainer {

	public BlockMachine(int itemID) {
		super(itemID, Material.iron);
		this.setStepSound(soundMetalFootstep);
		this.setHardness(2.0f);
		this.setResistance(1.5f);
		this.setCreativeTab(XActMod.xactTab);
	}


	// update block when it's placed on the world.
	@Override
	public int func_85104_a(World world, int x, int y, int z, int side, float xOff, float yOff, float zOff, int metadata) { //updateBlockMetadata
		return side;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving living) {
		EntityPlayer player = (EntityPlayer) living;
		int frontSide = world.getBlockMetadata(x, y, z);
		if( frontSide == 0 || frontSide == 1 ){
			frontSide = sideByAngles(player, x, z);
		}
		world.setBlockMetadata(x, y, z, frontSide);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOff, float yOff, float zOff){

		if( player.isSneaking() ) {
			return false;
		}
		if( !world.isRemote )
			player.openGui(XActMod.instance, 0, world, x, y, z);

		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		TileMachine entity = (TileMachine) world.getBlockTileEntity(x, y, z);
		
		if( entity != null )
			for( ItemStack stack : entity.getDropItems() ){
				if( stack != null )
					Utils.dropItemAsEntity( world, x, y, z, stack );
			}

		super.breakBlock(world, x, y, z, par5, par6);
		world.removeBlockTileEntity(x, y, z);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(this, 1, 0));
		return list;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileCrafter();
	}


	private int sideByAngles(EntityPlayer player, int x, int z) {
		double Dx = player.posX - x;
		double Dz = player.posZ - z;
		double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315)
			return 4;
		else if (angle < 135)
			return 2;
		else if (angle < 225)
			return 5;
		else
			return 3;
	}
	
	///////////////
	///// Textures
	@Override
	public String getTextureFile(){
		return XActMod.TEXTURE_BLOCKS;
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int metadata){
		switch (side){
			case 0: // bottom
				return 16+3;
			case 1: // top
				return 16;
			default:
				if( side == metadata ) // front
					return 16+1;
				else // any other side.
					return 16+2;
		}
	}

}
