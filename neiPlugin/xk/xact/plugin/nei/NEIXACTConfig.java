package xk.xact.plugin.nei;


import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.DefaultOverlayHandler;
import xk.xact.client.gui.GuiCrafter;
import xk.xact.client.gui.GuiPad;
import xk.xact.client.gui.GuiRecipe;
import xk.xact.client.gui.GuiVanillaWorkbench;

public class NEIXACTConfig implements IConfigureNEI {

	@Override
	public void loadConfig() {

		// Register the handlers for the replaced (vanilla) workbench.
		API.registerGuiOverlay( GuiVanillaWorkbench.class, "crafting" );
		API.registerGuiOverlayHandler( GuiVanillaWorkbench.class, new DefaultOverlayHandler(), "crafting" );

		// Register the overlay handlers for the crafting devices.
		XactOverlayHandler handler = new XactOverlayHandler();

		API.registerGuiOverlayHandler( GuiPad.class, handler, "crafting" );
		API.registerGuiOverlayHandler( GuiRecipe.class, handler, "crafting" );
		API.registerGuiOverlayHandler( GuiCrafter.class, handler, "crafting" );

		API.registerUsageHandler( new ChipHandler() );
	}

	@Override
	public String getName() {
		return "xact_nei_plugin";
	}

	@Override
	public String getVersion() {
		return "v1.3";
	}
}
