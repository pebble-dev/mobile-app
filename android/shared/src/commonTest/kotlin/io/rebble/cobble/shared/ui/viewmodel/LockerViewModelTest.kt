package io.rebble.cobble.shared.ui.viewmodel

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatform
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.domain.state.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test

class LockerViewModelTest : KoinTest {
    
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `given there is an exception when attempting to load entries then a error state is returned`() = runTest {
        setupKoin()
        val viewModel = LockerViewModel(lockerDao = object : LockerDao by NoOpLockerDao() {
            override fun getAllEntriesFlow(): Flow<List<SyncedLockerEntryWithPlatforms>> = flow {
                error("oops!")
            }
        }, dispatcher = StandardTestDispatcher(this.testScheduler))

        viewModel.entriesState.test {
            // first state is loading
            assertThat(awaitItem()).isEqualTo(LockerViewModel.LockerEntriesState.Loading)

            // second state should be error
            assertThat(awaitItem()).isEqualTo(LockerViewModel.LockerEntriesState.Error)

            cancel()
        }
    }

    @Test
    fun `given there are entries when entries are requested the loaded entry state is returned`() = runTest {
        setupKoin()
        val entries = listOf(SyncedLockerEntryWithPlatforms(entry = SyncedLockerEntry(id = "", uuid = "", version = "", title = "", type = "", hearts = 0, developerName = "", developerId = null, configurable = false, timelineEnabled = false, removeLink = "", shareLink = "", pbwLink = "", pbwReleaseId = "", pbwIconResourceId = 0, nextSyncAction = NextSyncAction.Upload, order = 0, lastOpened = null, local = false), platforms = listOf()))

        val viewModel = LockerViewModel(lockerDao = object : LockerDao by NoOpLockerDao() {
            override fun getAllEntriesFlow(): Flow<List<SyncedLockerEntryWithPlatforms>> = flow {
                emit(entries)
            }
        }, dispatcher = StandardTestDispatcher(this.testScheduler))

        viewModel.entriesState.test {
            // first state is loading
            assertThat(awaitItem()).isEqualTo(LockerViewModel.LockerEntriesState.Loading)

            // second state is our loaded state
            assertThat(awaitItem()).isEqualTo(LockerViewModel.LockerEntriesState.Loaded(entries = entries))

            cancel()
        }
    }

    @Test
    fun `given a model is provided when a model sheet is attempted to be opened then modal state returns open`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(this.testScheduler))
        setupKoin()

        val viewModel = LockerViewModel(lockerDao = object : LockerDao by NoOpLockerDao() {
            override fun getAllEntriesFlow(): Flow<List<SyncedLockerEntryWithPlatforms>> = flow {
                emit(emptyList())
            }
        }, dispatcher = StandardTestDispatcher(this.testScheduler))

        val model = LockerItemViewModel(
                httpClient = HttpClient(MockEngine.create {
                    addHandler {
                        HttpResponseData(statusCode = HttpStatusCode(value = 0, description = ""), requestTime = GMTDate(timestamp = null), headers = Headers.Empty, version = HttpProtocolVersion(name = "", major = 0, minor = 0), body = "", callContext = this@runTest.coroutineContext)
                    }
                }),
                entry = SyncedLockerEntryWithPlatforms(entry = SyncedLockerEntry(id = "", uuid = "", version = "", title = "", type = "", hearts = 0, developerName = "", developerId = null, configurable = false, timelineEnabled = false, removeLink = "", shareLink = "", pbwLink = "", pbwReleaseId = "", pbwIconResourceId = 0, nextSyncAction = NextSyncAction.Upload, order = 0, lastOpened = null, local = false), platforms = listOf()),
        )

        viewModel.modalSheetState.test {
            // first state
            assertThat(awaitItem()).isEqualTo(LockerViewModel.ModalSheetState.Closed)
            viewModel.openModalSheet(model)

            assertThat(awaitItem()).isEqualTo(LockerViewModel.ModalSheetState.Open(model))
        }
    }

    private fun setupKoin(connectState: ConnectionState = ConnectionState.Disconnected, isConnected: Boolean = false) {
        startKoin {
            modules(module {
                single(named("connectionState")) {
                    MutableStateFlow(connectState)
                } bind StateFlow::class

                single(named("isConnected")) {
                    MutableStateFlow(isConnected)
                } bind StateFlow::class
            })
        }
    }
}


/**
 * Exists so we can only stub out the LockerDao function we need for testing.
 *
 * ```
 *   val testDao = object: LockerDao by NoOpLockerDao() {
 *          // Only need to override the test method we're going to use.
 *         override suspend fun getSyncedEntries(): List<SyncedLockerEntry> {
 *             return emptyList()
 *         }
 *     }
 * ```
 */
class NoOpLockerDao : LockerDao {
    override suspend fun insertOrReplace(entry: SyncedLockerEntry) {
    }

    override suspend fun update(entry: SyncedLockerEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrReplacePlatform(platform: SyncedLockerEntryPlatform) {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrReplaceAll(entries: List<SyncedLockerEntry>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrReplaceAllPlatforms(platforms: List<SyncedLockerEntryPlatform>) {
        TODO("Not yet implemented")
    }

    override suspend fun getEntry(id: String): SyncedLockerEntryWithPlatforms? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllEntries(): List<SyncedLockerEntryWithPlatforms> {
        TODO("Not yet implemented")
    }

    override fun getAllEntriesFlow(): Flow<List<SyncedLockerEntryWithPlatforms>> {
        TODO("Not yet implemented")
    }

    override suspend fun clearPlatformsFor(entryId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setNextSyncAction(id: String, action: NextSyncAction) {
        TODO("Not yet implemented")
    }

    override suspend fun setNextSyncAction(ids: Set<String>, action: NextSyncAction) {
        TODO("Not yet implemented")
    }

    override suspend fun getEntriesForSync(): List<SyncedLockerEntryWithPlatforms> {
        TODO("Not yet implemented")
    }

    override suspend fun getEntryByUuid(uuid: String): SyncedLockerEntryWithPlatforms? {
        TODO("Not yet implemented")
    }

    override suspend fun updateOrder(id: String, order: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun clearAll() {
        TODO("Not yet implemented")
    }

    override suspend fun countEntriesWithNextSyncAction(action: NextSyncAction): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getSyncedEntries(): List<SyncedLockerEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastOpened(uuid: String, time: Instant?) {
        TODO("Not yet implemented")
    }

}