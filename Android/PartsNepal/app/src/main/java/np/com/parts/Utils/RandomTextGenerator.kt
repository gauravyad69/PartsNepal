package np.com.parts.Utils

object RandomTextGenerator {
    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    // Generate random string with specified length
    fun generate(length: Int): String {
        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }

    // Generate random string with length range
    fun generateInRange(minLength: Int, maxLength: Int): String {
        val length = (minLength..maxLength).random()
        return generate(length)
    }

    // Generate with specific character sets
    fun generateCustom(
        length: Int,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
    ): String {
        var characters = mutableListOf<Char>()

        if (includeUppercase) characters.addAll('A'..'Z')
        if (includeLowercase) characters.addAll('a'..'z')
        if (includeNumbers) characters.addAll('0'..'9')

        require(characters.isNotEmpty()) { "At least one character set must be included" }

        return (1..length)
            .map { characters.random() }
            .joinToString("")
    }
}