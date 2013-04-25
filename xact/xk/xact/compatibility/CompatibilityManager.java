package xk.xact.compatibility;


import xk.xact.plugin.mps.PluginForMPS;
import xk.xact.recipes.SpecialCasedRecipe;

import java.util.ArrayList;
import java.util.List;

public class CompatibilityManager {

	private static List<SpecialCasedRecipe> specialCasedRecipes = new ArrayList<SpecialCasedRecipe>();


	public static void checkEverything() {
	}

	public static void initializePlugins() {
		PluginForMPS.loadPlugin();
	}

	public static List<SpecialCasedRecipe> getSpecialCasedRecipes() {
		return specialCasedRecipes;
	}

	private static boolean classMatches(Object o, String className) {
		return o != null && o.getClass().getSimpleName().equals( className );
	}

	private static Class getClass(String classFullName) {
		try {
			return Class.forName( classFullName );
		} catch( ClassNotFoundException e ) {
			return null;
		}
	}
}
