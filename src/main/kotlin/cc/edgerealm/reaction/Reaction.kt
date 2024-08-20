package cc.edgerealm.reaction

import cc.edgerealm.reaction.model.config.Question
import cc.edgerealm.reaction.task.ReactionTask
import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class Reaction : JavaPlugin() {
    private lateinit var questions: List<Question>
    private var duration: Long = 0
    private var frequency: Long = 0
    private var questionTask: ReactionTask? = null

    val prefix = Component.text("[Reaction] ", NamedTextColor.GOLD)

    private fun loadConfig(): Boolean {
        componentLogger.info("讀取設定檔中...")

        reloadConfig()
        var success = true
        val newFrequency = config.getLong("frequency")
        val newDuration = config.getLong("duration")

        if (newDuration > newFrequency) {
            componentLogger.error(prefix.append(Component.text("反應長度不得大於頻率，請檢查設定檔並在修正後重新載入插件。")))
        } else {
            frequency = newFrequency
            duration = newDuration
        }

        val questionList = config.getMapList("questions")
        questions = questionList.mapNotNull {
            try {
                @Suppress("UNCHECKED_CAST") Question.deserialize(it as Map<String?, Any?>)
            } catch (e: Exception) {
                success = false
                componentLogger.error(Component.text("無效的問題格式: $it. 跳過中..."), e)
                null
            }
        }
        componentLogger.info("已讀取設定檔")

        return success
    }

    private fun getRandomQuestion(): Question? {
        return if (questions.isNotEmpty()) {
            questions[Random.nextInt(questions.size)]
        } else {
            null
        }
    }

    override fun onEnable() {
        saveDefaultConfig()
        loadConfig()

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register(
                Commands
                    .literal("reaction")
                    .executes { ctx ->
                        val sender = ctx.source.sender
                        sender.sendMessage(
                            prefix.append(
                                Component.text(
                                    "Reaction v${this.pluginMeta.version}",
                                    NamedTextColor.YELLOW,
                                )
                            )
                        )
                        sender.sendMessage(
                            prefix.append(
                                Component.text(
                                    "Github: https://github.com/edgerealm/reaction",
                                    NamedTextColor.YELLOW,
                                ).clickEvent(ClickEvent.openUrl("https://github.com/edgerealm/reaction"))
                            )
                        )
                        Command.SINGLE_SUCCESS
                    }.then(
                        Commands
                            .literal("reload")
                            .requires { source -> source.sender.hasPermission("reaction.reload") }
                            .executes { ctx ->
                                val sender = ctx.source.sender

                                if (!sender.hasPermission("reaction.reload")) {
                                    sender.sendMessage(
                                        prefix.append(
                                            Component.text(
                                                "你沒有權限執行這個指令",
                                                NamedTextColor.RED,
                                            )
                                        )
                                    )
                                    return@executes Command.SINGLE_SUCCESS
                                }

                                if (!loadConfig()) {
                                    sender.sendMessage(
                                        prefix.append(
                                            Component.text(
                                                "重新讀取設定時發生錯誤（請查看主控臺）",
                                                NamedTextColor.RED,
                                            )
                                        )
                                    )
                                }

                                sender.sendMessage(
                                    prefix.append(
                                        Component.text(
                                            "已重新讀取設定",
                                            NamedTextColor.GREEN
                                        )
                                    )
                                )

                                return@executes Command.SINGLE_SUCCESS
                            }
                    ).build()
            )
        }

        makeSchedule()
    }

    private fun makeSchedule() {
        questionTask?.stopTask()

        questionTask = ReactionTask(this, duration) { getRandomQuestion() }
        questionTask?.startTask(frequency)
    }

    override fun onDisable() {
        questionTask?.stopTask()
    }
}
