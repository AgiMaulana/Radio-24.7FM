package com.pajk.idpersonaldoc.core.shared.test.randomizer

import io.github.agimaulana.radio.core.network.test.randomizer.LowercaseLetterRandomizer
import io.github.agimaulana.radio.core.network.test.randomizer.NumberCharRandomizer
import io.github.agimaulana.radio.core.network.test.randomizer.Randomizer
import io.github.agimaulana.radio.core.network.test.randomizer.UppercaseLetterRandomizer

class StringRandomizer private constructor(
    private val maxLength: Int = 10,
    private val randomizers: List<Randomizer<Char>>
) : Randomizer<String> {
    override fun random(): String {
        return (0 until maxLength).map {
            randomizers.random().random()
        }.joinToString("")
    }

    class Builder {
        private var minLength = 0
        private var maxLength: Int = DEFAULT_LENGTH
        private val randomizers: MutableList<Randomizer<Char>> = mutableListOf()

        fun minimum(minLength: Int): Builder {
            this.minLength = minLength
            return this
        }

        fun length(length: Int): Builder {
            this.maxLength = length
            return this
        }

        fun lowercaseLetter(): Builder {
            randomizers.add(LowercaseLetterRandomizer())
            return this
        }

        fun uppercaseLetter(): Builder {
            randomizers.add(UppercaseLetterRandomizer())
            return this
        }

        fun number(): Builder {
            randomizers.add(NumberCharRandomizer())
            return this
        }

        fun build(): StringRandomizer {
            val finalRandomizer = randomizers.ifEmpty { listOf(LowercaseLetterRandomizer()) }
            return StringRandomizer(maxLength, finalRandomizer)
        }

        companion object {
            private const val DEFAULT_LENGTH = 10
        }
    }
}
