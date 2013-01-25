package xk.xact.keybinding;

import java.util.EnumSet;

import xk.xact.api.InteractiveCraftingGui;
import xk.xact.gui.GuiCrafter;
import xk.xact.gui.GuiPad;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;

public class KeyBindingHandler extends KeyBindingRegistry.KeyHandler {

	public KeyBindingHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super(keyBindings, repeatings);
	}

	@Override
	public String getLabel() {
		return "xact test bindings";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {

		if (tickEnd) {
			if (FMLClientHandler.instance().getClient().currentScreen instanceof InteractiveCraftingGui) {
				((InteractiveCraftingGui)FMLClientHandler.instance().getClient().currentScreen).handleKeyBinding(kb.keyDescription);
			}
		}
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);

	}

}
