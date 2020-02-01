package dev.vankka.jdacommands.model;

import net.dv8tion.jda.api.entities.Guild;

/**
 * The prefix provider interface.
 */
public interface PrefixProvider {
    /**
     * Provides prefixes depending on guild.
     *
     * @param guild The guild this prefix is being requested for, may be null.
     * @param defaultPrefix The default prefix, if no special prefix is specified.
     * @return The prefix returned by this provider.
     */
    String providePrefix(Guild guild, String defaultPrefix);
}
