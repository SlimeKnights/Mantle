package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.apache.commons.lang3.StringUtils;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.BookLoadException;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.IngredientData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ImageElement;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;

import static slimeknights.mantle.client.screen.book.Textures.TEX_CRAFTING;

public class ContentCrafting extends PageContent {
  public static final ResourceLocation ID = Mantle.getResource("crafting");

  public static final transient int TEX_SIZE = 256;
  public static final transient ImageData IMG_CRAFTING_LARGE = new ImageData(TEX_CRAFTING, 0, 0, 183, 114, TEX_SIZE, TEX_SIZE);
  public static final transient ImageData IMG_CRAFTING_SMALL = new ImageData(TEX_CRAFTING, 0, 114, 155, 78, TEX_SIZE, TEX_SIZE);

  public static final transient int X_RESULT_SMALL = 118;
  public static final transient int Y_RESULT_SMALL = 23;
  public static final transient int X_RESULT_LARGE = 146;
  public static final transient int Y_RESULT_LARGE = 41;

  public static final transient float ITEM_SCALE = 2.0F;
  public static final transient int SLOT_MARGIN = 5;
  public static final transient int SLOT_PADDING = 4;

  @Getter
  public String title = "Crafting";
  public String grid_size = "auto";
  public IngredientData[][] grid;
  public IngredientData result;
  public TextData[] description;
  public String recipe;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int x = 0;
    int y;
    int height = 100;
    int resultX = 100;
    int resultY = 50;

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
      y = getTitleHeight();
    }

    // Fallback for if grid size is not specified in a manual recipe
    String size = this.grid_size.equalsIgnoreCase("auto") ? "large" : this.grid_size;

    if (size.equalsIgnoreCase("small")) {
      x = BookScreen.PAGE_WIDTH / 2 - IMG_CRAFTING_SMALL.width / 2;
      height = y + IMG_CRAFTING_SMALL.height;
      list.add(new ImageElement(x, y, IMG_CRAFTING_SMALL.width, IMG_CRAFTING_SMALL.height, IMG_CRAFTING_SMALL, book.appearance.slotColor));
      resultX = x + X_RESULT_SMALL;
      resultY = y + Y_RESULT_SMALL;
    } else if (size.equalsIgnoreCase("large")) {
      x = BookScreen.PAGE_WIDTH / 2 - IMG_CRAFTING_LARGE.width / 2;
      height = y + IMG_CRAFTING_LARGE.height;
      list.add(new ImageElement(x, y, IMG_CRAFTING_LARGE.width, IMG_CRAFTING_LARGE.height, IMG_CRAFTING_LARGE, book.appearance.slotColor));
      resultX = x + X_RESULT_LARGE;
      resultY = y + Y_RESULT_LARGE;
    }

    if (this.grid != null) {
      for (int i = 0; i < this.grid.length; i++) {
        for (int j = 0; j < this.grid[i].length; j++) {
          if (this.grid[i][j] == null || this.grid[i][j].getItems().isEmpty()) {
            continue;
          }
          list.add(new ItemElement(x + SLOT_MARGIN + (SLOT_PADDING + Math.round(ItemElement.ITEM_SIZE_HARDCODED * ITEM_SCALE)) * j, y + SLOT_MARGIN + (SLOT_PADDING + Math.round(ItemElement.ITEM_SIZE_HARDCODED * ITEM_SCALE)) * i, ITEM_SCALE, this.grid[i][j].getItems(), this.grid[i][j].action));
        }
      }
    }

    if (this.result != null) {
      list.add(new ItemElement(resultX, resultY, ITEM_SCALE, this.result.getItems(), this.result.action));
    }

    if (this.description != null && this.description.length > 0) {
      list.add(new TextElement(0, height + 5, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - height - 5, this.description));
    }
  }

  @Override
  public void load() {
    super.load();

    if (!StringUtils.isEmpty(recipe) && ResourceLocation.isValidResourceLocation(recipe)) {
      int w = 0, h = 0;

      assert Minecraft.getInstance().level != null;
      Recipe<?> recipe = Minecraft.getInstance().level.getRecipeManager().byKey(new ResourceLocation(this.recipe)).orElse(null);
      if (recipe instanceof CraftingRecipe) {
        if(grid_size.equalsIgnoreCase("auto")) {
          if(recipe.canCraftInDimensions(2, 2)) {
            grid_size = "small";
          } else {
            grid_size = "large";
          }
        }

        switch (grid_size.toLowerCase()) {
          case "large" -> w = h = 3;
          case "small" -> w = h = 2;
        }

        if (!recipe.canCraftInDimensions(w, h)) {
          throw new BookLoadException("Recipe " + this.recipe + " cannot fit in a " + w + "x" + h + " crafting grid");
        }

        result = IngredientData.getItemStackData(recipe.getResultItem());

        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        if (recipe instanceof IShapedRecipe<?> shaped) {
          grid = new IngredientData[shaped.getRecipeHeight()][shaped.getRecipeWidth()];

          for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
              grid[y][x] = IngredientData.getItemStackData(NonNullList.of(ItemStack.EMPTY, ingredients.get(x + y * grid[y].length).getItems()));
            }
          }

          return;
        }

        grid = new IngredientData[h][w];
        for (int i = 0; i < ingredients.size(); i++) {
          grid[i / h][i % w] = IngredientData.getItemStackData(NonNullList.of(ItemStack.EMPTY, ingredients.get(i).getItems()));
        }
      }
    }
  }
}
