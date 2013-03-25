package xk.xact.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.config.Textures;
import xk.xact.util.Utils;

import java.util.ArrayList;

/**
 * @author Xhamolk_
 */
public class BlockMachine extends BlockContainer {

	public BlockMachine(int itemID) {
		super( itemID, Material.iron );
		this.setStepSound( soundMetalFootstep );
		this.setHardness( 2.0f );
		this.setResistance( 1.5f );
		this.setCreativeTab( XActMod.xactTab );
	}


	// update block when it's placed on the world.
	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float xOff, float yOff, float zOff, int metadata) { //updateBlockMetadata
		return side;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving living, ItemStack itemStack) {
		EntityPlayer player = (EntityPlayer) living;
		int frontSide = world.getBlockMetadata( x, y, z );
		if( frontSide == 0 || frontSide == 1 ) {
			frontSide = sideByAngles( player, x, z );
		}
		world.setBlockMetadataWithNotify( x, y, z, frontSide, 3 );
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOff, float yOff, float zOff) {

		if( player.isSneaking() ) {
			return false;
		}
		if( ! world.isRemote )
			player.openGui( XActMod.instance, 0, world, x, y, z );

		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		TileMachine entity = (TileMachine) world.getBlockTileEntity( x, y, z );

		if( entity != null )
			for( ItemStack stack : entity.getDropItems() ) {
				if( stack != null )
					Utils.dropItemAsEntity( world, x, y, z, stack );
			}

		super.breakBlock( world, x, y, z, par5, par6 );
		world.removeBlockTileEntity( x, y, z );
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add( new ItemStack( this, 1, 0 ) );
		return list;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileCrafter();
	}


	private int sideByAngles(EntityPlayer player, int x, int z) {
		double Dx = player.posX - x;
		double Dz = player.posZ - z;
		double angle = Math.atan2( Dz, Dx ) / Math.PI * 180 + 180;

		if( angle < 45 || angle > 315 )
			return 4;
		else if( angle < 135 )
			return 2;
		else if( angle < 225 )
			return 5;
		else
			return 3;
	}

	///////////////
	///// Textures
	@Override
	public Icon getBlockTextureFromSideAndMetadata(int side, int metadata) {
		switch( side ) {
			case 0: // bottom
				return TEXTURE_BOTTOM;
			case 1: // top
				return TEXTURE_TOP;
			default:
				if( side == metadata ) // front
					return TEXTURE_FRONT;
				else // any other side.
					return TEXTURE_SIDE;
		}
	}

	@SideOnly(Side.CLIENT)
	private static Icon TEXTURE_TOP, TEXTURE_BOTTOM, TEXTURE_FRONT, TEXTURE_SIDE;

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister) {
		TEXTURE_TOP = iconRegister.registerIcon( Textures.CRAFTER_TOP );
		TEXTURE_BOTTOM = iconRegister.registerIcon( Textures.CRAFTER_BOTTOM );
		TEXTURE_FRONT = iconRegister.registerIcon( Textures.CRAFTER_FRONT );
		TEXTURE_SIDE = iconRegister.registerIcon( Textures.CRAFTER_SIDE );
	}

}
