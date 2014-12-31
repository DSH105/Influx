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

package com.dsh105.influx;

import com.dsh105.influx.command.BukkitNest;
import com.dsh105.influx.command.ExampleNest;
import com.dsh105.influx.command.InvalidNest;
import com.dsh105.influx.dispatch.CommandInvocationException;
import com.dsh105.influx.help.ExpandedHelpProvider;
import com.dsh105.influx.help.HelpProvision;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.response.ResponseLevel;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandManagementTest {

    private static InfluxManager<MockSender> manager;
    protected static MockSender mockSender;
    protected static MockSender mockChildSender;
    protected static MockResponder mockResponder;

    private static InfluxBukkitManager bukkitManager;
    private static Plugin mockPlugin;
    protected static CommandSender mockCommandSender;

    public static InfluxManager<MockSender> getManager() {
        if (manager == null) {
            mockSender = new MockSender("InfluxTest");
            mockChildSender = new MockChildSender("InfluxTestChild");
            mockResponder = new MockResponder("[Influx]");

            manager = new CommandManager<>(null, "!", "Influx");
            manager.setResponseHandler(mockResponder);
            manager.setHelpProvision(HelpProvision.EXPANDED);
            ((ExpandedHelpProvider) manager.getHelp()).getPaginator().setPerPage(6);

            manager.register(new ExampleNest());
        }
        return manager;
    }

    public static InfluxBukkitManager getBukkitManager() {
        if (bukkitManager == null) {
            mockPlugin = mock(Plugin.class);
            when(mockPlugin.getName()).thenReturn("InfluxPlugin");
            bukkitManager = new BukkitCommandManager(mockPlugin);
            bukkitManager.setAuthorization(new BukkitAuthorization());
            bukkitManager.register(new BukkitNest());

            mockCommandSender = mock(CommandSender.class);
            when(mockCommandSender.getName()).thenReturn("InfluxCommandSender");
        }
        return bukkitManager;
    }

    @Test
    public void testResponses() {
        System.out.println("\n\n*** Testing responses ***");
        getManager().respond(mockSender, "Testing default sender...");
        getManager().respond(mockChildSender, "Testing child sender...");

        // Nope, compiler can do this
        /*try {
            getManager().respond(new MockUnsupportedSender(), "Testing unsupported sender type...");
            throw new IllegalStateException("Unsupported sender type responses are returning successful.");
        } catch (ResponseUnsupportedException e) {
            getManager().respond(mockSender, "Expected exception caught: (" + e.getClass().getSimpleName() + ") " + e.getMessage());
        }*/

        getManager().respond(mockSender, "Testing default response...", ResponseLevel.DEFAULT);
        getManager().respond(mockSender, "Testing warning response...", ResponseLevel.WARNING);
        getManager().respond(mockSender, "Testing severe response...", ResponseLevel.SEVERE);
    }

    @Test
    public void testSorting() {
        System.out.println("\n\n*** Sorting commands ***");
        for (Controller controller : getManager().getMappedCommands()) {
            System.out.println(controller.getCommand().getStringSyntax());
        }
    }

    @Test
    public void testCommands() {
        System.out.println("\n\n*** Registered commands: " + getManager().getMappedCommands().size() + " ***");
        System.out.println("*** Registered commands: ***");

        for (Controller controller : getManager().getMappedCommands()) {
            System.out.println("- " + controller.getCommand().getStringSyntax());
        }

        System.out.println("\n*** Testing CommandContext sender restriction... ***");
        String restrictedCommand = "influx restricted";
        getManager().getDispatcher().dispatch(mockSender, restrictedCommand);
        getManager().getDispatcher().dispatch(mockChildSender, restrictedCommand);

        String[][] commands = {
                {"influx help", "influx help 2", "influx help help", "influx help something 3"},
                {"influx voxel 1 2 3", "influx voxel 1 2 3 false", "influx voxel true"},
                {"influx", "influx hi", "influx hi hi"}
        };

        for (int i = 0; i < commands.length; i++) {
            System.out.println("\n*** Dispatching set #" + i + "... ***");
            for (String command : commands[i]) {
                System.out.println("*** Dispatching \"!" + command + "\" ***");
                getManager().getDispatcher().dispatch(mockSender, command);
            }
        }

        // Errors are expected for these commands
        String[][] commandsWithErrors = {
                // TODO: Fill these up
        };

        for (int i = 0; i < commandsWithErrors.length; i++) {
            System.out.println("\n*** Dispatching error set #" + i + "... ***");
            for (final String command : commandsWithErrors[i]) {
                System.out.println("*** Dispatching \"!" + command + "\" ***");
                expectError(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        getManager().getDispatcher().dispatch(mockSender, command);
                        return null;
                    }

                    @Override
                    public String toString() {
                        return command;
                    }
                });
            }
        }
    }

    @Test
    public void testRegistration() {
        System.out.println("\n\n*** Testing registration safeguards... ***");

        final InvalidNest invalidNest = new InvalidNest();
        for (final Method method : invalidNest.getClass().getDeclaredMethods()) {
            expectError(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    getManager().register(new CommandBuilder().from(invalidNest, method));
                    return null;
                }

                @Override
                public String toString() {
                    return method.getDeclaringClass().getCanonicalName() + "#" + method.getName();
                }
            });
        }
    }

    @Test
    public void testBukkitIntegration() {
        System.out.println("\n\n*** Registered commands: " + getManager().getMappedCommands().size() + " ***");
        System.out.println("*** Registered commands: ***");

        for (Controller controller : getManager().getMappedCommands()) {
            System.out.println("- " + controller.getCommand().getStringSyntax());
        }

        String[][] commands = {
                // TODO: Make some
        };

        for (int i = 0; i < commands.length; i++) {
            System.out.println("\n*** Dispatching set #" + i + "... ***");
            for (String command : commands[i]) {
                System.out.println("*** Dispatching \"!" + command + "\" ***");
                getBukkitManager().getDispatcher().dispatch(mockCommandSender, command);
            }
        }
    }

    private void expectError(Callable<?> callable) {
        try {
            callable.call();
            throw new IllegalStateException("Execution of callable \"" + callable + "\" returned successful, but an exception was expected.");
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }

            System.out.println("*** Expected exception caught for \"" + callable + "\": ***");
            Throwable thrown = e;
            while (thrown instanceof CommandInvocationException) {
                thrown = thrown.getCause();
            }
            System.out.println(thrown.getClass().getCanonicalName() + ": " + thrown.getMessage());
        }
    }
}