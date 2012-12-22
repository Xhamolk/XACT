package xk.xact.core;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import xk.xact.XActMod;

import java.util.ArrayList;
import java.util.Random;

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
					drop(world, x, y, z, stack);
			}

		super.breakBlock(world, x, y, z, par5, par6);
		world.removeBlockTileEntity(x, y, z);
	}

	private void drop(World world, int x, int y, int z, ItemStack stack) {
		Random random = new Random();
		float var10 = random.nextFloat() * 0.8F + 0.1F;
		float var11 = random.nextFloat() * 0.8F + 0.1F;
		EntityItem item;

		for( float var12 = random.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(item) ) {
			int var13 = random.nextInt(21) + 10;

			if (var13 > stack.stackSize) {
				var13 = stack.stackSize;
			}

			stack.stackSize -= var13;
			item = new EntityItem(world, (double)(x + var10), (double)(y + var11), (double)(z + var12), new ItemStack(stack.itemID, var13, stack.getItemDamage()));
			float var15 = 0.05F;
			item.motionX = (random.nextGaussian() * var15);
			item.motionY = (random.nextGaussian() * var15 + 0.2F);
			item.motionZ = (random.nextGaussian() * var15);

			if (stack.hasTagCompound()) {
				item.func_92014_d().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
			}
		}
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
