package cc.edgerealm.reaction

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

val serializer = LegacyComponentSerializer.legacyAmpersand()

class ReactionMessage(private val plugin: Reaction) {
    private var prefixString = "&6[Reaction] "
    private var insufficientPermissionString = "&c你沒有權限執行這個指令"
    private var reloadErrorString = "&c重新讀取設定時發生錯誤（請查看主控臺）"
    private var reloadCompleteString = "&a已重新讀取設定"
    private var reactionCorrectString = "&a%player% 在 %seconds% 秒內答對了"
    private var reactionTimeoutString = "&c沒有人在時間內答對 :<"
    private var reactionQuestionString = "&e將滑鼠移到這裡回答問題"

    val prefix
        get() = serializer.deserialize(prefixString)
    val insufficientPermission
        get() = prefix.append(serializer.deserialize(insufficientPermissionString))
    val reloadError
        get() = prefix.append(serializer.deserialize(reloadErrorString))
    val reloadComplete
        get() = prefix.append(serializer.deserialize(reloadCompleteString))

    fun reactionCorrect(player: String, seconds: String): TextComponent {
        return serializer.deserialize(
            reactionCorrectString
                .replace("%player%", player)
                .replace("%seconds%", seconds)
        )
    }

    val reactionTimeout
        get() = prefix.append(serializer.deserialize(reactionTimeoutString))
    val reactionQuestion
        get() = prefix.append(serializer.deserialize(reactionQuestionString))

    fun load(): Boolean {
        var success = true

        try {
            val file = File(plugin.dataFolder, "messages.yml")

            if (!file.exists()) {
                plugin.saveResource("messages.yml", false)
            }

            val config = YamlConfiguration.loadConfiguration(file)

            config.getString("prefix")?.let { v -> prefixString = v }
            config.getString("insufficient_permission")?.let { v -> insufficientPermissionString = v }
            config.getString("reload_error")?.let { v -> reloadErrorString = v }
            config.getString("reload_complete")?.let { v -> reloadCompleteString = v }
            config.getString("reaction_correct")?.let { v -> reactionCorrectString = v }
            config.getString("reaction_timeout")?.let { v -> reactionTimeoutString = v }
            config.getString("reaction_question")?.let { v -> reactionQuestionString = v }
        } catch (e: Exception) {
            success = false
            plugin.componentLogger.error("讀取訊息時發生錯誤: $e", e)
        }

        return success
    }
}