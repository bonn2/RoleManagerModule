package net.bonn2.rolemanager.listeners;

import net.bonn2.rolemanager.rules.GuildRules;
import net.bonn2.rolemanager.rules.Rule;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
            case "force-apply" -> {
                event.getGuild().loadMembers().onSuccess(members -> {
                    for (Member member : members) {
                        for (Rule rule : GuildRules.getRules(event.getGuild())) {
                            rule.evaluate(member);
                        }
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    event.getHook().editOriginal("~~" + event.getHook().retrieveOriginal().complete().getContentRaw() + "~~\nCompleted!").queue();
                });
                event.reply("Applying rules to members, this will take approximately " + event.getGuild().getMemberCount() + " seconds...").queue();
            }
        }
    }
}
