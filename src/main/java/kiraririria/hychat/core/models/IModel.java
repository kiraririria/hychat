package kiraririria.hychat.core.models;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.function.Consumer;

public interface IModel
{
    void generateTextStream(JsonObject request, Consumer<String> onChunk, boolean useStreaming) throws IOException;
}
