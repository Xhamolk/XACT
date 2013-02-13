package xk.xact.gui.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import xk.xact.core.ItemChip;
import xk.xact.gui.GuiUtils;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

public class ChipRenderer implements IItemRenderer {

	private static RenderItem itemRender = new RenderItem();

	// Prevents an infinite loop when drawing a recipe chip on a recipe chip...
	private boolean drawing = false;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return canRevealRecipe() && item.getItem() instanceof ItemChip
				&& ((ItemChip) item.getItem()).encoded && type == ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false; // No, this is not a block.
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data) {
		drawing = true;

		CraftRecipe recipe = RecipeUtils.getRecipe( itemStack, Minecraft.getMinecraft().theWorld );
		if( recipe != null ) {
			GL11.glEnable( GL11.GL_LIGHTING );
			GuiUtils.paintItem( recipe.getResult(), 0, 0, Minecraft.getMinecraft(), itemRender );
		}
		// Green overlay
		GuiUtils.paintEffectOverlay( 0, 0, Minecraft.getMinecraft().renderEngine, itemRender, 0.25f, 0.55f, 0.3f, 0.85f );

		drawing = false;
		GL11.glEnable( GL11.GL_CULL_FACE );
	}

	private boolean canRevealRecipe() {
		return !drawing && GuiUtils.isRevealKeyPressed();
	}

}
