package mantle.books.external;

public class BookIS
{
    //users chosen nickname for this specific itemstack
    public String cname;

    //fully qualified name from the game registry for the item/block in question
    public String fullISName;

    public int metadata;

    public String tags;

    public int stackSize;

    public int getStackSize()
    {
        return this.stackSize >= 0 ? this.stackSize : 1;
    }

}
