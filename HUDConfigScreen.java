/*
We got this working to some extent, there are still some issues.
Dragging overlaps other elements, if anyone can find a fix PLEASE send a pull request.
Possibly fixed by Jonathan Halterman#5542

Please use the ScreenPosition.java file that is included in this repository.
Thanks caterpillow#3310

Enjoy smooth dragging for your PvP Client!
*/

package location.of.your.hudconfigscreen;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class HUDConfigScreen extends GuiScreen {

    private final HashMap<IRenderer, ScreenPosition> renderers = new HashMap<IRenderer, ScreenPosition>();
    private Optional<IRenderer> selectedRenderer = Optional.empty();
    
    private int prevX, prevY;
    
    private boolean dragged;
    protected boolean hovered;

    @Override
    public void initGui() {
        // modified to add your own buttons <3
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch(button.id) {
            case /*id of the button*/:
                //do something
                break;
        }
    }

    public HUDConfigScreen(HUDManager api) {
        for (IRenderer ren : api.getRegisteredRenderers()) {
            if (!ren.isEnabled()) continue;

            ScreenPosition pos = ren.load();
            if (pos == null) pos = ScreenPosition.fromRelativePosition(0.5, 0.5);

            adjustBounds(ren, pos);
            this.renderers.put(ren, pos);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        
        for (IRenderer renderer : renderers.keySet()) {
            ScreenPosition pos = renderers.get(renderer);

            Gui.drawRect(pos.getAbsoluteX(), pos.getAbsoluteY(), pos.getAbsoluteX() + renderer.getWidth(), pos.getAbsoluteY() + renderer.getHeight(), 0x33FFFFFF);
            this.drawHollowRect(pos.getAbsoluteX(), pos.getAbsoluteY(), renderer.getWidth(), renderer.getHeight(), 0x88FFFFFF);
            
            renderer.renderDummy(pos);
            
            int x = pos.getAbsoluteX();
            int y = pos.getAbsoluteY();

            this.hovered = mouseX >= x && mouseX <= x + renderer.getWidth() && mouseY >= y && mouseY <= y + renderer.getHeight();
            if (this.hovered) {
            	Gui.drawRect(pos.getAbsoluteX(), pos.getAbsoluteY(), ren.getWidth() + pos.getAbsoluteX(), ren.getHeight() + pos.getAbsoluteY(), 0x43000000);
            	if (selectedRenderer.isPresent() && selectedRenderer.get() == ren && renderers.get(selectedRenderer.get()) == pos) {
                    pos.setAbsolute(pos.getAbsoluteX() + mouseX - this.prevX, pos.getAbsoluteY() + mouseY - this.prevY);

                    adjustBounds(ren, pos);
                    
                    this.drawHollowRect(x, y, ren.getWidth(), ren.getHeight(), new Color(70, 0, 70, 230).getRGB());
                    
                    this.prevX = mouseX;
                    this.prevY = mouseY;
                }
            }
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHollowRect(int x, int y, int w, int h, int color) {
        this.drawHorizontalLine(x, x + w, y, color);
        this.drawHorizontalLine(x, x + w, y + h, color);

        this.drawVerticalLine(x, y + h, y, color);
        this.drawVerticalLine(x + w, y + h, y, color);
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long time) {
        if (selectedRenderer.isPresent()) moveSelectedRenderBy(x - prevX, y - prevY);

        this.prevX = x;
        this.prevY = y;
    }

    private void moveSelectedRenderBy(int offsetX, int offsetY) {
        IRenderer renderer = selectedRenderer.get();
        ScreenPosition pos = renderers.get(renderer);

        pos.setAbsolute(pos.getAbsoluteX() + offsetX, pos.getAbsoluteY() + offsetY);

        adjustBounds(renderer, pos);
    }

    @Override
    public void onGuiClosed() {
        /*for (IRenderer renderer : renderers.keySet()) {
            renderer.save(renderers.get(renderer));
        }*/
        renderers.forEach(IRendererConfig::save); //Lambda is avaible from Java 8 and up, if you use older versions of Java 8 you won't be able to use it, so you have to use the normal for loop
    }

    private void adjustBounds(IRenderer renderer, ScreenPosition pos) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        int x = Math.max(0, Math.min(pos.getAbsoluteX(), Math.max(sr.getScaledWidth() - renderer.getWidth(), 0)));
        int y = Math.max(0, Math.min(pos.getAbsoluteY(), Math.max(sr.getScaledHeight() - renderer.getHeight(), 0)));

        pos.setAbsolute(x, y);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        this.prevX = x;
        this.prevY = y;

        loadMouseOver(x, y);
        super.mouseClicked(x, y, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.selectedRenderer = Optional.empty();

        super.mouseReleased(mouseX, mouseY, state);
    }

    private void loadMouseOver(int x, int y) {
        this.selectedRenderer = renderers.keySet().stream().filter(new MouseOverFinder(x, y)).findFirst();
    }

    private class MouseOverFinder implements Predicate<IRenderer> {
        private int mouseX, mouseY;

        public MouseOverFinder(int x, int y) {
            this.mouseX = x;
            this.mouseY = y;
        }

        @Override
        public boolean test(IRenderer renderer) {
            ScreenPosition pos = renderers.get(renderer);

            int x = pos.getAbsoluteX();
            int y = pos.getAbsoluteY();

            return (mouseX >= x && mouseX <= x + renderer.getWidth()) && (mouseY >= y && mouseY <= y + renderer.getHeight());
        }

    }

}
