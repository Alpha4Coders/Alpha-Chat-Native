
package com.example.alpha_chat_native.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.alpha_chat_native.data.remote.MongoApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMongoApi(): MongoApi {
        return Retrofit.Builder()
            .baseUrl("https://your-backend-api-url.com/") // Replace with your actual backend URL hosting the MongoDB logic
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MongoApi::class.java)
    }
}
