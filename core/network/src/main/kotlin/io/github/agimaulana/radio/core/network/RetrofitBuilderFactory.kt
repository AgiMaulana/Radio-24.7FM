package io.github.agimaulana.radio.core.network

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RetrofitBuilderFactory {
    fun create(
        baseUrl: String,
        moshi: Moshi,
        okHttpClient: OkHttpClient = OkHttpClient.Builder().build(),
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
    }
}
