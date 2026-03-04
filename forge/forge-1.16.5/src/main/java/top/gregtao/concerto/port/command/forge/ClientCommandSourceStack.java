/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package top.gregtao.concerto.port.command.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * overrides for {@link CommandSource} so that the methods will run successfully client side
 */
public class ClientCommandSourceStack extends CommandSource {

    public ClientCommandSourceStack(ICommandSource source, Vector3d position, Vector2f rotation, int permission, String plainTextName, ITextComponent displayName,
                                    Entity executing) {
        super(source, position, rotation, null, permission, plainTextName, displayName, null, executing);
    }

    /**
     * Sends a success message without attempting to get the server side list of admins
     */
    @Override
    public void sendSuccess(ITextComponent message, boolean sendToAdmins) {
        Minecraft.getInstance().player.sendMessage(message, Util.NIL_UUID);
    }

    /**
     * Gets the list of teams from the client side
     */
    @Override
    public Collection<String> getAllTeams() {
        return Minecraft.getInstance().level.getScoreboard().getTeamNames();
    }

    /**
     * Gets the list of online player names from the client side
     */
    @Override
    public Collection<String> getOnlinePlayerNames() {
        return Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map((player) -> player.getProfile().getName()).collect(Collectors.toList());
    }

    /**
     * Gets a {@link Stream} of recipe ids that are available on the client
     */
    @Override
    public Stream<ResourceLocation> getRecipeNames() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getRecipeIds();
    }

    /**
     * Gets a set of {@link RegistryKey} for levels from the client side
     */
    @Override
    public Set<RegistryKey<World>> levels() {
        return Minecraft.getInstance().getConnection().levels();
    }

    /**
     * Gets the {@link DynamicRegistries} from the client side
     */
    @Override
    public DynamicRegistries registryAccess() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }

    /**
     * @throws UnsupportedOperationException because the server isn't available on the client
     */
    @Override
    public MinecraftServer getServer() {
        throw new UnsupportedOperationException("Attempted to get server in client command");
    }

    /**
     * @throws UnsupportedOperationException because the server side level isn't available on the client side
     */
    @Override
    public ServerWorld getLevel() {
        throw new UnsupportedOperationException("Attempted to get server level in client command");
    }

}
