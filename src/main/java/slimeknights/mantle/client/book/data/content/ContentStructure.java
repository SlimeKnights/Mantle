package slimeknights.mantle.client.book.data.content;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.screen.book.ArrowButton;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.AnimationToggleElement;
import slimeknights.mantle.client.screen.book.element.StructureElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContentStructure extends PageContent {

  public static final transient String ID = "structure";

  public IFormattableTextComponent title;
  public String data;

  public IFormattableTextComponent text;

  public final transient Template template = new Template();
  public transient List<Template.BlockInfo> templateBlocks = new ArrayList<>();

  @Override
  public void load() {
    BookRepository repo = this.parent.source;

    if (this.data == null || this.data.isEmpty()) {
      return;
    }

    ResourceLocation location = repo.getResourceLocation(this.data);
    IResource resource = repo.getResource(location);

    if (resource == null) {
      return;
    }

    try {
      CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(resource.getInputStream());
      this.template.read(compoundnbt);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    this.templateBlocks = this.template.blocks.get(0).func_237157_a_();

    for (int i = 0; i < this.templateBlocks.size(); i++) {
      Template.BlockInfo info = this.templateBlocks.get(i);
      if (info.state == Blocks.AIR.getDefaultState()) {
        this.templateBlocks.remove(i);
        i--;
      } else if (info.state.isAir())
        // Usually means it contains a block that has been renamed
        Mantle.logger.error("Found non-default air block in template " + this.data);
    }
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = TITLE_HEIGHT;

    if (this.title == null || this.title.getString().isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    int offset = 0;
    int structureSizeX = BookScreen.PAGE_WIDTH;
    int structureSizeY = BookScreen.PAGE_HEIGHT - y - 10;

    if (this.text != null && !StringUtils.isNullOrEmpty(this.text.getString())) {
      offset = 15;
      structureSizeX -= 2 * offset;
      structureSizeY -= 2 * offset;

      list.add(new TextElement(0, BookScreen.PAGE_HEIGHT - 10 - 2 * offset, BookScreen.PAGE_WIDTH, 2 * offset, this.text));
    }

    if (this.template != null && this.template.getSize() != BlockPos.ZERO) {
      boolean showButtons = this.template.getSize().getY() > 1;

      StructureElement structureElement = new StructureElement(offset, y, structureSizeX, structureSizeY, this.template, this.templateBlocks);
      list.add(structureElement);

      if (showButtons) {
        int col = book.appearance.structureButtonColor;
        int colHover = book.appearance.structureButtonColorHovered;
        int colToggled = book.appearance.structureButtonColorToggled;

        list.add(new AnimationToggleElement(BookScreen.PAGE_WIDTH - ArrowButton.ArrowType.REFRESH.w, 0, ArrowButton.ArrowType.REFRESH, col, colHover, colToggled, structureElement));
      }
    }
  }
}
