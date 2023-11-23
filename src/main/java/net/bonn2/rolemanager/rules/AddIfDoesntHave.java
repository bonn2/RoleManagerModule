package net.bonn2.rolemanager.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.bonn2.rolemanager.listeners.RuleEnforcementListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddIfDoesntHave implements Rule {
    Guild guild;
    List<Role> group1;
    List<Role> group2;

    public AddIfDoesntHave(Guild guild, List<Role> group1, List<Role> group2) {
        this.guild = guild;
        this.group1 = group1;
        this.group2 = group2;
    }

    @Override
    public void evaluate(@NotNull Member member) {
        for (Role role2 : group2) {
            if (member.getRoles().contains(role2)) {
                return;
            }
        }
        for (Role role1 : group1) {
            RuleEnforcementListener.modifiedMembers.add(member);
            member.getGuild().addRoleToMember(member, role1).complete();
            RuleEnforcementListener.modifiedMembers.remove(member);
        }
    }

    @Override
    public JsonObject serialize() {
        JsonObject serialized = new JsonObject();
        serialized.add("type", new JsonPrimitive("add-if-doesnt-have"));
        serialized.add("guild", new JsonPrimitive(guild.getId()));
        JsonArray jsonGroup1 = new JsonArray(group1.size());
        group1.forEach(role -> jsonGroup1.add(new JsonPrimitive(role.getId())));
        serialized.add("group1", jsonGroup1);
        JsonArray jsonGroup2 = new JsonArray(group2.size());
        group2.forEach(role -> jsonGroup2.add(new JsonPrimitive(role.getId())));
        serialized.add("group2", jsonGroup2);
        return serialized;
    }
}
