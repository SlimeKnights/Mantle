package mantle.books;

import static mantle.lib.CoreRepo.logger;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mantle.Mantle;

import org.w3c.dom.Document;

public class ManualReader
{
    public static Document readManual(String location)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        try
        {
            logger.info("Loading Manual XML from: " + location);
            InputStream stream = Mantle.class.getResourceAsStream(location);
            return readManual(stream, location);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Document readManual(InputStream is, String filenameOrLocation)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        try
        {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            return doc;
        }
        catch (Exception e)
        {
            logger.error("Failed to Load Manual XML from: " + filenameOrLocation);
            e.printStackTrace();
            return null;
        }
    }
}
