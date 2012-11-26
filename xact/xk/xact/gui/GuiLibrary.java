package xk.xact.gui;


import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.ItemStack;
import org.lwjgl.opengl.GL11;
import xk.xact.core.ItemChip;

public class GuiLibrary extends GuiContainer {

	public GuiLibrary(Container par1Container) {
		super(par1Container);
		this.xSize = 196;
		this.ySize = 191;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/library.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);


	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		// the title
	}

	protected ItemChip getChipAt(int x, int y){
		return null; // returning null means can't find.
	}


	// the painted grid:
		// position: (14, 16), size (52x77)
		// texture position: (197, 9)

	// painted slots:
		// main: 31, 16
		// first: 13, 40 (slot size 18x18)


	// to paint the items:
		// func_85044_b(ItemStack stack, int x, int y)        // copy this method, as it's private.
	// tooltip: func_74184_a(ItemStack stack, int x, int y)   // this one is protected, so don't copy.


	protected void paintChip(ItemStack chip) {

	}

}
