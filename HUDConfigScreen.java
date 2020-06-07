/*
We got this working to some extent, there are still some issues.
Dragging overlaps other elements, if anyone can find a fix PLEASE send a pull request.

Excuse my spaghetti code. I tried my hardest to clean it up.
Enjoy smooth dragging for your PvP Client!
*/

package this.is.where.your.HUDConfigScreen.is.located;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

public class HUDConfigScreen extends GuiScreen {

	private final HashMap<IRenderer, ScreenPosition> renderers = new HashMap<IRenderer, ScreenPosition>();

	private Optional<IRenderer> selectedRenderer = Optional.empty();

	private int prevX, prevY;
	
	// Can't remember if this is used or not.
	private int smX, smY;

	// These booleans are needed for smooth dragging.
	private boolean dragged = false;

	protected boolean hovered;

	public HUDConfigScreen(HUDManager api) {

		Collection<IRenderer> registeredRenderers = api.getRegisteredRenderers();

		for(IRenderer ren : registeredRenderers) {
			if(!ren.isEnabled()) {
				continue;
			}

			ScreenPosition pos = ren.load();

			if(pos == null) {
				pos = ScreenPosition.fromRelativePosition(0.5, 0.5);
			}

			adjustBounds(ren, pos);
			this.renderers.put(ren, pos);
		}
	}

	public void initGui() {

		// Modified to allow you to draw buttons on your mod edit screen <3

	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {

		// Only used if you are adding buttons to the screen.

		this.updateScreen();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// This might be an error, I don't have my IDE open while editing this to check.
		super.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);

		final float zBackup = this.zLevel;
		this.zLevel = 200;

		for(IRenderer renderer : renderers.keySet()) {

			ScreenPosition pos = renderers.get(renderer);

			renderer.renderDummy(pos);

			// Start of smooth dragging.

			this.hovered = mouseX >= pos.getAbsoluteX() && mouseY >= pos.getAbsoluteY() && mouseX < pos.getAbsoluteX() + renderer.getWidth() && mouseY < pos.getAbsoluteY() + renderer.getHeight();

			if (this.hovered) {
				if (dragged == true) {
                    pos.setAbsolute(pos.getAbsoluteX() + mouseX - this.prevX, pos.getAbsoluteY() + mouseY - this.prevY);

                    adjustBounds(renderer, pos);

                    this.prevX = mouseX;
                    this.prevY = mouseY;
                }
			}

			// End of smooth dragging.

		}

		// Once again, I'm not sure if this is used.
		this.smX = mouseX;
		this.smY = mouseY;

		this.zLevel = zBackup;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			renderers.entrySet().forEach((entry) -> {
				entry.getKey().save(entry.getValue());
			});
			this.mc.displayGuiScreen(null);
		}
	}

	@Override
    protected void mouseClickMove(int x, int y, int button, long time)
    {
        if (selectedRenderer.isPresent())
        {
            moveSelectedRenderBy(x - prevX, y - prevY);
        }

        this.prevX = x;
        this.prevY = y;
    }

    private void moveSelectedRenderBy(int offsetX, int offsetY)
    {
        IRenderer renderer = selectedRenderer.get();
        ScreenPosition pos = renderers.get(renderer);
        pos.setAbsolute(pos.getAbsoluteX() + offsetX, pos.getAbsoluteY() + offsetY);

        // Specifies Borders
        if (pos.getAbsoluteX() == 0 << 1) {
            pos.setAbsolute(1, pos.getAbsoluteY());
        }

        if (pos.getAbsoluteY() == 0 << 1) {
            pos.setAbsolute(pos.getAbsoluteX(), 1);
        }

        adjustBounds(renderer, pos);
    }

	@Override
	public void onGuiClosed() {

		for(IRenderer renderer : renderers.keySet()) {
			renderer.save(renderers.get(renderer));
		}

		super.onGuiClosed();

	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	private void adjustBounds(IRenderer renderer, ScreenPosition pos) {

		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

		int screenWidth = res.getScaledWidth();
		int screenHeight = res.getScaledHeight();

		int absoluteX = Math.max(0, Math.min(pos.getAbsoluteX(), Math.max(screenWidth - renderer.getWidth(), 0)));
		int absoluteY = Math.max(0, Math.min(pos.getAbsoluteY(), Math.max(screenHeight - renderer.getHeight(), 0)));

		pos.setAbsolute(absoluteX, absoluteY);
	}

	@Override
	protected void mouseClicked(int x, int y, int mobuttonuseButton) throws IOException {
		loadMouseOver(this.prevX = x, this.prevY = y);

		// NECESSARY FOR SMOOTH DRAGGING
		// Set the drag to true if mouse is clicked.
		dragged = true;

		super.mouseClicked(x, y, mobuttonuseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {

		// NECESSARY FOR SMOOTH DRAGGING
		// Set the drag to false once released.
		dragged = false;

		super.mouseReleased(mouseX, mouseY, state);
	}

	private void loadMouseOver(int x, int y) {
		this.selectedRenderer = renderers.keySet().stream().filter(new MouseOverFinder(smX, smY)).findFirst();
	}

	private class MouseOverFinder implements Predicate<IRenderer> {

		private int mouseX, mouseY;

		public MouseOverFinder(int x, int y) {
			this.mouseX = smX;
			this.mouseY = smY;
		}

		@Override
		public boolean test(IRenderer renderer) {

			ScreenPosition pos = renderers.get(renderer);

			int absoluteX = pos.getAbsoluteX();
			int absoluteY = pos.getAbsoluteY();

			if(mouseX >= absoluteX && mouseX <= absoluteX + renderer.getWidth()) {

				if(mouseY >= absoluteY && mouseY <= absoluteY + renderer.getHeight()) {

					return true;

				}

			}

			return false;
		}

	}


}
