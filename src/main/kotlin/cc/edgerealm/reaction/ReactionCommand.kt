package cc.edgerealm.reaction

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

class ReactionCommand(private val plugin: Reaction) {
    private val versionText = Component.text(
        "Reaction v${plugin.pluginMeta.version}",
        NamedTextColor.YELLOW,
    )

    private val githubUrl = Component.text(
        "Github: https://github.com/edgerealm/reaction",
        NamedTextColor.YELLOW,
    ).clickEvent(ClickEvent.openUrl("https://github.com/edgerealm/reaction"))

    val reaction = Commands
        .literal("reaction")
        .executes { ctx ->
            val sender = ctx.source.sender

            val version = plugin.message.prefix.append(versionText)
            val github = plugin.message.prefix.append(githubUrl)

            sender.sendMessage(version)
            sender.sendMessage(github)

            Command.SINGLE_SUCCESS
        }

    val reload = Commands
        .literal("reload")
        .requires { source -> source.sender.hasPermission("reaction.reload") }
        .executes { ctx ->
            val sender = ctx.source.sender

            if (!sender.hasPermission("reaction.reload")) {
                sender.sendMessage(plugin.message.insufficientPermission)
                return@executes Command.SINGLE_SUCCESS
            }

            plugin.componentLogger.info("讀取設定檔中...")

            if (!plugin.cfg.load() or !plugin.message.load()) {
                sender.sendMessage(plugin.message.reloadError)
            }

            plugin.makeSchedule()

            plugin.componentLogger.info("已讀取設定檔")

            sender.sendMessage(plugin.message.reloadComplete)

            return@executes Command.SINGLE_SUCCESS
        }
}
