package kiraririria.hychat.core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kiraririria.hychat.common.HyChatFiles;
import kiraririria.hychat.core.utils.ImageUtil;
import kiraririria.hychat.core.utils.png.PngMetadataHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterCard
{
    public String spec = "chara_card_v2";
    public String specVersion = "2.0";
    public String name = "";
    public String description = "";
    public String personality = "";
    public String scenario = "";
    public String firstMes = "Hi, how are you?";
    public String mesExample = "";
    public String creatorNotes = "";
    public String systemPrompt = "";
    public String postHistoryInstructions = "";
    public List<String> alternateGreetings = new ArrayList<>();
    public List<String> tags = new ArrayList<>();
    public String creator = "";
    public String characterVersion = "";

    private CharacterCard()
    {

    }

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public void copy(CharacterCard other)
    {
        this.spec = other.spec;
        this.specVersion = other.specVersion;
        this.name = other.name;
        this.description = other.description;
        this.personality = other.personality;
        this.scenario = other.scenario;
        this.firstMes = other.firstMes;
        this.mesExample = other.mesExample;
        this.creatorNotes = other.creatorNotes;
        this.systemPrompt = other.systemPrompt;
        this.postHistoryInstructions = other.postHistoryInstructions;
        this.alternateGreetings = other.alternateGreetings;
        this.tags = other.tags;
        this.creator = other.creator;
        this.characterVersion = other.characterVersion;
    }

    public static String clearWhaterMark(String string)
    {
        return string.split("\n")[0];
    }

    public static CharacterCard empty()
    {
        return new CharacterCard();
    }

    public String toJson()
    {
        JsonObject root = new JsonObject();
        root.addProperty("spec", this.spec);
        root.addProperty("spec_version", this.specVersion);

        JsonObject data = new JsonObject();
        data.addProperty("name", this.name);
        data.addProperty("description", this.description);
        data.addProperty("personality", this.personality);
        data.addProperty("scenario", this.scenario);
        data.addProperty("first_mes", this.firstMes);
        data.addProperty("mes_example", this.mesExample);
        data.addProperty("creator_notes", this.creatorNotes);
        data.addProperty("system_prompt", this.systemPrompt);
        data.addProperty("post_history_instructions", this.postHistoryInstructions);

        JsonArray greetingsArray = new JsonArray();
        this.alternateGreetings.forEach(greetingsArray::add);
        data.add("alternate_greetings", greetingsArray);

        JsonArray tagsArray = new JsonArray();
        this.tags.forEach(tagsArray::add);
        data.add("tags", tagsArray);

        data.addProperty("creator", this.creator);
        data.addProperty("character_version", this.characterVersion);

        root.add("data", data);

        return GSON.toJson(root);
    }

    public void fromJson(String json)
    {
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        JsonObject data = root.getAsJsonObject("data");
        this.spec = getStringSafe(root, "spec", "chara_card_v2");
        this.specVersion = getStringSafe(root, "spec_version", "2.0");
        this.name = getStringSafe(data, "name");
        this.description = getStringSafe(data, "description");
        this.personality = getStringSafe(data, "personality");
        this.scenario = getStringSafe(data, "scenario");
        this.firstMes = getStringSafe(data, "first_mes");
        this.mesExample = getStringSafe(data, "mes_example");
        this.creatorNotes = getStringSafe(data, "creator_notes");
        this.systemPrompt = getStringSafe(data, "system_prompt", "");
        this.postHistoryInstructions = getStringSafe(data, "post_history_instructions", "");
        this.creator = getStringSafe(data, "creator", "");
        this.characterVersion = getStringSafe(data, "character_version", "");
        this.alternateGreetings = getStringListSafe(data, "alternate_greetings");
        this.tags = getStringListSafe(data, "tags");
    }

    private String getStringSafe(JsonObject obj, String key)
    {
        return getStringSafe(obj, key, "");
    }

    private String getStringSafe(JsonObject obj, String key, String defaultValue)
    {
        if (obj.has(key) && !obj.get(key).isJsonNull())
        {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    private List<String> getStringListSafe(JsonObject obj, String key)
    {
        List<String> result = new ArrayList<>();
        if (obj.has(key) && obj.get(key).isJsonArray())
        {
            JsonArray array = obj.getAsJsonArray(key);
            array.forEach(e -> result.add(e.getAsString()));
        }
        return result;
    }

    @Override
    public String toString()
    {
        return toJson();
    }

    public boolean hasTag(String tag)
    {
        return tags.stream().anyMatch(t -> t.equalsIgnoreCase(tag));
    }

    public static CharacterCard fromPng(String cardName)
    {
        CharacterCard characterCard = new CharacterCard();
        try
        {
            String jsonData = PngMetadataHandler.readPngTextChunk(ImageUtil.getResourceImageInputStream(cardName + ".png"));
            if (jsonData == null)
            {
                System.out.println("[Card] Load empty char " + cardName + ".png");
                return characterCard;
            }
            characterCard.fromJson(jsonData);
            characterCard.creatorNotes = clearWhaterMark(characterCard.creatorNotes);
            return characterCard;
        }
        catch (IOException e)
        {
            return characterCard;
        }
    }

    public static void savePng(CharacterCard characterCard, String cardName)
    {
        try
        {
            byte[] newImage = PngMetadataHandler.writePngMetadata(ImageUtil.getResourceImageInputStream(cardName + ".png"), characterCard.toJson());
            try (FileOutputStream fos = new FileOutputStream(new File(HyChatFiles.getCardsFolder().toFile(), cardName + ".png")))
            {
                fos.write(newImage);
                fos.flush();
            }
        }
        catch (IOException e)
        {
        }
    }
}
