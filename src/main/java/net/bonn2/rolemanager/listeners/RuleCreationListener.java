package net.bonn2.rolemanager.listeners;

import net.bonn2.rolemanager.rules.GuildRules;
import net.bonn2.rolemanager.rules.RemoveIfHas;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleCreationListener extends ListenerAdapter {

    private static class RuleCreationSequence {

        public enum Stage {
            GROUP_1, GROUP_2, CONFIRMATION, ERROR
        }

        public final String type;
        public final TextChannel channel;

        public RuleCreationSequence(String type, TextChannel channel) {
            this.type = type;
            this.channel = channel;
        }

        public List<Role> group1 = new ArrayList<>();
        public List<Role> group2 = new ArrayList<>();

        public Stage getStage() {
            // Neither group is set
            if (group1.isEmpty() && group2.isEmpty()) return Stage.GROUP_1;
            // Only group1 is set
            if (!group1.isEmpty() && group2.isEmpty()) return Stage.GROUP_2;
            // Both groups are set
            if (!group1.isEmpty()) return Stage.CONFIRMATION;
            // Invalid state
            return Stage.ERROR;
        }
    }

    Map<Member, RuleCreationSequence> activeStates = new HashMap<>();

    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("rolerule")) return;
        event.replyChoice(
                "remove-if-has",
                "remove-if-has"
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rolerule")) return;
        if (!event.getSubcommandName().equals("add")) return;

        switch (Objects.requireNonNull(event.getOption("type")).getAsString()) {
            case "remove-if-has" -> {
                activeStates.put(event.getMember(), new RuleCreationSequence("remove-if-has", event.getChannel().asTextChannel()));
                event.reply("Please send a message pinging every role you want to remove when a user has a different role.").queue();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Make sure member and channel are correct
        if (!activeStates.containsKey(event.getMember())) return;
        RuleCreationSequence state = activeStates.get(event.getMember());
        if (!state.channel.equals(event.getChannel())) return;

        switch (state.type) {
            case "remove-if-has" -> {
                switch (state.getStage()) {
                    case GROUP_1 -> {
                        List<Role> roles = getRolesFromMessage(event.getMessage());
                        if (roles.isEmpty()) {
                            state.channel.sendMessage("Please ping the roles you wish to use").queue();
                            return;
                        }
                        state.group1 = roles;
                        activeStates.put(event.getMember(), state);
                        state.channel.sendMessage("Please send a message pinging every role you want to check for in order to remove the first role. (Exclusive)").queue();
                    }
                    case GROUP_2 -> {
                        List<Role> roles = getRolesFromMessage(event.getMessage());
                        if (roles.isEmpty()) {
                            state.channel.sendMessage("Please ping the roles you wish to use").queue();
                            return;
                        }
                        state.group2 = roles;
                        GuildRules.addRule(event.getGuild(), new RemoveIfHas(event.getGuild(), state.group1, state.group2));
                        state.channel.sendMessage("Done").complete();
                        activeStates.remove(event.getMember());
                    }
                }
            }
        }
    }

    /**
     * Get all roles from a message
     * @param message The message containing mentions of the roles
     * @return A list containing all roles mentioned in the message
     */
    @NotNull
    private List<Role> getRolesFromMessage(Message message) {
        if (message == null) return new ArrayList<>();

        Pattern pattern = Pattern.compile("<@&[0-9]+>");
        Matcher matcher = pattern.matcher(message.getContentRaw());

        List<Role> roles = new ArrayList<>();
        while (matcher.find()) {
            roles.add(message.getGuild().getRoleById(matcher.group().replaceAll("<@&", "").replaceAll(">", "")));
        }

        return roles;
    }
}
