package xk.xact.plugin.betterstorage;

import cpw.mods.fml.common.Loader;
import net.mcft.copy.betterstorage.api.ICrateStorage;
import xk.xact.api.plugin.XACTPlugin;
import xk.xact.plugin.PluginManager;
import xk.xact.plugin.betterstorage.inventory.CrateInventory;
import xk.xact.util.Utils;

/**
 * Plug-in for Better Storage.
 * <p/>
 * Enables the crafting table to pull from crates.
 *
 * @author Xhamolk_
 */
public class PluginForBetterStorage implements XACTPlugin {

	@Override
	public void initialize() {
		if( Loader.isModLoaded( "betterstorage" ) ) {
			Utils.log( "Better Storage mod detected. Initializing plug-in..." );
			PluginManager.registerInventoryAdapter( ICrateStorage.class, new CrateInventory.Provider() );
		} else {
			Utils.log( "Better Storage mod not detected. Plug-in not initialized." );
		}
	}

}
