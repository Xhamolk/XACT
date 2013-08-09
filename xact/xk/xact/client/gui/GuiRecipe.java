package xk.xact.client.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import xk.xact.client.GuiUtils;
import xk.xact.util.Textures;
import xk.xact.inventory.InventoryUtils;
import xk.xact.network.ClientProxy;

// GUI used to set the recipe of a node.
public class GuiRecipe extends GuiCrafting {

	private static final ResourceLocation guiTexture = new ResourceLocation( Textures.GUI_RECIPE );

	private EntityPlayer player;

	public GuiRecipe(EntityPlayer player, Container container) {
		super( container );
		this.player = player;
	}

	private ItemStack target = null;

	public void setTarget(ItemStack target) {
		this.target = target;
	}

	@Override
	protected ResourceLocation getBaseTexture() {
		return guiTexture;
	}

	@Override
	public String getGuiTitle() {
		return "Choose the Recipe";
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
		super.drawGuiContainerBackgroundLayer( partial, x, y );
		if( matching = matchingTarget() ) {
			// draw the "success" button
			this.drawTexturedModalRect( guiLeft + 117, guiTop+ 63, 176, 0, 14, 14 );
		} else {
			// Paint the target.
			paintTarget();
		}
		// todo: draw slot overlays.
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) {
		if( matching && guiLeft + 117 <= x && x < guiTop + 117 + 14 ) {
			if( guiLeft + 63 <= y && y < guiTop + 63 + 14 ) {
				// todo: either 1 or 2.
				buttonClicked( 1 );
				return;
			}
		}
		super.mouseClicked( x, y, mouseButton );
	}

	@Override
	protected void keyTyped(char par1, int key) {
		if( key == 1 ) {
			buttonClicked( 0 );
		}
		super.keyTyped( par1, key );
	}

	private boolean matching = false;

	private boolean matchingTarget() {
		Slot outputSlot = player.openContainer.getSlot( 0 );
		if( target == null ) {
			return outputSlot.getHasStack();
		}
		return outputSlot.getHasStack() && InventoryUtils.similarStacks( outputSlot.getStack(), target, false );
	}

	private void paintTarget() {
		Slot slot = player.openContainer.getSlot( 0 );

		if( !matching && target != null ) {
			int x = slot.xDisplayPosition, y = slot.yDisplayPosition;

			this.zLevel = 100.0F;
			itemRenderer.zLevel = 100.0F;
			GL11.glEnable( GL11.GL_DEPTH_TEST );
			itemRenderer.renderItemAndEffectIntoGUI( this.fontRenderer, this.mc.renderEngine, target, x, y );
			itemRenderer.renderItemOverlayIntoGUI( this.fontRenderer, this.mc.renderEngine, target, x, y );
			itemRenderer.zLevel = 0.0F;
			this.zLevel = 0.0F;
		}

		int color = 165 << 24 | GuiUtils.COLOR_GRAY;
		GuiUtils.paintSlotOverlay( slot, 16, color );
	}

	private void buttonClicked(int i) {
		// todo: 0 is cancel, 1 is accept, 2 is clear.
	}

	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		if( ingredients == null ) {
			GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) -1, null );
			return;
		}
		GuiUtils.sendItemsToServer( ClientProxy.getNetClientHandler(), ingredients, 1);
	}

}
