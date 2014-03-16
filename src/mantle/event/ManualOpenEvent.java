package mantle.event;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class ManualOpenEvent extends Event {

	public ItemStack bookStack;
	public EntityPlayer player;

	public ManualOpenEvent(ItemStack book, EntityPlayer player) {
		this.bookStack = book;
		this.player = player;
	}

	public static class Pre extends ManualOpenEvent {
		public Pre(ItemStack book, EntityPlayer player) {
			super(book, player);
		}
	}

	public static class Post extends ManualOpenEvent {
		public Post(ItemStack book, EntityPlayer player) {
			super(book, player);
		}
	}
}
