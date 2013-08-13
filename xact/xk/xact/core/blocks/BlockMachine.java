package xk.xact.core.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import xk.xact.XActMod;
import xk.xact.core.Machines;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.core.tileentities.TileMachine;
import xk.xact.util.Utils;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack itemStack) {
		EntityPlayer player = (EntityPlayer) living;
		int side = sideByAngles( player, x, z );
		int frontSide = side / 2 - 1;
		int metadata = ((itemStack.getItemDamage() & 0x7) << 1) | (frontSide & 1);
		world.setBlockMetadataWithNotify( x, y, z, metadata, 3 );
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOff, float yOff, float zOff) {
		if( player.isSneaking() ) {
			return false;
		}
		if( !world.isRemote )
			player.openGui( XActMod.instance, 0, world, x, y, z );

		return true;
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int metadata, EntityPlayer player) {
		if( player.capabilities.isCreativeMode )
			return;

		TileMachine entity = (TileMachine) world.getBlockTileEntity( x, y, z );
		if( entity != null ) {
			for( ItemStack stack : entity.getDropItems() ) {
				if( stack != null )
					Utils.dropItemAsEntity( world, x, y, z, stack );
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID) {
		TileEntity tileEntity = world.getBlockTileEntity( x, y, z );
		if( tileEntity != null && tileEntity instanceof TileMachine ) {
			((TileMachine) tileEntity).onBlockUpdate( 0 ); // Trigger update on next update tick.
		}
	}

	@Override
	public void onNeighborTileChange(World world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		TileEntity tileEntity = world.getBlockTileEntity( x, y, z );
		if( tileEntity != null && tileEntity instanceof TileMachine ) {
			((TileMachine) tileEntity).onBlockUpdate( 1 ); // Tile E
		}
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add( new ItemStack( this, 1, Machines.getMachineFromMetadata( metadata ) ) );
		return list;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileCrafter();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubBlocks(int blockID, CreativeTabs creativeTab, List list) {
		for( int i = 0; i < Machines.values().length; i++ ) {
			list.add( new ItemStack( blockID, 1, i ) );
		}
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
	@SideOnly(Side.CLIENT)
	private static Icon[][] textures;

	@Override
	public Icon getIcon(int side, int metadata) {
		int machine = Machines.getMachineFromMetadata( metadata );
		if( side >= 2 ) {
			side = isFrontSide( side, metadata ) ? 2 : 3;
		}
		return textures[machine][side];
	}

	private boolean isFrontSide(int side, int metadata) {
		return side / 2 - 1 == (metadata & 1);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister) {
		textures = new Icon[Machines.values().length][4];

		// For each machine: bottom, top, front side, other side.
		for( int machine = 0; machine < Machines.values().length; machine++ ) {
			String[] textureFiles = Machines.getTextureFiles( machine );
			for( int side = 0; side < 4; side++ ) {
				textures[machine][side] = iconRegister.registerIcon( textureFiles[side] );
			}
		}
	}

}
