package xk.xact.plugin.mps;


import cpw.mods.fml.common.Loader;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.ModuleManager;
import net.minecraft.item.Item;
import xk.xact.api.plugin.XACTPlugin;
import xk.xact.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class PluginForMPS implements XACTPlugin {

	@Override
	public void initialize() {
		if( Loader.isModLoaded( "mmmPowersuits" ) ) {
			Utils.log( "Initializing plug-in for ModularPowerSuits." );
			init();
		} else {
			Utils.log( "ModularPowerSuits not detected. Plug-in not initialized." );
		}
	}

	private static void init() {
		ModuleManager.addModule( new CraftPadModule() );
	}

	public static List<IModularItem> validItems(Item... items) {
		ArrayList<IModularItem> validItems = new ArrayList<IModularItem>();
		for( Item item : items ) {
			if( item != null && item instanceof IModularItem )
				validItems.add( (IModularItem) item );
		}
		return validItems;
	}

}
