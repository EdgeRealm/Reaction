package cc.edgerealm.reaction.model.config

import cc.edgerealm.reaction.util.convertToStringList
import org.bukkit.configuration.serialization.ConfigurationSerializable

class Question(
    val question: String,
    val answer: String,
    val commands: List<String>
) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> {
        val data: MutableMap<String, Any> = HashMap()

        data["question"] = question
        data["answer"] = answer
        data["commands"] = commands

        return data
    }

    companion object {
        fun deserialize(args: Map<String?, Any?>): Question {
            return Question(
                args["question"] as String,
                args["answer"] as String,
                convertToStringList(args["commands"]),
            )
        }
    }
}