package net.bonn2.rolemanager.rules;

import com.google.gson.*;
import net.bonn2.Bot;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GuildRules {

    protected static Map<Guild, List<Rule>> guildRulesMap = new HashMap<>();

    /**
     * Add a rule to a guild
     * @param guild The guild to add the rule to
     * @param rule  The rule to add
     */
    public static void addRule(Guild guild, Rule rule) {
        List<Rule> guildRules = getRules(guild);
        guildRules.add(rule);
        guildRulesMap.put(guild, guildRules);
        save(guild);
    }

    /**
     * Remove a rule from a guild
     * @param guild The guild to remove
     * @param id    The id of the rule to remove
     */
    public static void removeRule(Guild guild, int id) {
        List<Rule> guildRules = getRules(guild);
        guildRules.remove(id);
        guildRulesMap.put(guild, guildRules);
        save(guild);
    }

    /**
     * Get all the rules currently in a guild
     * @param guild The guild to get the rules for
     * @return      A list of Rules
     */
    @NotNull
    public static List<Rule> getRules(Guild guild) {
        return guildRulesMap.getOrDefault(guild, new ArrayList<>());
    }

    protected static final File rulesFolder = new File(Bot.localPath + File.separator + "rolemanager" + File.separator + "rules");

    /**
     * Save all rules for all guilds to disk
     */
    public static void saveAll() {
        if (guildRulesMap.isEmpty()) return;
        for (Guild guild : guildRulesMap.keySet()) {
            save(guild);
        }
    }

    /**
     * Save all rules for a given guild to disk
     * @param guild The guild to save
     */
    public static void save(@NotNull Guild guild) {
        try {
            rulesFolder.mkdirs();
            File rulesFile = new File(rulesFolder + File.separator + guild.getId() + ".json");
            rulesFile.createNewFile();

            JsonArray jsonArray = new JsonArray(guildRulesMap.get(guild).size());
            guildRulesMap.get(guild).forEach(rule -> jsonArray.add(rule.serialize()));

            try (FileOutputStream os = new FileOutputStream(rulesFile)) {
                os.write(
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(jsonArray)
                                .getBytes(StandardCharsets.UTF_8)
                );
            }
        } catch (IOException e) {
            Bot.logger.error("Failed to save guild rules for guild: " + guild.getId());
            Bot.logger.error(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Reset rules map and load rules for all guilds from file
     */
    public static void load() {
        guildRulesMap = new HashMap<>(Bot.jda.getGuilds().size());
        try {
            if (rulesFolder.mkdirs()) return;
            for (String filename : Objects.requireNonNull(rulesFolder.list())) {
                // Valid filenames will be in the format guildID.json
                if (!filename.toLowerCase().endsWith(".json")) continue;
                Guild guild = Bot.jda.getGuildById(filename.replace(".json", ""));
                if (guild == null) continue;
                File rulesFile = new File(rulesFolder + File.separator + filename);
                try (FileInputStream is = new FileInputStream(rulesFile)) {
                    JsonArray jsonArray = new Gson().fromJson(new String(is.readAllBytes(), StandardCharsets.UTF_8), JsonArray.class);
                    if (jsonArray == null) continue;
                    List<Rule> rules = new ArrayList<>(jsonArray.size());
                    jsonArray.forEach(jsonElement -> {
                        Rule rule = Rule.deserialize(jsonElement.getAsJsonObject());
                        if (rule != null) rules.add(rule);
                    });
                    if (!rules.isEmpty()) guildRulesMap.put(guild, rules);
                }
            }
        } catch (IOException e) {
            Bot.logger.error("Failed to load role rules from file!");
            Bot.logger.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
