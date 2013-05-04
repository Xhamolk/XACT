package xk.xact.plugin.mps;


import net.machinemuse.api.IModularItem;
import net.machinemuse.api.moduletrigger.IRightClickModule;
import net.machinemuse.powersuits.common.ModularPowersuits;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.MuseCommonStrings;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xk.xact.XActMod;

import java.util.Arrays;

public class CraftPadModule extends PowerModuleBase implements IRightClickModule {

	public static final String MODULE_NAME = "XACT CraftPad";
	public static final String MODULE_CATEGORY = MuseCommonStrings.CATEGORY_SPECIAL;
	public static final String MODULE_DESCRIPTION = "Build-in the CraftPad into your Power Tool";

	@SuppressWarnings("unchecked")
	public CraftPadModule() {
		super( Arrays.asList( (IModularItem) ModularPowersuits.powerTool ) );
		addInstallCost( new ItemStack( XActMod.itemCraftPad ) );
	}

	@Override
	public void onRightClick(EntityPlayer player, World world, ItemStack item) {
		player.openGui( XActMod.instance, 3, world, 0, 0, 0 );
	}

	@Override
	public void onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		// Unused
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
//		player.openGui( XActMod.instance, 3, world, x, y, z );
		// I could load a chest's contents!
		// But I'd need a different GUI for displaying that.
		return false;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int par4) {
		// Unused
	}

	@Override
	public void registerIcon(IconRegister register) {
		this.icon = XActMod.itemCraftPad.getIconFromDamage( 0 );
	}

	@Override
	public String getCategory() {
		return MODULE_CATEGORY;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String getTextureFile() {
		return null; // unused
	}

	@Override
	public String getDescription() {
		return MODULE_DESCRIPTION;
	}

}
