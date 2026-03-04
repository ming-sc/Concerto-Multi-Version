/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package top.gregtao.concerto.port.command.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.Event;

public class RegisterClientCommandsEvent extends Event
{

    private final CommandDispatcher<CommandSource> dispatcher;

    public RegisterClientCommandsEvent(CommandDispatcher<CommandSource> dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    /**
     * @return The command dispatcher for registering commands to be executed on the client
     */
    public CommandDispatcher<CommandSource> getDispatcher()
    {
        return dispatcher;
    }
}
