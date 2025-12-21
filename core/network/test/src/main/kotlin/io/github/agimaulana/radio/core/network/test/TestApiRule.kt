package io.github.agimaulana.radio.core.network.test

import io.github.agimaulana.radio.core.network.RetrofitBuilderFactory
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Retrofit
import kotlin.reflect.KClass

class TestApiRule(
    private val basePath: String = "/",
) : TestWatcher() {

    private val mockWebServer = MockWebServer()
    private lateinit var retrofit: Retrofit

    val baseUrl: String
        get() = mockWebServer.url(path = basePath).toString()

    override fun starting(description: Description) {
        retrofit = RetrofitBuilderFactory()
            .create(
                baseUrl = baseUrl,
                okHttpClient = OkHttpClient.Builder().build()
            )
            .build()
    }

    override fun finished(description: Description) {
        mockWebServer.shutdown()
    }

    fun <T : Any> create(clazz: KClass<T>): T = retrofit.create(clazz.java)

    fun setResponse(responseCode: Int, data: String) {
        val response = MockResponse().apply {
            setResponseCode(responseCode)
            setBody(data)
        }
        mockWebServer.enqueue(response)
    }

    /**
     * Just can be called once per request
     */
    fun takeLastRequest(): Request {
        val request = mockWebServer.takeRequest()
        return Request(
            method = request.method,
            url = request.requestUrl?.toString(),
            path = request.requestUrl?.encodedPath,
            body = request.body.readUtf8(),
            queryParameters = request.requestUrl?.run {
                queryParameterNames.associateWith {
                    queryParameterValues(it).takeIf { q -> q.size > 1 }
                        ?: queryParameter(it)
                        ?: ""
                }
            } ?: emptyMap(),
            pathWithQueryParams = request.requestUrl?.encodedPath + "?" + request.requestUrl?.encodedQuery,
            headers = request.headers.associate { it.first to it.second }
        )
    }

    data class Request(
        val method: String?,
        val url: String?,
        val path: String?,
        val body: String,
        val queryParameters: Map<String, Any>,
        val pathWithQueryParams: String?,
        val headers: Map<String, String>,
    )
}