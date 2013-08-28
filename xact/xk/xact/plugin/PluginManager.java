package xk.xact.plugin;


import net.minecraft.inventory.IInventory;
import xk.xact.api.IInventoryAdapterProvider;
import xk.xact.api.SpecialCasedRecipe;
import xk.xact.api.plugin.XACTPlugin;
import xk.xact.config.ConfigurationManager;
import xk.xact.plugin.appliedenergistics.AEProxy;
import xk.xact.util.ReflectionUtils;
import xk.xact.util.Utils;

import java.util.*;

public class PluginManager {

	// ---------- Proxies ---------- //

	public static AEProxy aeProxy = null;


	private static List<SpecialCasedRecipe> specialCasedRecipes = new ArrayList<SpecialCasedRecipe>();
	private static List<XACTPlugin> plugins = new ArrayList<XACTPlugin>();
	private static Map<Class, IInventoryAdapterProvider> inventoryAdapters = new HashMap<Class, IInventoryAdapterProvider>();

	public static void checkEverything() {
	}

	public static void initializePlugins() {
		if( ConfigurationManager.ENABLE_MPS_PLUGIN ) {
			// Register ModularPowerSuits plug-in.
			Class mpsPlugin = ReflectionUtils.getClassByName( "xk.xact.plugin.mps.PluginForMPS" );
			if( mpsPlugin != null ) {
				Object instance = ReflectionUtils.newInstanceOf( mpsPlugin );
				if( instance != null ) {
					addPlugin( XACTPlugin.class.cast( instance ) );
				}
			}
		}

		if( ConfigurationManager.ENABLE_BETTER_STORAGE_PLUGIN ) {
			// Register BetterStorage plug-in.
			Class betterStoragePlugin = ReflectionUtils.getClassByName( "xk.xact.plugin.betterstorage.PluginForBetterStorage" );
			if( betterStoragePlugin != null ) {
				Object instance = ReflectionUtils.newInstanceOf( betterStoragePlugin );
				if( instance != null ) {
					addPlugin( XACTPlugin.class.cast( instance ) );
				}
			}
		}

		if( ConfigurationManager.ENABLE_AE_PLUGIN ) {
			// Register Applied Energistics plug-in.
			Class aePlugin = ReflectionUtils.getClassByName( "xk.xact.plugin.appliedenergistics.PluginForAppliedEnergistics" );
			if( aePlugin != null ) {
				Object instance = ReflectionUtils.newInstanceOf( aePlugin );
				if( instance != null ) {
					addPlugin( XACTPlugin.class.cast( instance ) );
				}
			}
		}

		// Load all other plugins.
		Utils.log( "Loading plug-ins..." );
		for( XACTPlugin plugin : plugins ) {
			plugin.initialize();
		}

		// Clear the list.
		plugins.clear();
		plugins = null;
	}

	public static void addPlugin(XACTPlugin plugin) {
		plugins.add( plugin );
	}

	public static void addSpecialCasedRecipe(SpecialCasedRecipe specialCase) {
		if( specialCase != null )
			specialCasedRecipes.add( specialCase );
	}

	public static List<SpecialCasedRecipe> getSpecialCasedRecipes() {
		return specialCasedRecipes;
	}

	public static Map<Class, IInventoryAdapterProvider> getInventoryAdapters() {
		return Collections.unmodifiableMap( inventoryAdapters );
	}

	public static void registerInventoryAdapter(Class inventoryClass, IInventoryAdapterProvider provider) {
		if( inventoryClass != null && provider != null ) {
			if( !inventoryClass.equals( IInventory.class ) ) {
				inventoryAdapters.put( inventoryClass, provider );
			}
		}
	}
}
