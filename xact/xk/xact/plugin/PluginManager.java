package xk.xact.plugin;


import xk.xact.api.plugin.XACTPlugin;
import xk.xact.api.SpecialCasedRecipe;
import xk.xact.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class PluginManager {

	private static List<SpecialCasedRecipe> specialCasedRecipes = new ArrayList<SpecialCasedRecipe>();
	private static List<XACTPlugin> plugins = new ArrayList<XACTPlugin>();

	public static void checkEverything() {
	}

	public static void initializePlugins() {
		// Load ModularPowerSuits plug-in.
		Class mpsPlugin = ReflectionUtils.getClassByName( "xk.xact.plugin.mps.PluginForMPS" );
		if( mpsPlugin != null ) {
			Object instance = ReflectionUtils.newInstanceOf( mpsPlugin );
			if( instance != null ) {
				XACTPlugin.class.cast( instance ).initialize();
			}
		}

		// Load all other plugins.
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

}
