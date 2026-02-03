package kiraririria.hychat.core.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenerationSettings
{
    public double temp = 0.7;
    public boolean temperature_last = true;
    public double top_p = 0.5;
    public int top_k = 40;
    public double top_a = 0;
    public double tfs = 1;
    public double epsilon_cutoff = 0;
    public double eta_cutoff = 0;
    public double typical_p = 1;
    public double min_p = 0;
    public double rep_pen = 1.2;
    public int rep_pen_range = 0;
    public double rep_pen_decay = 0;
    public double rep_pen_slope = 1;
    public int no_repeat_ngram_size = 0;
    public double penalty_alpha = 0;
    public int num_beams = 1;
    public double length_penalty = 1;
    public int min_length = 0;
    public double encoder_rep_pen = 1;
    public double freq_pen = 0;
    public double presence_pen = 0;
    public double skew = 0;
    public boolean do_sample = true;
    public boolean early_stopping = false;
    public boolean dynatemp = false;
    public double min_temp = 0;
    public double max_temp = 2;
    public double dynatemp_exponent = 1;
    public double smoothing_factor = 0;
    public double smoothing_curve = 1;
    public int dry_allowed_length = 2;
    public double dry_multiplier = 0;
    public double dry_base = 1.75;
    public String dry_sequence_breakers = "[\"\\n\",\":\",\"\\\"\",\"*\"]";
    public int dry_penalty_last_n = 0;
    public boolean add_bos_token = true;
    public boolean ban_eos_token = false;
    public boolean skip_special_tokens = true;
    public int mirostat_mode = 0;
    public double mirostat_tau = 5;
    public double mirostat_eta = 0.1;
    public double guidance_scale = 1;
    public String negative_prompt = "";
    public String grammar_string = "";
    public String json_schema = "{}";
    public String banned_tokens = "";
    public List<String> sampler_priority = new ArrayList<>();
    public List<String> samplers = new ArrayList<>();
    public List<String> samplers_priorities = new ArrayList<>();
    public boolean ignore_eos_token = false;
    public boolean spaces_between_special_tokens = true;
    public boolean speculative_ngram = false;
    public List<Integer> sampler_order = new ArrayList<>();
    public List<Object> logit_bias = new ArrayList<>();
    public double xtc_threshold = 0.1;
    public double xtc_probability = 0;
    public double nsigma = 0;
    public int min_keep = 0;
    public String extensions = "{}";
    public int rep_pen_size = 0;
    public int genamt = 350;
    public int max_length = 8192;
    public int max_tokens = 1000;


    public List<String> stopping_strings = new ArrayList<>();

    public GenerationSettings()
    {
        // Установка значений по умолчанию для списков
        sampler_priority.addAll(Arrays.asList(
                "repetition_penalty", "presence_penalty", "frequency_penalty", "dry",
                "temperature", "dynamic_temperature", "quadratic_sampling", "top_n_sigma",
                "top_k", "top_p", "typical_p", "epsilon_cutoff", "eta_cutoff", "tfs",
                "top_a", "min_p", "mirostat", "xtc", "encoder_repetition_penalty", "no_repeat_ngram"
        ));

        samplers.addAll(Arrays.asList(
                "penalties", "dry", "top_n_sigma", "top_k", "typ_p", "tfs_z",
                "typical_p", "xtc", "top_p", "min_p", "temperature"
        ));

        samplers_priorities.addAll(Arrays.asList(
                "dry", "penalties", "no_repeat_ngram", "temperature", "top_nsigma",
                "top_p_top_k", "top_a", "min_p", "tfs", "eta_cutoff", "epsilon_cutoff",
                "typical_p", "quadratic", "xtc"
        ));

        sampler_order.addAll(Arrays.asList(6, 0, 1, 3, 4, 2, 5));

        //stopping_strings.add("\nUser:");
    }


    public static GenerationSettings fromJson(JsonObject json)
    {
        GenerationSettings settings = new GenerationSettings();
        Gson gson = new Gson();

        for (Map.Entry<String, JsonElement> entry : json.entrySet())
        {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            try
            {
                switch (key)
                {
                    case "temp":
                        settings.temp = value.getAsDouble();
                        break;
                    case "temperature_last":
                        settings.temperature_last = value.getAsBoolean();
                        break;
                    case "top_p":
                        settings.top_p = value.getAsDouble();
                        break;
                    case "top_k":
                        settings.top_k = value.getAsInt();
                        break;
                    case "top_a":
                        settings.top_a = value.getAsDouble();
                        break;
                    case "tfs":
                        settings.tfs = value.getAsDouble();
                        break;
                    case "epsilon_cutoff":
                        settings.epsilon_cutoff = value.getAsDouble();
                        break;
                    case "eta_cutoff":
                        settings.eta_cutoff = value.getAsDouble();
                        break;
                    case "typical_p":
                        settings.typical_p = value.getAsDouble();
                        break;
                    case "min_p":
                        settings.min_p = value.getAsDouble();
                        break;
                    case "rep_pen":
                        settings.rep_pen = value.getAsDouble();
                        break;
                    case "rep_pen_range":
                        settings.rep_pen_range = value.getAsInt();
                        break;
                    case "rep_pen_decay":
                        settings.rep_pen_decay = value.getAsDouble();
                        break;
                    case "rep_pen_slope":
                        settings.rep_pen_slope = value.getAsDouble();
                        break;
                    case "no_repeat_ngram_size":
                        settings.no_repeat_ngram_size = value.getAsInt();
                        break;
                    case "penalty_alpha":
                        settings.penalty_alpha = value.getAsDouble();
                        break;
                    case "num_beams":
                        settings.num_beams = value.getAsInt();
                        break;
                    case "length_penalty":
                        settings.length_penalty = value.getAsDouble();
                        break;
                    case "min_length":
                        settings.min_length = value.getAsInt();
                        break;
                    case "encoder_rep_pen":
                        settings.encoder_rep_pen = value.getAsDouble();
                        break;
                    case "freq_pen":
                        settings.freq_pen = value.getAsDouble();
                        break;
                    case "presence_pen":
                        settings.presence_pen = value.getAsDouble();
                        break;
                    case "skew":
                        settings.skew = value.getAsDouble();
                        break;
                    case "do_sample":
                        settings.do_sample = value.getAsBoolean();
                        break;
                    case "early_stopping":
                        settings.early_stopping = value.getAsBoolean();
                        break;
                    case "dynatemp":
                        settings.dynatemp = value.getAsBoolean();
                        break;
                    case "min_temp":
                        settings.min_temp = value.getAsDouble();
                        break;
                    case "max_temp":
                        settings.max_temp = value.getAsDouble();
                        break;
                    case "dynatemp_exponent":
                        settings.dynatemp_exponent = value.getAsDouble();
                        break;
                    case "smoothing_factor":
                        settings.smoothing_factor = value.getAsDouble();
                        break;
                    case "smoothing_curve":
                        settings.smoothing_curve = value.getAsDouble();
                        break;
                    case "dry_allowed_length":
                        settings.dry_allowed_length = value.getAsInt();
                        break;
                    case "dry_multiplier":
                        settings.dry_multiplier = value.getAsDouble();
                        break;
                    case "dry_base":
                        settings.dry_base = value.getAsDouble();
                        break;
                    case "dry_sequence_breakers":
                        settings.dry_sequence_breakers = value.getAsString();
                        break;
                    case "dry_penalty_last_n":
                        settings.dry_penalty_last_n = value.getAsInt();
                        break;
                    case "add_bos_token":
                        settings.add_bos_token = value.getAsBoolean();
                        break;
                    case "ban_eos_token":
                        settings.ban_eos_token = value.getAsBoolean();
                        break;
                    case "skip_special_tokens":
                        settings.skip_special_tokens = value.getAsBoolean();
                        break;
                    case "mirostat_mode":
                        settings.mirostat_mode = value.getAsInt();
                        break;
                    case "mirostat_tau":
                        settings.mirostat_tau = value.getAsDouble();
                        break;
                    case "mirostat_eta":
                        settings.mirostat_eta = value.getAsDouble();
                        break;
                    case "guidance_scale":
                        settings.guidance_scale = value.getAsDouble();
                        break;
                    case "negative_prompt":
                        settings.negative_prompt = value.getAsString();
                        break;
                    case "grammar_string":
                        settings.grammar_string = value.getAsString();
                        break;
                    case "json_schema":
                        settings.json_schema = value.getAsString();
                        break;
                    case "banned_tokens":
                        settings.banned_tokens = value.getAsString();
                        break;
                    case "sampler_priority":
                        Type listType = new TypeToken<List<String>>() {}.getType();
                        settings.sampler_priority = gson.fromJson(value, listType);
                        break;
                    case "samplers":
                        settings.samplers = gson.fromJson(value, new TypeToken<List<String>>() {}.getType());
                        break;
                    case "samplers_priorities":
                        settings.samplers_priorities = gson.fromJson(value, new TypeToken<List<String>>() {}.getType());
                        break;
                    case "ignore_eos_token":
                        settings.ignore_eos_token = value.getAsBoolean();
                        break;
                    case "spaces_between_special_tokens":
                        settings.spaces_between_special_tokens = value.getAsBoolean();
                        break;
                    case "speculative_ngram":
                        settings.speculative_ngram = value.getAsBoolean();
                        break;
                    case "sampler_order":
                        settings.sampler_order = gson.fromJson(value, new TypeToken<List<Integer>>() {}.getType());
                        break;
                    case "logit_bias":
                        settings.logit_bias = gson.fromJson(value, new TypeToken<List<Object>>() {}.getType());
                        break;
                    case "xtc_threshold":
                        settings.xtc_threshold = value.getAsDouble();
                        break;
                    case "xtc_probability":
                        settings.xtc_probability = value.getAsDouble();
                        break;
                    case "nsigma":
                        settings.nsigma = value.getAsDouble();
                        break;
                    case "min_keep":
                        settings.min_keep = value.getAsInt();
                        break;
                    case "extensions":
                        settings.extensions = value.getAsString();
                        break;
                    case "rep_pen_size":
                        settings.rep_pen_size = value.getAsInt();
                        break;
                    case "genamt":
                        settings.genamt = value.getAsInt();
                        break;
                    case "max_length":
                        settings.max_length = value.getAsInt();
                        break;
                    case "max_tokens":
                        settings.max_tokens = value.getAsInt();
                        break;
                    case "stopping_strings":
                        settings.stopping_strings = gson.fromJson(value, new TypeToken<List<String>>() {}.getType());
                        break;
                }
            }
            catch (Exception e)
            {
                System.err.println("Error loading setting: " + key + " - " + e.getMessage());
            }
        }

        return settings;
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        Gson gson = new Gson();

        json.addProperty("temp", temp);
        json.addProperty("temperature_last", temperature_last);
        json.addProperty("top_p", top_p);
        json.addProperty("top_k", top_k);
        json.addProperty("top_a", top_a);
        json.addProperty("tfs", tfs);
        json.addProperty("epsilon_cutoff", epsilon_cutoff);
        json.addProperty("eta_cutoff", eta_cutoff);
        json.addProperty("typical_p", typical_p);
        json.addProperty("min_p", min_p);
        json.addProperty("rep_pen", rep_pen);
        json.addProperty("rep_pen_range", rep_pen_range);
        json.addProperty("rep_pen_decay", rep_pen_decay);
        json.addProperty("rep_pen_slope", rep_pen_slope);
        json.addProperty("no_repeat_ngram_size", no_repeat_ngram_size);
        json.addProperty("penalty_alpha", penalty_alpha);
        json.addProperty("num_beams", num_beams);
        json.addProperty("length_penalty", length_penalty);
        json.addProperty("min_length", min_length);
        json.addProperty("encoder_rep_pen", encoder_rep_pen);
        json.addProperty("freq_pen", freq_pen);
        json.addProperty("presence_pen", presence_pen);
        json.addProperty("skew", skew);
        json.addProperty("do_sample", do_sample);
        json.addProperty("early_stopping", early_stopping);
        json.addProperty("dynatemp", dynatemp);
        json.addProperty("min_temp", min_temp);
        json.addProperty("max_temp", max_temp);
        json.addProperty("dynatemp_exponent", dynatemp_exponent);
        json.addProperty("smoothing_factor", smoothing_factor);
        json.addProperty("smoothing_curve", smoothing_curve);
        json.addProperty("dry_allowed_length", dry_allowed_length);
        json.addProperty("dry_multiplier", dry_multiplier);
        json.addProperty("dry_base", dry_base);
        json.addProperty("dry_sequence_breakers", dry_sequence_breakers);
        json.addProperty("dry_penalty_last_n", dry_penalty_last_n);
        json.addProperty("add_bos_token", add_bos_token);
        json.addProperty("ban_eos_token", ban_eos_token);
        json.addProperty("skip_special_tokens", skip_special_tokens);
        json.addProperty("mirostat_mode", mirostat_mode);
        json.addProperty("mirostat_tau", mirostat_tau);
        json.addProperty("mirostat_eta", mirostat_eta);
        json.addProperty("guidance_scale", guidance_scale);
        json.addProperty("negative_prompt", negative_prompt);
        json.addProperty("grammar_string", grammar_string);
        json.addProperty("json_schema", json_schema);
        json.addProperty("banned_tokens", banned_tokens);
        json.add("sampler_priority", gson.toJsonTree(sampler_priority));
        json.add("samplers", gson.toJsonTree(samplers));
        json.add("samplers_priorities", gson.toJsonTree(samplers_priorities));
        json.addProperty("ignore_eos_token", ignore_eos_token);
        json.addProperty("spaces_between_special_tokens", spaces_between_special_tokens);
        json.addProperty("speculative_ngram", speculative_ngram);
        json.add("sampler_order", gson.toJsonTree(sampler_order));
        json.add("logit_bias", gson.toJsonTree(logit_bias));
        json.addProperty("xtc_threshold", xtc_threshold);
        json.addProperty("xtc_probability", xtc_probability);
        json.addProperty("nsigma", nsigma);
        json.addProperty("min_keep", min_keep);
        json.addProperty("extensions", extensions);
        json.addProperty("rep_pen_size", rep_pen_size);
        json.addProperty("genamt", genamt);
        json.addProperty("max_length", max_length);
        json.addProperty("max_tokens", max_tokens);

        json.add("stopping_strings", gson.toJsonTree(stopping_strings));

        return json;
    }

    public static void saveToFile(GenerationSettings settings, File file)
    {
        try (FileWriter writer = new FileWriter(file))
        {
            writer.write(settings.toJson().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static GenerationSettings loadFromFile(File file)
    {
        if (!file.exists()) return new GenerationSettings();

        try (FileReader reader = new FileReader(file))
        {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            return fromJson(json);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new GenerationSettings();
        }
    }
}