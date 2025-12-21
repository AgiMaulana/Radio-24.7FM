package io.github.agimaulana.radio.core.network.test.randomizer

import com.pajk.idpersonaldoc.core.shared.test.randomizer.StringRandomizer
import java.util.Random

private const val STRING_LENGTH_LOWER_BOUND = 2
private const val STRING_LENGTH_UPPER_BOUND = 10
private const val HOST_AND_TLD_LENGTH = 3
private const val SLD_LENGTH = 10
private const val TOTAL_PATH_RANGE = 4
private const val PATH_LENGTH = 5

fun randomInt(): Int = Random().nextInt()

fun randomFloat(): Float = Random().nextFloat()

fun randomInt(range: Int): Int = Random().nextInt(range)

fun randomFloat(range: Float): Float = Random().nextFloat() * range

fun randomInt(start: Int, end: Int): Int = (start..end).random()

fun randomFloat(start: Float, end: Float): Float = Random().nextFloat() * (end - start) + start

fun randomLong(): Long = Random().nextLong()

fun randomLong(start: Long, end: Long): Long = (start..end).random()

fun randomDouble(): Double = Random().nextDouble()

fun randomBoolean(): Boolean = Random().nextBoolean()

fun randomString(
    length: Int = randomInt(
        STRING_LENGTH_LOWER_BOUND,
        STRING_LENGTH_UPPER_BOUND
    )
): String {
    return StringRandomizer.Builder()
        .length(length)
        .uppercaseLetter()
        .lowercaseLetter()
        .number()
        .build()
        .random()
}

fun randomAlphabeticString(
    length: Int = randomInt(
        STRING_LENGTH_LOWER_BOUND,
        STRING_LENGTH_UPPER_BOUND
    )
): String {
    return StringRandomizer.Builder()
        .length(length)
        .uppercaseLetter()
        .lowercaseLetter()
        .build()
        .random()
}

fun randomLetter(): String = StringRandomizer.Builder()
    .length(1)
    .uppercaseLetter()
    .lowercaseLetter()
    .build()
    .random()

fun randomListOfString(size: Int): List<String> {
    val list: MutableList<String> = mutableListOf()
    (0 until size).forEach { _ -> list.add(randomString()) }
    return list
}

fun randomUrl(): String = buildString {
    append("https://")
    append(randomString(HOST_AND_TLD_LENGTH)) // www
    append(".")
    append(randomString(SLD_LENGTH))
    append(".")
    append(randomString(HOST_AND_TLD_LENGTH)) // com

    val pathCount = randomInt(TOTAL_PATH_RANGE)
    (0 until pathCount).forEach { _ ->
        append("/").append(randomString(PATH_LENGTH))
    }
}
