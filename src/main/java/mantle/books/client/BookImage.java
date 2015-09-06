package mantle.books.client;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class BookImage
{
    public int height, width;

    public DynamicTexture texture;

    public ResourceLocation resource;

    public BookImage(int width, int height, DynamicTexture texture, ResourceLocation resource)
    {
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.resource = resource;
    }
}
