package mantle.client.pages;

import mantle.lib.client.MantleClientRegistry;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FurnacePage extends BookPage
{
    String text;

    ItemStack[] icons;

    @Override
    public void readPageFromXML(Element element)
    {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null)
        {
            this.text = nodes.item(0).getTextContent();
        }

        nodes = element.getElementsByTagName("recipe");
        if (nodes != null)
        {
            this.icons = MantleClientRegistry.getRecipeIcons(nodes.item(0).getTextContent());
        }
    }

    @Override
    public void renderContentLayer(int localWidth, int localHeight, boolean isTranslatable)
    {
        if (this.text != null)
        {
            if (isTranslatable)
            {
                this.text = StatCollector.translateToLocal(this.text);
            }
            this.manual.fonts.drawString("\u00a7n" + this.text, localWidth + 50, localHeight + 4, 0);
        }
        GL11.glScalef(2f, 2f, 2f);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        this.manual.renderitem.zLevel = 100;

        this.manual.renderitem.renderItemAndEffectIntoGUI(MantleClientRegistry.getManualIcon("coal"), (localWidth + 38) / 2, (localHeight + 110) / 2);
        this.manual.renderitem.renderItemAndEffectIntoGUI(this.icons[0], (localWidth + 106) / 2, (localHeight + 74) / 2);
        this.manual.renderitem.renderItemAndEffectIntoGUI(this.icons[1], (localWidth + 38) / 2, (localHeight + 38) / 2);

        if (this.icons[0].stackSize > 1)
        {
            this.manual.renderitem.renderItemOverlayIntoGUI(this.manual.fonts, this.icons[0], (localWidth + 106) / 2, (localHeight + 74) / 2, String.valueOf(this.icons[0].stackSize));
        }

        this.manual.renderitem.zLevel = 0;
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    private static final ResourceLocation background = new ResourceLocation("mantle", "textures/gui/bookfurnace.png");

    @Override
    public void renderBackgroundLayer(int localWidth, int localHeight)
    {
        this.manual.getMC().getTextureManager().bindTexture(background);
        this.manual.drawTexturedModalRect(localWidth + 32, localHeight + 32, 0, 0, 111, 114);
    }
}
