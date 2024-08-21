package cc.edgerealm.reaction

import cc.edgerealm.reaction.model.config.Question
import cc.edgerealm.reaction.task.ReactionTask
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class Reaction : JavaPlugin() {
    private var questionTask: ReactionTask? = null
    private val command = ReactionCommand(this)
    val cfg = ReactionConfig(this)
    val message = ReactionMessage(this)

    private fun getRandomQuestion(): Question? {
        return if (cfg.questions.isNotEmpty()) {
            cfg.questions[Random.nextInt(cfg.questions.size)]
        } else {
            null
        }
    }

    override fun onEnable() {
        cfg.load()
        message.load()

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register(
                command.reaction
                    .then(command.reload)
                    .build()
            )
        }

        makeSchedule()
    }

    private fun makeSchedule() {
        questionTask?.stopTask()

        questionTask = ReactionTask(this, cfg.duration) { getRandomQuestion() }
        questionTask?.startTask(cfg.frequency)
    }

    override fun onDisable() {
        questionTask?.stopTask()
    }
}
