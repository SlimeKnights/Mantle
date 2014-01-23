package mantle.books;

import java.util.HashMap;

public class BookData
{
   public final String unlocalizedName = new String();
   public final String toolTip = new String();
   public final String modID = new String();
   public String getFullUnlocalizedName(){
       return this.modID + ":" + this.unlocalizedName;
   }

   
    

}
