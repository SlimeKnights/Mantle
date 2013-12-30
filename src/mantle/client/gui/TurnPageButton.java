package mantle.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TurnPageButton extends GuiButton
{
    /**
     * True for pointing right (next page), false for pointing left (previous page).
     */
    private final boolean nextPage;

    public TurnPageButton(int par1, int par2, int par3, boolean par4)
    {
        super(par1, par2, par3, 23, 13, "");
        this.nextPage = par4;
    }

    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/bookleft.png");

    /**
     * Draws this button to the screen.
     */
    public void drawButton (Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.field_146125_m)//TODO drawButton?
        {
            //TODO xPosition, yPosition, zPosition, width, height
            boolean var4 = par2 >= this.field_146128_h && par3 >= this.field_146129_i && par2 < this.field_146128_h + this.field_146120_f && par3 < this.field_146129_i + this.field_146121_g;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            par1Minecraft.getTextureManager().bindTexture(background);
            int var5 = 0;
            int var6 = 192;

            if (var4)
            {
                var5 += 23;
            }

            if (!this.nextPage)
            {
                var6 += 13;
            }

            this.drawTexturedModalRect(this.field_146128_h, this.field_146129_i, var5, var6, 23, 13);
        }
    }
}
