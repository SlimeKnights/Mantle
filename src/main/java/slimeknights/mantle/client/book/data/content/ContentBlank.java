package slimeknights.mantle.client.book.data.content;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.element.BookElement;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ContentBlank extends PageContent {
  public static final ResourceLocation ID = Mantle.getResource("blank");

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
  }
}
