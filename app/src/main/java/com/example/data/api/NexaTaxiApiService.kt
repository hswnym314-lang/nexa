package com.example.data.api

import com.example.data.model.Driver
import com.example.data.model.RideRequest
import com.example.data.model.RideResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface NexaTaxiApiService {

    @GET(".")
    suspend fun checkHealth(): Response<ResponseBody>

    @GET("drivers")
    suspend fun getDrivers(): Response<List<Driver>>

    @POST("request-ride")
    suspend fun requestRide(@Body request: RideRequest): Response<RideResponse>

    companion object {
        const val BASE_URL = "https://jesse-native-rug-gst.trycloudflare.com/"

        fun create(): NexaTaxiApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .retryOnConnectionFailure(true)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(NexaTaxiApiService::class.java)
        }
    }
}
