package cc.edgerealm.reaction.task

import cc.edgerealm.reaction.Reaction
import cc.edgerealm.reaction.model.config.Question
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable

class ReactionTask(
    private val plugin: Reaction, private val duration: Long, private val questionProvider: () -> Question?
) : BukkitRunnable(), Listener {
    private var currentQuestion: Question? = null
    private var timeoutTask: BukkitRunnable? = null
    private var startTime: Long = 0L

    override fun run() {
        val question = questionProvider()
        if (question != null) {
            currentQuestion = question

            plugin.server.broadcast(
                plugin.message.reactionQuestion
                    .hoverEvent(HoverEvent.showText(Component.text(question.question)))
            )
            plugin.server.pluginManager.registerEvents(this, plugin)
            startTime = System.currentTimeMillis()

            timeoutTask = object : BukkitRunnable() {
                override fun run() {
                    plugin.server.broadcast(plugin.message.reactionTimeout)
                    cleanup()
                }
            }
            timeoutTask?.runTaskLater(plugin, duration)
        }
    }

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val player = event.player
        val message = LegacyComponentSerializer.legacySection().serialize(event.originalMessage()).trim()

        if (currentQuestion != null && message == currentQuestion!!.answer) {
            event.isCancelled = true

            val elapsedTime = System.currentTimeMillis() - startTime
            val elapsedSeconds = elapsedTime / 1000.0

            plugin.server.broadcast(
                plugin.message.prefix.append(
                    plugin.message.reactionCorrect(player.name, "$elapsedSeconds")
                )
            )
            executeRewardCommands(player, currentQuestion!!.commands)

            cleanup()
        }
    }

    private fun cleanup() {
        AsyncChatEvent.getHandlerList().unregister(this)

        timeoutTask?.cancel()
        timeoutTask = null

        currentQuestion = null
    }

    private fun executeRewardCommands(player: Player, commands: List<String>) {
        for (command in commands) {
            val formattedCommand = command.replace("%player%", player.name)
            object : BukkitRunnable() {
                override fun run() {
                    Bukkit.dispatchCommand(plugin.server.consoleSender, formattedCommand)
                }
            }.runTask(plugin)
        }
    }

    fun startTask(interval: Long) {
        this.runTaskTimer(plugin, 0, interval)
    }

    fun stopTask() {
        this.cancel()
    }
}