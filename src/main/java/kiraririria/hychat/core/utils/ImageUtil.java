package kiraririria.hychat.core.utils;

import kiraririria.hychat.common.HyChatFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil
{
    public static InputStream getResourceImageInputStream(String imageName)
    {
        InputStream stream = null;
        try
        {
            File f = new File(HyChatFiles.getCardsFolder().toFile(), imageName);
            if (f.exists())
            {
                stream = new FileInputStream(f);
            }
        }
        catch (IOException e)
        {
        }
        return stream;
    }
}
