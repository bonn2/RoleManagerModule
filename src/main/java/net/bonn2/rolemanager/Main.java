package net.bonn2.rolemanager;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import net.bonn2.rolemanager.listeners.MenuListener;
import net.bonn2.rolemanager.listeners.RuleCommandListener;
import net.bonn2.rolemanager.listeners.RuleCreationListener;
import net.bonn2.rolemanager.listeners.RuleEnforcementListener;
import net.bonn2.rolemanager.rules.GuildRules;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Main extends Module {

    @Override
    public void registerLoggingChannels() {}

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        Bot.jda.addEventListener(new MenuListener(this));
        Bot.jda.addEventListener(new RuleCreationListener());
        Bot.jda.addEventListener(new RuleCommandListener());
        Bot.jda.addEventListener(new RuleEnforcementListener());
        GuildRules.load();
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[] {
                Commands.slash(
                        "rolemenu",
                        "Root command for RoleManager."
                ).addSubcommands(
                        new SubcommandData(
                                "add",
                                "Add a role to a menu."
                        ).addOption(
                                OptionType.STRING,
                                "message_link",
                                "The link to the selector to edit.",
                                true
                        ).addOption(
                                OptionType.ROLE,
                                "role",
                                "The role to add.",
                                true
                        ),
                        new SubcommandData(
                                "remove",
                                "Remove a role from a menu."
                        ).addOption(
                                OptionType.STRING,
                                "message_link",
                                "The link to the selector to edit.",
                                true
                        ).addOption(
                                OptionType.ROLE,
                                "role",
                                "The role to remove.",
                                true
                        ),
                        new SubcommandData(
                                "new",
                                "Create a role selection menu."
                        ).addOption(
                                OptionType.STRING,
                                "title",
                                "The title of the embed.",
                                true
                        ).addOption(
                                OptionType.STRING,
                                "description",
                                "The description of the embed. You can use \\n for newlines",
                                true
                        ).addOption(
                                OptionType.INTEGER,
                                "min_selection",
                                "The minimum number of roles a user can select.",
                                true
                        ).addOption(
                                OptionType.INTEGER,
                                "max_selection",
                                "The maximum number of roles a user can select.",
                                true
                        ).addOption(
                                OptionType.STRING,
                                "placeholder",
                                "What the selection will show when nothing is selected.",
                                true
                        ).addOption(
                                OptionType.ROLE,
                                "role_1",
                                "The first role.",
                                true
                        ).addOption(
                                OptionType.ROLE,
                                "role_2",
                                "The second role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_3",
                                "The third role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_4",
                                "The fourth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_5",
                                "The fifth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_6",
                                "The sixth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_7",
                                "The seventh role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_8",
                                "The eighth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_9",
                                "The ninth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_10",
                                "The tenth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_11",
                                "The eleventh role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_12",
                                "The twelfth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_13",
                                "The thirteenth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_14",
                                "The fourteenth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_15",
                                "The fifteenth role",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_16",
                                "The sixteenth role",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_17",
                                "The seventeenth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_18",
                                "The eighteenth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_19",
                                "The nineteenth role.",
                                false
                        ).addOption(
                                OptionType.ROLE,
                                "role_20",
                                "The twentieth role.",
                                false
                        )
                ).setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash(
                        "rolerule",
                        "Set role rules."
                ).addSubcommands(
                        new SubcommandData(
                                "add",
                                "Add a role rule"
                        ).addOption(
                                OptionType.STRING,
                                "type",
                                "The type of role rule",
                                true,
                                true
                        ),
                        new SubcommandData(
                                "remove",
                                "Remove a role rule"
                        ).addOption(
                                OptionType.INTEGER,
                                "id",
                                "The id of the role rule",
                                true
                        ),
                        new SubcommandData(
                                "list",
                                "List all role rules on the server"
                        ),
                        new SubcommandData(
                                "force-apply",
                                "Applies all rules to all members of the server"
                        )
                ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        };
    }
}
