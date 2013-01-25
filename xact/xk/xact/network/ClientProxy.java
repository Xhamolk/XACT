package xk.xact.network;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.MinecraftForgeClient;
import xk.xact.XActMod;
import xk.xact.keybinding.KeyBindingHandler;

public class ClientProxy extends CommonProxy {

	@SideOnly(Side.CLIENT)
	public static GuiScreen getCurrentScreen() {
		return Minecraft.getMinecraft().currentScreen;
	}

	public void registerRenderInformation() {
		MinecraftForgeClient.preloadTexture(XActMod.TEXTURE_ITEMS);
		MinecraftForgeClient.preloadTexture(XActMod.TEXTURE_BLOCKS);
	}

	public void registerKeyBindings() {
		KeyBindingRegistry.registerKeyBinding(new KeyBindingHandler(
				new KeyBinding[] { 
						new KeyBinding("xact.clear", 208),
						new KeyBinding("xact.load", 200),
						new KeyBinding("xact.prev", 203),
						new KeyBinding("xact.next", 205),
						new KeyBinding("xact.delete", 211)
				}, new boolean[] {
						false,
						false,
						false,
						false,
						false
				}));

	}
}
