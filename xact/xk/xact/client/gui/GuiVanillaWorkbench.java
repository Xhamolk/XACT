package xk.xact.client.gui;


import invtweaks.api.ContainerGUI;
import invtweaks.api.ContainerSection;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import xk.xact.gui.ContainerVanillaWorkbench;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContainerGUI
public class GuiVanillaWorkbench extends GuiContainer {

	public GuiVanillaWorkbench(ContainerVanillaWorkbench container) {
		super( container );
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString( StatCollector.translateToLocal( "container.crafting" ), 28, 6, 4210752 );
		this.fontRenderer.drawString( StatCollector.translateToLocal( "container.inventory" ), 8, this.ySize - 96 + 2, 4210752 );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.mc.renderEngine.bindTexture( "/gui/crafting.png" );
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );
	}

	// Compatibility with Inventory Tweaks.
	@ContainerGUI.ContainerSectionCallback
	@SuppressWarnings({ "unchecked", "unused" })
	public Map<ContainerSection, List<Slot>> getContainerSections() {
		Map<ContainerSection, List<Slot>> map = new HashMap<ContainerSection, List<Slot>>();
		List<Slot> slots = inventorySlots.inventorySlots;

		map.put( ContainerSection.CRAFTING_OUT, slots.subList( 0, 1 ) ); // output slot
		map.put( ContainerSection.CRAFTING_IN, slots.subList( 1, 1 + 9 ) ); // crafting grid.

		return map;
	}


}
