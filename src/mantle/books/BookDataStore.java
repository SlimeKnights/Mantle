package mantle.books;

import com.google.common.collect.HashBiMap;

public class BookDataStore
{
    private static HashBiMap<String, BookData> data;
    private static HashBiMap<Integer, String> intMap;

    public void addBook (BookData bd)
    {
        int insertOrder = data.size();
        data.put(bd.getFullUnlocalizedName(), bd);
        intMap.put(insertOrder, bd.getFullUnlocalizedName());
    }

    public static BookData getBookfromName (String ModID, String unlocalizedName)
    {
        return getBookFromName(ModID + ":" + unlocalizedName);
    }

    public static BookData getBookFromName (String fullBookName)
    {
        return data.get(fullBookName);
    }

    public static BookData getBookfromID (int bookIDNum)
    {
        return data.get(intMap.get(bookIDNum));
    }

    public static int getIDFromName (String FullUnlocalizedName)
    {
        return intMap.inverse().get(FullUnlocalizedName);
    }

    public static int getIDFromBook (BookData b)
    {
        return getIDFromName(data.inverse().get(b));
    }
}
