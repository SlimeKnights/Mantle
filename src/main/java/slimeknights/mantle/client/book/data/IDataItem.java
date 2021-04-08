package slimeknights.mantle.client.book.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IDataItem {

  void load();
}
