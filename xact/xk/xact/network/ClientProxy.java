package xk.xact.network;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.MinecraftForgeClient;
import xk.xact.XActMod;

public class ClientProxy extends CommonProxy {

	@SideOnly(Side.CLIENT)
	public static GuiScreen getCurrentScreen() {
		return Minecraft.getMinecraft().currentScreen;
	}

	public void registerRenderInformation() {
		MinecraftForgeClient.preloadTexture(XActMod.TEXTURE_ITEMS);
		MinecraftForgeClient.preloadTexture(XActMod.TEXTURE_BLOCKS);
	}

}
