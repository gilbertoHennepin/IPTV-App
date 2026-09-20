package com.iptvapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class annotated with @HiltAndroidApp to trigger
 * Hilt's code generation and serve as the application-level
 * dependency container.
 */
@HiltAndroidApp
class IPTVApplication : Application()
