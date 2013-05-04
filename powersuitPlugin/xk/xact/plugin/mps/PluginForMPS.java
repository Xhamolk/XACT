package xk.xact.plugin.mps;


import cpw.mods.fml.common.Loader;
import net.machinemuse.api.ModuleManager;
import xk.xact.api.plugin.XACTPlugin;
import xk.xact.util.Utils;

public class PluginForMPS implements XACTPlugin {

	@Override
	public void initialize() {
		if( Loader.isModLoaded( "mmmPowersuits" ) ) {
			Utils.log( "ModularPowerSuits detected. Initializing plug-in for MPS." );
			init();
		} else {
			Utils.log( "ModularPowerSuits not detected. Plug-in not initialized." );
		}
	}

	private static void init() {
		ModuleManager.addModule( new CraftPadModule() );
	}

}
