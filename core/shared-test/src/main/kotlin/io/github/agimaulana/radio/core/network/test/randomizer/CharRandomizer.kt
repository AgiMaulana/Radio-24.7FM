package io.github.agimaulana.radio.core.network.test.randomizer

class LowercaseLetterRandomizer : Randomizer<Char> {
    override fun random(): Char {
        return ('a'..'z').random()
    }
}

class UppercaseLetterRandomizer : Randomizer<Char> {
    override fun random(): Char {
        return ('A'..'Z').random()
    }
}

class NumberCharRandomizer : Randomizer<Char> {
    override fun random(): Char {
        return randomInt(MINIMUM_NUMBER, MAXIMUM_NUMBER).digitToChar()
    }

    companion object {
        private const val MINIMUM_NUMBER = 0
        private const val MAXIMUM_NUMBER = 9
    }
}
