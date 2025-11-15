package com.theo.meowbook.api

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.mready.apiclient.ApiClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun okHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .connectTimeout(90, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun catApiClient(
        okHttpClient: OkHttpClient
    ): ApiClient = CatApiClient(
        httpClient = okHttpClient,
        apiKey = "live_ceFycb1FtwGpqaQ6esaSXETJuLOoN0n15q81nmIQqdJeCDYGsokbtDj7SF0kKClY"
    )
}