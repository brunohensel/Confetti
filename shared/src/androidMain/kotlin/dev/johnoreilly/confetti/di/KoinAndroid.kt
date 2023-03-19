@file:OptIn(
    ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class,
    ExperimentalHorologistDataLayerApi::class
)
@file:Suppress("RemoveExplicitTypeArguments")

package dev.johnoreilly.confetti.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import coil.ImageLoader
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.google.android.horologist.data.ExperimentalHorologistDataLayerApi
import com.google.android.horologist.data.WearDataLayerRegistry
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toBlockingObservableSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.AndroidLoggingAnalyticsLogger
import dev.johnoreilly.confetti.analytics.FirebaseAnalyticsLogger
import dev.johnoreilly.confetti.settings.WearSettingsSerializer
import dev.johnoreilly.confetti.shared.BuildConfig
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.work.RefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

actual fun platformModule() = module {
    singleOf(::AndroidDateService).withOptions { bind<DateService>() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .apply {
                // TODO enable based on debug flag
                eventListenerFactory(LoggingEventListener.Factory())
            }
            .build()
    }
    factory<ApolloClient.Builder> {
        ApolloClient.Builder().okHttpClient(get())
    }
    single<ImageLoader> {
        ImageLoader.Builder(androidContext())
            .okHttpClient { get() }
            .build()
    }
    single<AnalyticsLogger> {
        if (BuildConfig.DEBUG) {
            AndroidLoggingAnalyticsLogger
        } else {
            FirebaseAnalyticsLogger
        }
    }
    single { androidContext().settingsStore }
    single<FlowSettings> { new(::DataStoreSettings) }
    single { get<FlowSettings>().toBlockingObservableSettings() }
    workerOf(::RefreshWorker)
    single { WorkManager.getInstance(androidContext()) }

    single<WearDataLayerRegistry> {
        WearDataLayerRegistry.fromContext(
            application = androidContext(),
            coroutineScope = get()
        ).apply {
            registerSerializer(WearSettingsSerializer)
        }
    }
}

val Context.settingsStore by preferencesDataStore("settings")

actual fun getDatabaseName(conference: String) = "$conference.db"
