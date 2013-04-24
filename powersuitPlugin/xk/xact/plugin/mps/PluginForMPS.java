package xk.xact.plugin.mps;


import cpw.mods.fml.common.Loader;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.ModuleManager;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class PluginForMPS {

	public static void loadPlugin() {
		if( Loader.isModLoaded( "mmmPowersuits" ) ) {
			System.out.println("Initializing XACT's plug-in for ModularPowerSuits.");
			init();
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
