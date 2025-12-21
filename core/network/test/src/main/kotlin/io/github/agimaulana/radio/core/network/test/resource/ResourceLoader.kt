package io.github.agimaulana.radio.core.network.test.resource

fun loadJsonResource(path: String): String {
    return ClassLoader.getSystemResourceAsStream(path).bufferedReader().use { it.readText() }
}