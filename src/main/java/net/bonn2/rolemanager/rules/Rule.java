package net.bonn2.rolemanager.rules;

import com.google.gson.JsonObject;
import net.bonn2.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface Rule {

    /**
     * Check if the provided member complies with the rule, and correct them if they don't.
     * All implementations should be non-blocking (use queue)
     * @param member The member to be evaluated and updated
     */
    void evaluate(Member member);

    /**
     * @return A JsonObject that represents the rule
     */
    JsonObject serialize();

    /**
     * Takes a serialized rule and return a full rule object, or null if serialized rule is malformed
     * @param jsonObject The serialized rule
     * @return Full rule object, or null
     */
    @Nullable
    static Rule deserialize(@NotNull JsonObject jsonObject) {
        switch (jsonObject.get("type").getAsString()) {
            case "remove-if-has" -> {
                // Get guild, return null on fail
                Guild guild = Bot.jda.getGuildById(jsonObject.get("guild").getAsString());
                if (guild == null) return null;
                // Get group1, return null if empty
                List<Role> group1 = new ArrayList<>(jsonObject.get("group1").getAsJsonArray().size());
                jsonObject.get("group1").getAsJsonArray().forEach(jsonElement -> {
                    Role role = guild.getRoleById(jsonElement.getAsString());
                    if (role != null) group1.add(role);
                });
                if (group1.isEmpty()) return null;
                // Get group2, return null if empty
                List<Role> group2 = new ArrayList<>(jsonObject.get("group2").getAsJsonArray().size());
                jsonObject.get("group2").getAsJsonArray().forEach(jsonElement -> {
                    Role role = guild.getRoleById(jsonElement.getAsString());
                    if (role != null) group2.add(role);
                });
                if (group2.isEmpty()) return null;
                // All vars should be valid at this point, return a new Rule object
                return new RemoveIfHas(guild, group1, group2);
            }
            case "add-if-doesnt-have" -> {
                // Get guild, return null on fail
                Guild guild = Bot.jda.getGuildById(jsonObject.get("guild").getAsString());
                if (guild == null) return null;
                // Get group1, return null if empty
                List<Role> group1 = new ArrayList<>(jsonObject.get("group1").getAsJsonArray().size());
                jsonObject.get("group1").getAsJsonArray().forEach(jsonElement -> {
                    Role role = guild.getRoleById(jsonElement.getAsString());
                    if (role != null) group1.add(role);
                });
                if (group1.isEmpty()) return null;
                // Get group2, return null if empty
                List<Role> group2 = new ArrayList<>(jsonObject.get("group2").getAsJsonArray().size());
                jsonObject.get("group2").getAsJsonArray().forEach(jsonElement -> {
                    Role role = guild.getRoleById(jsonElement.getAsString());
                    if (role != null) group2.add(role);
                });
                if (group2.isEmpty()) return null;
                // All vars should be valid at this point, return a new Rule object
                return new AddIfDoesntHave(guild, group1, group2);
            }
        }
        return null;
    }
}
