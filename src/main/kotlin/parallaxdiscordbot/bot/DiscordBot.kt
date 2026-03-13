package es.daw.parallaxdiscordbot.bot

import es.daw.parallaxdiscordbot.config.AppConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ConfigureDiscordBot")

// JDA Configuration & commands
fun configureDiscordBot(
    listener: DiscordListener,
    config: AppConfig,
    commands: List<ICommand>
): DiscordBotHandler {

    val jda = JDABuilder.createDefault(config.token)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
        .addEventListeners(listener)
        .build()

    jda.awaitReady()

    jda.registerCommands(commands, config.serverId)

    logger.info("DiscordBot started: ${jda.selfUser.name}")

    return DiscordBotHandler(jda, config.channelId)
}

private fun JDA.registerCommands(commands: List<ICommand>, serverId: String) {
    val jdaCommands = commands.map { it.getCommandData() }

    // Command setup for specific server (DEV Testing)
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

