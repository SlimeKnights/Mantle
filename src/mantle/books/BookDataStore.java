package mantle.books;

import java.util.List;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

public class BookDataStore
{
    private static HashBiMap<String, BookData> data = HashBiMap.create();

    private static HashBiMap<Integer, String> intMap = HashBiMap.create();

    private static List<String> zipBooks = Lists.newArrayList();

    public static void addBook(BookData bd)
    {
        int insertOrder = data.size();
        data.put(bd.getFullUnlocalizedName(), bd);
        intMap.put(insertOrder, bd.getFullUnlocalizedName());
        if (bd.isFromZip)
        {
            zipBooks.add(bd.getFullUnlocalizedName());
        }

    }

    public static BookData getBookfromName(String ModID, String unlocalizedName)
    {
        return getBookFromName(ModID + ":" + unlocalizedName);
    }

    public static BookData getBookFromName(String fullBookName)
    {
        return data.get(fullBookName);
    }

    public static BookData getBookfromID(int bookIDNum)
    {
        return data.get(intMap.get(bookIDNum));
    }

    public static int getIDFromName(String FullUnlocalizedName)
    {
        return intMap.inverse().get(FullUnlocalizedName);
    }

    public static int getIDFromBook(BookData b)
    {
        return getIDFromName(data.inverse().get(b));
    }

    public static List<String> getZipBooks()
    {
        return zipBooks;
    }
}
