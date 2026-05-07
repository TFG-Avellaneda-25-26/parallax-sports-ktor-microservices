package es.daw.parallaxbot.discord.bot

import es.daw.parallaxbot.common.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("ConfigureDiscordBot")


// -> Source: Service Startup || Action: Connect Discord bot and register global slash commands || Strategy: awaitReady before command registration
fun configureDiscordBot(
    listener: DiscordListener,
    config: DiscordConfig,
    commands: List<ICommand>
): JDA {

    val jda = JDABuilder.createDefault(config.token)
        .enableIntents(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.DIRECT_MESSAGES
        )
        .addEventListeners(listener)
        .build()

    jda.awaitReady()

    if (!config.devGuild.isNullOrBlank()) {
        jda.registerGuildCommands(config.devGuild, commands)
    } else {
        jda.registerGlobalCommands(commands)
    }

    logger.info("DiscordBot started: ${jda.selfUser.name}")

    return jda
}

/**
 * Registers slash commands globally across every guild the bot is in.
 *
 * Discord propagates global commands in up to ~1 hour; this is acceptable for
 * a bot that expects to be installed into many guilds.
 */
private fun JDA.registerGlobalCommands(commands: List<ICommand>) {
    val jdaCommands = commands.map { it.getCommandData() }
    updateCommands().addCommands(jdaCommands).queue(
        { logger.info("Global commands updated (${jdaCommands.size})") },
        { err -> logger.error("Global commands update failed!", err) }
    )
}

/**
 *  Register slash commands in one specific guild
 *  Discord propagates guild commands instantly; this is optimal for
 *  testing in one specific server
 */
private fun JDA.registerGuildCommands(devGuild: String, commands: List<ICommand>) {
    val guild = getGuildById(devGuild)

    val jdaCommands = commands.map { it.getCommandData() }
    guild?.updateCommands()?.addCommands(jdaCommands)?.queue(
        { logger.info("Dev guild commands updated (${jdaCommands.size})") },
        { err -> logger.error("Failed to update dev guild commands!", err) }
    )
}
