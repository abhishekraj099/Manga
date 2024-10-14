package com.example

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMangaApi(): MangaApi {
        return Retrofit.Builder()
            .baseUrl("https://api.jikan.moe/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMangaRepository(api: MangaApi): MangaRepository {
        return MangaRepositoryImpl(api)
    }
}
