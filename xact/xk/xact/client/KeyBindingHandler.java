package xk.xact.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import xk.xact.api.InteractiveCraftingGui;

import java.util.EnumSet;

public class KeyBindingHandler extends KeyBindingRegistry.KeyHandler {

	public KeyBindingHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super( keyBindings, repeatings );
	}

	@Override
	public String getLabel() {
		return "xact test bindings";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		if( tickEnd ) {
			GuiScreen currentScreen = FMLClientHandler.instance().getClient().currentScreen;
			if( currentScreen instanceof InteractiveCraftingGui ) {
				((InteractiveCraftingGui) currentScreen).handleKeyBinding( kb.keyCode, kb.keyDescription );
			}
		}
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of( TickType.CLIENT );
	}

}
