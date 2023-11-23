package net.bonn2.rolemanager.listeners;

import net.bonn2.rolemanager.rules.GuildRules;
import net.bonn2.rolemanager.rules.Rule;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RuleCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rolerule")) return;

        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "list" -> {
                StringBuilder message = new StringBuilder("**Rules:**\n");
                int count = 0;
                for (Rule rule : GuildRules.getRules(event.getGuild())) {
                    message.append(count).append(": ").append(rule.serialize().toString()).append("\n");
                }
                event.reply(message.toString()).queue();
            }
            case "remove" -> {
                if (GuildRules.removeRule(event.getGuild(), event.getOption("id").getAsInt())) {
                    event.reply("Success!").queue();
                } else {
                    event.reply("Failed!").queue();
                }
            }
        }
    }
}
