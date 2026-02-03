package kiraririria.hychat.core.utils;

public class EmojiUtil
{
    public static String removeEmojis(String text)
    {
        if (text == null || text.isEmpty())
        {
            return text;
        }

        StringBuilder cleaned = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++)
        {
            char c = text.charAt(i);

            if (Character.isHighSurrogate(c))
            {
                if (i + 1 < length && Character.isLowSurrogate(text.charAt(i + 1)))
                {
                    i++;
                    continue;
                }
            }

            int codePoint = text.codePointAt(i);
            if (isEmoji(codePoint))
            {
                i += Character.charCount(codePoint) - 1;
                continue;
            }

            cleaned.append(c);
        }

        return cleaned.toString();
    }

    private static boolean isEmoji(int codePoint)
    {
        return (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons
                (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) || // Misc Symbols and Pictographs
                (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) || // Transport and Map
                (codePoint >= 0x2600 && codePoint <= 0x26FF) || // Misc symbols
                (codePoint >= 0x2700 && codePoint <= 0x27BF) || // Dingbats
                (codePoint >= 0xFE00 && codePoint <= 0xFE0F) || // Variation Selectors
                (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) || // Supplemental Symbols
                (codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF);   // Flags
    }

    public static String cleanup(String text)
    {
        String cleaned = removeEmojis(text);
        return cleaned != null ? cleaned.replaceAll("\\s{2,}", " ").trim() : "";
    }
}
