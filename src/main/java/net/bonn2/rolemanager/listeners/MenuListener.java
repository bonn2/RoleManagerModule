package net.bonn2.rolemanager.listeners;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import net.bonn2.modules.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuListener extends ListenerAdapter {

    private static final int max_selections = 25;
    private final Module module;

    public MenuListener(Module module) {
        this.module = module;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rolemenu")) return;

        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "new" -> {
                // Validate min/max
                if (event.getOption("min_selection").getAsLong() < 0) {
                    event.reply("Your minimum cannot be below 0!").setEphemeral(true).queue();
                    return;
                }
                if (event.getOption("max_selection").getAsLong() < event.getOption("min_selection").getAsLong()) {
                    event.reply("You maximum cannot be less than your minimum!").setEphemeral(true).queue();
                    return;
                }

                // Create embed
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(event.getOption("title").getAsString());
                embedBuilder.setDescription(event.getOption("description").getAsString().replaceAll("\\\\n", "\n"));
                embedBuilder.setColor(Color.CYAN);

                // Create selection menu
                StringSelectMenu.Builder selectionBuilder = StringSelectMenu.create("rolemenu")
                        .setMinValues((int) event.getOption("min_selection").getAsLong())
                        .setMaxValues(
                                event.getOption("max_selection").getAsLong() > 25 ?
                                        25 : (int) event.getOption("max_selection").getAsLong()
                        )
                        .setPlaceholder(event.getOption("placeholder").getAsString());

                // Add roles
                for (int i = 1; i <= max_selections; i++) {
                    OptionMapping option = event.getOption("role_" + i);
                    if (option == null) continue;
                    Role role = option.getAsRole();
                    selectionBuilder.addOption(role.getName(), "role_select_" + role.getId());
                }

                // Build final message
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
                messageBuilder.setEmbeds(embedBuilder.build());
                messageBuilder.setActionRow(selectionBuilder.build());

                // Send message
                event.getChannel().sendMessage(messageBuilder.build()).queue();

                // Confirm
                event.reply("Posted!").setEphemeral(true).queue();
            }
            case "add" -> {
                // Defer reply as we will need to query discord servers, which may take a while.
                event.deferReply(true).complete();

                // Get guild channel and message id from provided link
                Pattern pattern = Pattern.compile("[0-9]+/[0-9]+/[0-9]+");
                Matcher matcher = pattern.matcher(event.getOption("message_link").getAsString());
                if (!matcher.find()) {
                    event.getHook().editOriginal("That is not a valid message link!").queue();
                    return;
                }
                String match = matcher.group();
                String[] messageLocation = match.split("/");
                String guildID   = messageLocation[0];
                String channelID = messageLocation[1];
                String messageID = messageLocation[2];

                // Use ids to retrieve message
                Guild guild = Bot.jda.getGuildById(guildID);
                if (guild == null) {
                    event.getHook().editOriginal("Failed to get guild!").queue();
                    return;
                }
                MessageChannel messageChannel = guild.getChannelById(MessageChannel.class, channelID);
                if (messageChannel == null) {
                    event.getHook().editOriginal("Failed to get message channel!").queue();
                    return;
                }
                Message message = messageChannel.retrieveMessageById(messageID).complete();
                if (message == null) {
                    event.getHook().editOriginal("Failed to retrieve message!").queue();
                    return;
                }
                List<ActionRow> actionRows = message.getActionRows();
                if (actionRows.size() == 1) {

                }
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("rolemenu") && !event.getComponentId().equals("roleselect")) return;
        event.deferReply(true).queue();
        Member member = event.getMember();
        assert member != null;
        List<Role> addedRoles = new ArrayList<>();
        // Add selected roles
        for (String value : event.getValues()) {
            // Only add allowed roles
            String id = value.split("_")[2];
            // Get and add role
            Role role = event.getGuild().getRoleById(id);
            if (role == null) continue;
            addedRoles.add(role);
            event.getGuild().addRoleToMember(member, role).queue();
        }
        // Remove deselected roles
        for (SelectOption option : event.getComponent().getOptions()) {
            // Don't remove selected roles
            if (event.getValues().contains(option.getValue())) continue;
            // Only remove allowed roles
            String id = option.getValue().split("_")[2];
            // Get and remove role
            Role role = event.getGuild().getRoleById(id);
            if (role == null) continue;
            event.getGuild().removeRoleFromMember(member, role).queue();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Roles successfully updated!\n" +
                "Your new roles are: ");
        for (Role role : addedRoles)
            stringBuilder.append("<@&%s> ".formatted(role.getId()));

        event.getHook().editOriginal(stringBuilder.toString()).queue();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        List<Role> roles = Settings.get(module, event.getGuild().getId(), "default_roles").getAsRoleList(event.getGuild());
        for (Role role : roles) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
    }
}
