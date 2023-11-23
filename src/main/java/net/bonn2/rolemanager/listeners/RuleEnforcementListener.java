package net.bonn2.rolemanager.listeners;

import net.bonn2.rolemanager.rules.GuildRules;
import net.bonn2.rolemanager.rules.Rule;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RuleEnforcementListener extends ListenerAdapter {

    public static List<Member> modifiedMembers = new ArrayList<>();

    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        for (Rule rule : GuildRules.getRules(event.getGuild())) {
            rule.evaluate(event.getMember());
        }
    }

    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        for (Rule rule : GuildRules.getRules(event.getGuild())) {
            rule.evaluate(event.getMember());
        }
    }
}
