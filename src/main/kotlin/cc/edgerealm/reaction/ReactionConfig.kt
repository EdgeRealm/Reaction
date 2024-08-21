package cc.edgerealm.reaction

import cc.edgerealm.reaction.model.config.Question

class ReactionConfig(private val plugin: Reaction) {
    lateinit var questions: List<Question>
    var duration: Long = 0
    var frequency: Long = 0

    fun load(): Boolean {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()

        var success = true
        val newFrequency = plugin.config.getLong("frequency")
        val newDuration = plugin.config.getLong("duration")

        if (newDuration > newFrequency) {
            plugin.componentLogger.error("反應長度不得大於頻率，請檢查設定檔並在修正後重新載入設定檔。")
        } else {
            frequency = newFrequency
            duration = newDuration
        }

        val questionList = plugin.config.getMapList("questions")
        questions = questionList.mapNotNull {
            try {
                @Suppress("UNCHECKED_CAST") Question.deserialize(it as Map<String?, Any?>)
            } catch (e: Exception) {
                success = false
                plugin.componentLogger.error("無效的問題格式: $it. 跳過中...", e)
                null
            }
        }

        return success
    }
}