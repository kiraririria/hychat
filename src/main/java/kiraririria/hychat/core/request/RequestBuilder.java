package kiraririria.hychat.core.request;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.core.data.GenerationSettings;

import java.util.List;

public class RequestBuilder
{
    public static JsonObject build(List<PromptMessage> messages, boolean stream)
    {
        JsonObject object = new JsonObject();

        if (HyChatPlugin.getInstance().getConfig().get().isOnlineMode())
        {
            addOpenRouterProperties(object, new GenerationSettings());
            addStreaming(object, stream);
            addMessages(object, messages);
        }
        else
        {
            addKoboldProperties(object, new GenerationSettings());
            addStreaming(object, stream);
            addPromptMessages(object, messages);
        }
        return object;
    }

    public static void addStreaming(JsonObject request, boolean streaming)
    {
        request.addProperty("stream", streaming);
    }
    public static void addOpenRouterProperties(JsonObject request, GenerationSettings settings)
    {
        request.addProperty("route", "fallback");

        request.addProperty("max_tokens", settings.max_tokens);
        request.addProperty("temperature", settings.temp);
        request.addProperty("top_p", settings.top_p);
        request.addProperty("top_a", settings.top_a);
        request.addProperty("frequency_penalty", settings.freq_pen);
        request.addProperty("presence_penalty", settings.presence_pen);
        request.addProperty("repetition_penalty", settings.rep_pen);
        request.addProperty("min_p", settings.min_p);
        JsonArray stopSequences = new JsonArray();
        for (String stop : settings.stopping_strings)
        {
            stopSequences.add(stop);
        }
        request.add("stop", stopSequences);

        request.addProperty("model", HyChatPlugin.getInstance().getConfig().get().getOpenrouterModel());
    }

    public static void addPromptMessages(JsonObject request, List<PromptMessage> messages)
    {
        StringBuilder prompt = new StringBuilder();
        for (PromptMessage message : messages)
        {
            if(message.role == PromptMessage.Role.SYSTEM)
            {
                prompt.append("\u003cstart_of_turn\u003esystem ").append(message.content).append("\u003cend_of_turn\u003e\n");
            }
            else if(message.role == PromptMessage.Role.ASSISTANT)
            {
                prompt.append("\u003cstart_of_turn\u003emodel ").append(message.content).append("\u003cend_of_turn\u003e\n");
            }
            else if (message.role == PromptMessage.Role.USER)
            {
                prompt.append("\u003cstart_of_turn\u003euser ").append(message.content).append("\u003cend_of_turn\u003e\n");
            }
        }
        request.addProperty("prompt", prompt.toString());
    }

    public static void addKoboldProperties(JsonObject request, GenerationSettings settings)
    {
        request.addProperty("max_new_tokens", settings.genamt);
        request.addProperty("max_tokens", settings.genamt);
        request.addProperty("temperature", settings.temp);
        request.addProperty("top_p", settings.top_p);
        request.addProperty("typical_p", settings.typical_p);
        request.addProperty("typical", 1);
        request.addProperty("min_p", settings.min_p);
        request.addProperty("repetition_penalty", settings.rep_pen);
        request.addProperty("frequency_penalty", settings.freq_pen);
        request.addProperty("presence_penalty", settings.presence_pen);
        request.addProperty("top_k", settings.top_k);
        request.addProperty("skew", settings.skew);
        request.addProperty("min_tokens", 0);
        request.addProperty("add_bos_token", settings.add_bos_token);
        request.addProperty("smoothing_factor", settings.smoothing_factor);
        request.addProperty("smoothing_curve", settings.smoothing_curve);
        request.addProperty("dry_allowed_length", settings.dry_allowed_length);
        request.addProperty("dry_multiplier", settings.dry_multiplier);
        request.addProperty("dry_base", settings.dry_base);
        request.addProperty("dry_sequence_breakers", settings.dry_sequence_breakers);
        request.addProperty("dry_penalty_last_n", settings.dry_penalty_last_n);
        request.addProperty("max_tokens_second", 0);

        JsonArray stoppingStrings = new JsonArray();
        for (String stop : settings.stopping_strings)
        {
            stoppingStrings.add(stop);
        }

        request.add("stopping_strings", stoppingStrings);
        request.add("stop", stoppingStrings);

        request.addProperty("truncation_length", settings.max_length);
        request.addProperty("ban_eos_token", false);
        request.addProperty("skip_special_tokens", settings.skip_special_tokens);
        request.addProperty("include_reasoning", true);
        request.addProperty("top_a", settings.top_a);
        request.addProperty("tfs", settings.tfs);
        request.addProperty("mirostat_mode", settings.mirostat_mode);
        request.addProperty("mirostat_tau", settings.mirostat_tau);
        request.addProperty("mirostat_eta", settings.mirostat_eta);
        request.addProperty("custom_token_bans", settings.banned_tokens);

        JsonArray bannedStrings = new JsonArray();
        request.add("banned_strings", bannedStrings);

        JsonArray samplerOrder = new JsonArray();
        for (int num : settings.sampler_order)
        {
            samplerOrder.add(num);
        }
        request.add("sampler_order", samplerOrder);

        request.addProperty("xtc_threshold", settings.xtc_threshold);
        request.addProperty("xtc_probability", settings.xtc_probability);
        request.addProperty("nsigma", settings.nsigma);
        request.addProperty("min_keep", settings.min_keep);
        request.addProperty("grammar", settings.grammar_string);
        request.addProperty("trim_stop", true);
        request.addProperty("rep_pen", settings.rep_pen);
        request.addProperty("rep_pen_range", settings.rep_pen_range);
        request.addProperty("repetition_penalty_range", settings.rep_pen_range);
        request.addProperty("guidance_scale", settings.guidance_scale);
        request.addProperty("negative_prompt", settings.negative_prompt);
        request.addProperty("grammar_string", settings.grammar_string);
        request.addProperty("repeat_penalty", settings.rep_pen);
        request.addProperty("repeat_last_n", 0);
        request.addProperty("n_predict", settings.genamt);
        request.addProperty("num_predict", settings.genamt);
        request.addProperty("num_ctx", settings.max_length);
        request.addProperty("mirostat", settings.mirostat_mode);
        request.addProperty("ignore_eos", settings.ignore_eos_token);
        request.addProperty("rep_pen_slope", settings.rep_pen_slope);
    }

    public static void addMessages(JsonObject request, List<PromptMessage> messages)
    {
        JsonArray jsonElements = new JsonArray();
        for (PromptMessage message : messages)
        {
            jsonElements.add(message.to());
        }
        request.add("messages", jsonElements);
    }

}
