package xk.xact.network;


import net.minecraftforge.client.MinecraftForgeClient;
import xk.xact.XActMod;

public class ClientProxy extends CommonProxy {

	public void registerRenderInformation() {
		MinecraftForgeClient.preloadTexture(XActMod.TEXTURE_ITEMS);
		MinecraftForgeClient.preloadTexture(XActMod.TEXTURE_BLOCKS);
	}

}
