/*
 * This file is part of Influx.
 *
 * Influx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Influx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Influx.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.influx.command;

import com.dsh105.influx.*;
import com.dsh105.influx.annotation.*;
import com.dsh105.influx.dispatch.CommandContext;

import java.util.SortedMap;

@Nest(nests = {"influx", "i"})
@Authorize("influx.nest")
public class ExampleNest implements CommandListener {

    @Command(
            syntax = "help",
            desc = "Retrieve command help listing",
            aliases = {"h"},
            help = {
                    "- Commands are listed in alphabetical order",
                    "- Use \"!help <index>\" for a certain page of help",
                    "- Use \"!help <command>\" for more help on a certain command"
            }
    )
    @Nested
    // Because we are doing something directly with the sender, we should specify which type of sender is accepted
    // Even though the manager created already ensures this, it is still required to cast properly
    public boolean help(CommandContext<MockSender> context) {
        context.getManager().getHelp().sendPage(context.sender());
        return true;
    }

    @Command(
            syntax = "help <index>",
            desc = "Retrieve a certain page of command help listing",
            aliases = {"h <index>"},
            help = {
                    "- Retrieve a certain page of help",
                    "- Use \"!help <command>\" for more help on a certain command"
            }
    )
    @Nested
    public boolean help(CommandContext<MockSender> context, @Bind("index") @Verify("[0-9]+") int index) {
        context.getManager().getHelp().sendPage(context.sender(), index);
        return true;
    }

    @Command(
            syntax = "help <command...>",
            desc = "Retrieve help on a certain command",
            aliases = {"h <command>"},
            help = {
                    "- Use \"!help\" for general command help listing"
            }
    )
    @Nested
    // Again, this one interacts with the manager
    public boolean helpCommand(CommandContext<MockSender> context) {
        String command = context.var("command");
        SortedMap<Controller, String[]> matches = context.getManager().getHelp().getHelpFor(command);
        if (matches.isEmpty()) {
            context.respond("No help found for \"" + command + "\".");
            return true;
        }

        context.respond(matches.size() + " matches found for \"" + command + "\":");

        for (Controller controller : matches.keySet()) {
            System.out.println('\n');
            context.respondAnonymously("++ Help :: !" + controller.getCommand().getAcceptedStringSyntax() + " ++");
            context.getManager().getHelp().sendHelpFor(context.sender(), controller);
        }
        return true;
    }

    @Command(
            syntax = "voxel <voxel>",
            desc = "Locate a voxel position",
            aliases = {"v <voxel>"}
    )
    @Nested
    public boolean voxel(CommandContext context, @Bind("voxel") @Accept(value = 3, showAs = "<x> <y> <z>") @Convert(Voxel.Converter.class) Voxel voxel) {
        context.respond("Voxel position located: " + voxel);
        return true;
    }

    @Command(
            syntax = "voxel [voxel] <flag>",
            desc = "Locate a voxel position",
            aliases = {"v [voxel] <flag>"}
    )
    @Nested
    public boolean voxel(CommandContext context, @Bind("voxel") @Accept(value = 3, showAs = "<x> <y> <z>") @Default("3 2 1") @Convert(Voxel.Converter.class) Voxel voxel, @Bind("flag") boolean flag) {
        context.respond("Voxel position located: " + voxel + " with flag: " + flag);
        return true;
    }

    @Command(
            syntax = "<yay> [option]",
            desc = "A test command"
    )
    public boolean test(CommandContext context) {
        return true;
    }

    @Command(
            syntax = "create <pet>",
            desc = "A test command"
    )
    public boolean test1(CommandContext context) {
        return true;
    }

    @Command(
            syntax = "<type> [name] [data]",
            desc = "A test command"
    )
    @Priority(Priority.Type.LOWEST)
    public boolean test2(CommandContext context) {
        return true;
    }

    @Command(
            syntax = "[opt1] [opt2]",
            desc = "A test command"
    )
    @Priority(Priority.Type.HIGHEST)
    public boolean opt(CommandContext context, @Bind("opt1") @Default("yaaaay") String opt1, @Bind("opt2") @Default("wooooo") String opt2) {
        context.respond(context.var("opt1") + "  -  " + context.var("opt2"));
        return true;
    }

    @Command(
            syntax = "restricted",
            desc = "A command that only accepts the child sender"
    )
    @Nested
    public boolean restricted(CommandContext<MockChildSender> context) {
        System.out.println("Restricted command execution successful");
        return true;
    }
}