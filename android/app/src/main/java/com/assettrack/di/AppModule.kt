package com.assettrack.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.assettrack.BuildConfig
import com.assettrack.data.local.AssetTrackDatabase
import com.assettrack.data.local.dao.AssetDao
import com.assettrack.data.local.dao.TransactionDao
import com.assettrack.data.remote.api.AssetTrackApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AssetTrackDatabase =
        Room.databaseBuilder(
            context,
            AssetTrackDatabase::class.java,
            AssetTrackDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()   // wipe & recreate jika versi naik
            .build()

    @Provides
    fun provideAssetDao(db: AssetTrackDatabase): AssetDao = db.assetDao()

    @Provides
    fun provideTransactionDao(db: AssetTrackDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)       // lebih lama untuk upload file
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BASIC
                else
                    HttpLoggingInterceptor.Level.NONE
            }
        )
        .addInterceptor(com.assettrack.data.remote.interceptor.GzipRequestInterceptor())
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): AssetTrackApiService =
        retrofit.create(AssetTrackApiService::class.java)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
