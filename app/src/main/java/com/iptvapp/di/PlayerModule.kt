package com.iptvapp.di

import android.content.Context
import com.iptvapp.player.PlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the media playback layer.
 *
 * Separated from [AppModule] to keep concerns grouped:
 * - [AppModule] → networking + database
 * - [PlayerModule] → media playback
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providePlayerManager(
        @ApplicationContext context: Context
    ): PlayerManager = PlayerManager(context)
}
