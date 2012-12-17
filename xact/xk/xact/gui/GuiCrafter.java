package xk.xact.gui;


import net.minecraft.src.EntityPlayer;
import org.lwjgl.opengl.GL11;
import xk.xact.core.TileCrafter;

public class GuiCrafter extends GuiMachine {

	private TileCrafter crafter;

	public GuiCrafter(TileCrafter crafter, EntityPlayer player){
		super(new ContainerCrafter(crafter, player));
		this.crafter = crafter;
		this.ySize = 220;
	}

	public void onInit() {
		crafter.updateRecipes();
		crafter.updateStates();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int xPos = (this.xSize - fontRenderer.getStringWidth("X.A.C.T. Crafter")) / 2;
		this.fontRenderer.drawString("X.A.C.T. Crafter", xPos, 6, 4210752);
		this.fontRenderer.drawString("Player's Inventory", 8, this.ySize - 94, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/crafter_2.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		// paint the overlays
		for( int i=0; i<4; i++ ) {
			paintOverlay(i, cornerX, cornerY);
		}
	}

	// next versions will add more colors (yellow)
	private void paintOverlay(int index, int cornerX, int cornerY) {
		int overlayIndex = getOverlayIndex(index);
		if( overlayIndex == -1 )
			return;

		// coordinates: x= 21+36*index; y=20
		int x = cornerX + 21 + 36*index;
		int y = cornerY + 20;

		// textures: green(176,0) red(176,52); sizes 26x26
		int textureX = 176;
		int textureY = 26*overlayIndex;

		this.drawTexturedModalRect( x, y,  textureX, textureY,  26, 26 );
	}

	private int getOverlayIndex(int recipeIndex){
		if( crafter.getRecipeResult(recipeIndex) != null ){
			if( crafter.isRedState(recipeIndex) ) {
				return 2; // red state
			} else {
				// next version will also use yellow overlay.
				return 0; // green.
			}
		}
		return -1; // none
	}

}
