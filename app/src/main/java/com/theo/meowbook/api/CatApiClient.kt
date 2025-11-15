package com.theo.meowbook.api

import com.theo.meowbook.BuildConfig
import net.mready.apiclient.ApiClient
import okhttp3.OkHttpClient
import okhttp3.Request

const val API_KEY_HEADER = "x-api-key"


class CatApiClient(
    private val apiKey: String,
    httpClient: OkHttpClient
) : ApiClient(httpClient = httpClient, baseUrl = BuildConfig.API_HOST)
{

    @Throws(Throwable::class)
    override suspend fun buildRequest(builder: Request.Builder): Request {
        builder.run {
            addHeader(API_KEY_HEADER, apiKey)
        }

        return super.buildRequest(builder)
    }
}