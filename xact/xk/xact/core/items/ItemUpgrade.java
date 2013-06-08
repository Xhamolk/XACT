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
import xk.xact.config.ConfigurationManager;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.core.tileentities.TileWorkbench;
import xk.xact.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUpgrade extends Item {

	public final UpgradeType upgradeType;

	public ItemUpgrade(int itemID, UpgradeType upgradeType) {
		super( itemID );
		this.upgradeType = upgradeType;
		this.upgradeType.itemID = itemID;

		this.setUnlocalizedName( "upgrade" );
		this.setMaxStackSize( 1 );
		setCreativeTab( XActMod.xactTab );
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		if( upgradeType.canUpgrade( world, x, y, z ) ) {
			if( world.isRemote ) return true;
			List<ItemStack> contents = new ArrayList<ItemStack>();

			TileEntity tile = world.getBlockTileEntity( x, y, z );
			if( tile != null ) { // get contents in block.
				if( tile instanceof TileWorkbench ) {
					contents.addAll( Arrays.asList( ((TileWorkbench) tile).craftingGrid.getContents() ) );
				} else if( tile instanceof TileCrafter ) {
					contents.addAll( Arrays.asList( ((TileCrafter) tile).resources.getContents() ) );
				}
			}

			upgradeType.onUpgrade( world, x, y, z, contents ); // upgrade machine

			world.markBlockForUpdate( x, y, z ); // sync the clients.
			return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister registry) {
		itemIcon = registry.registerIcon( "xact:" + upgradeType.textureFile );
	}

	public enum UpgradeType {
		ToCrafter( "upgrade.toCrafter", "Upgrade to Crafter", "upgradeToCrafter", Block.workbench.blockID ) {
			@Override
			void onUpgrade(World world, int x, int y, int z, List<ItemStack> contents) {
				// Set the XACT Crafter
				world.setBlock( x, y, z, ConfigurationManager.machineID, 0, 3 );

				// Give back the contents
				TileCrafter tile = (TileCrafter) world.getBlockTileEntity( x, y, z );
				List<ItemStack> remaining = new ArrayList<ItemStack>();
				if( tile != null ) {
					for( ItemStack stack : contents ) {
						if( stack != null && stack.stackSize > 0 ) {
							if( !tile.resources.addStack( stack ) )
								remaining.add( stack );
						}
					}
				} else {
					remaining = contents;
				}
				for( ItemStack stack : remaining ) {
					if( stack != null && stack.stackSize > 0 )
						Utils.dropItemAsEntity( world, x, y, z, stack );
				}
			}
		};

		String unLocalizedName;
		String localizedName;
		String textureFile;

		int targetID = -1;
		int itemID = -1;

		UpgradeType(String unLocalizedName, String localizedName, String textureFile) {
			this.unLocalizedName = unLocalizedName;
			this.localizedName = localizedName;
			this.textureFile = textureFile;
		}

		UpgradeType(String unLocalizedName, String localizedName, String textureFile, int targetID) {
			this( unLocalizedName, localizedName, textureFile );
			this.targetID = targetID;
		}

		abstract void onUpgrade(World world, int x, int y, int z, List<ItemStack> contents);

		boolean canUpgrade(World world, int x, int y, int z) {
			if( targetID != -1 ) {
				int blockID = world.getBlockId( x, y, z );
				return blockID == targetID;
			}
			return false;
		}

		public String getLocalizedName() {
			return localizedName;
		}

	}

}
