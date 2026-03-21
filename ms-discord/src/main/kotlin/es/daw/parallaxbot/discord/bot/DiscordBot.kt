package es.daw.parallaxbot.discord.bot

import es.daw.parallaxbot.common.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ConfigureDiscordBot")

/**
 * Builds and starts the JDA instance, then registers configured slash commands.
 *
 * @param listener listener that dispatches slash commands.
 * @param config Discord runtime configuration.
 * @param commands command implementations to register for the target guild.
 * @return ready-to-use JDA instance.
 */
fun configureDiscordBot(
    listener: DiscordListener,
    config: DiscordConfig,
    commands: List<ICommand>
): JDA {

    val jda = JDABuilder.createDefault(config.token)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
        .addEventListeners(listener)
        .build()

    jda.awaitReady()
    jda.registerCommands(commands, config.serverId)

    logger.info("DiscordBot started: ${jda.selfUser.name}")

    return jda
}

/**
 * Registers slash commands for the configured guild scope.
 *
 * @param commands command implementations to expose.
 * @param serverId target guild where commands are updated.
 */
private fun JDA.registerCommands(commands: List<ICommand>, serverId: String) {
    val jdaCommands = commands.map { it.getCommandData() }

    /*============================================================
      COMMAND REGISTRATION
      Guild-scoped setup for deterministic rollout behavior
    ============================================================*/
    getGuildById(serverId)?.let { guild ->
        guild.updateCommands().addCommands(jdaCommands).queue {
            logger.info("Guild Commands updated in ${guild.name}")
        }
    } ?: logger.warn("Could not find guild with ID: $serverId")

    // Command setup for global bot (PRODUCTION)
    /*
        jda.updateCommands().addCommands(jdaCommands).queue {
            logger.info("Global Commands updated")
        }
    */
}

