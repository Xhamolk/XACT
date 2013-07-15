package xk.xact.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.inventory.InventoryUtils;

import java.util.EnumSet;

public class KeyBindingHandler extends KeyBindingRegistry.KeyHandler {

	public KeyBindingHandler() {
		super( keyBindings(), repeatings() );
	}

	@Override
	public String getLabel() {
		return "xact test bindings";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		if( tickEnd ) {
			GuiScreen currentScreen = FMLClientHandler.instance().getClient().currentScreen;
			if( currentScreen == null ) {
				if( kb.keyDescription.equals( "xact.openGrid" ) ) {
					int slot = getCraftPadIndex();
					if( slot != -1 )
						GuiUtils.openGui( 3, slot + 1 ); // Open Craft Pad GUI.
				}
				return;
			}
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

	private static KeyBinding[] keyBindings() {
		return new KeyBinding[] {
				new KeyBinding( "xact.clear", Keyboard.KEY_DOWN ),
				new KeyBinding( "xact.load", Keyboard.KEY_UP ),
				new KeyBinding( "xact.prev", Keyboard.KEY_LEFT ),
				new KeyBinding( "xact.next", Keyboard.KEY_RIGHT ),
				new KeyBinding( "xact.delete", Keyboard.KEY_DELETE ),
				new KeyBinding( "xact.openGrid", Keyboard.KEY_C )
		};
	}

	private static boolean[] repeatings() {
		return new boolean[] {
				false,
				false,
				false,
				false,
				false,
				false
		};
	}

	private int getCraftPadIndex() {
		return InventoryUtils.checkHotbar( Minecraft.getMinecraft().thePlayer, new ItemStack( XActMod.itemCraftPad ) );
	}

}
