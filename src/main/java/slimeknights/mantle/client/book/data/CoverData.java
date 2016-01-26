package slimeknights.mantle.client.book.data;

/**
 * @author fuj1n
 */
public class CoverData implements IDataItem {

  public int color = 0x8B4631;
  public String title = "";
  public String subtitle = "";

  @Override
  public int cascadeLoad() {
    return 0;
  }
}
