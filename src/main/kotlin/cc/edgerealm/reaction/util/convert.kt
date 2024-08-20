package cc.edgerealm.reaction.util

fun convertToStringList(value: Any?): List<String> {
    return when (value) {
        is Int -> listOf(value.toString())
        is Double -> listOf(value.toString())
        is String -> listOf(value)
        is List<*> -> value.map { it.toString() } // Convert each element in the list to a string
        else -> throw IllegalArgumentException("Unsupported type for list conversion: ${value?.javaClass?.name}")
    }
}
