package kiraririria.hychat.core;

import com.google.gson.JsonObject;
import kiraririria.hychat.api.HyChatAPI;
import kiraririria.hychat.core.models.IModel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GenerationTask
{
    private final JsonObject jsonObject;
    private final Consumer<String> onUpdate;
    private final Runnable onComplete;
    private Thread generationThread;
    private final AtomicReference<String> currentText = new AtomicReference<>("");
    private final boolean stream;

    public GenerationTask(JsonObject jsonObject, Consumer<String> onUpdate, Runnable onComplete, boolean stream)
    {
        this.onUpdate = onUpdate;
        this.onComplete = onComplete;
        this.jsonObject = jsonObject;
        this.stream = stream;
    }

    public void start()
    {
        generationThread = new Thread(() ->
        {
            IModel model = HyChatAPI.getModel();
            final boolean[] wasReasoning = {false};
            assert model != null;
            try
            {
                model.generateTextStream(jsonObject, chunk ->
                {
                    if (chunk.startsWith("[R]"))
                    {
                        currentText.set("[R]" + currentText.get() + chunk.replace("[R]", ""));
                        onUpdate.accept(currentText.get());
                        currentText.set(currentText.get().replace("[R]", ""));
                        wasReasoning[0] = true;
                    }
                    else
                    {
                        if (wasReasoning[0])
                        {
                            currentText.set("");
                            wasReasoning[0] = false;
                        }
                        currentText.set(currentText.get() + chunk);
                        onUpdate.accept(currentText.get());
                    }
                },stream);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            String finalText = currentText.get();
            currentText.set(finalText);
            onUpdate.accept(finalText);

            onComplete.run();
        });
        generationThread.start();
    }


    public void stop()
    {
        if (generationThread != null && generationThread.isAlive())
        {
            generationThread.interrupt();
        }
    }
}

