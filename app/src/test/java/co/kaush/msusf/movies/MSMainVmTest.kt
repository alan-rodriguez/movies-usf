package co.kaush.msusf.movies

import co.kaush.msusf.MSApp
import co.kaush.msusf.movies.MSMovieEvent.ScreenLoadEvent
import co.kaush.msusf.movies.MSMovieEvent.SearchMovieEvent
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class MSMainVmTest {

    private lateinit var viewModel: MSMainVm

    @Test
    fun onSubscribing_shouldReceiveStartingViewState() {
        val mockMovieRepo: MSMovieRepository = mock(MSMovieRepository::class.java)

        viewModel = MSMainVm(mockApp, mockMovieRepo)

        val eventTester = PublishSubject.create<MSMovieEvent>()
        val viewModelTester = viewModel.send(eventTester).test()

        viewModelTester.assertValueCount(1)
    }

    @Test
    fun onScreenLoad_searchBoxText_shouldBeCleared() {
        val mockMovieRepo: MSMovieRepository = mock(MSMovieRepository::class.java)

        viewModel = MSMainVm(mockApp, mockMovieRepo)

        val eventTester = PublishSubject.create<MSMovieEvent>()
        val viewModelTester = viewModel.send(eventTester).test()

        eventTester.onNext(ScreenLoadEvent)

        viewModelTester.assertValueAt(1) {
            assertThat(it.searchBoxText).isEqualTo("")
            true
        }
    }

    @Test
    fun onSearchingMovie_shouldSeeSearchResults() {

        val mockMovieRepo: MSMovieRepository = mock(MSMovieRepository::class.java)
        whenever(mockMovieRepo.searchMovie("blade runner 2049"))
            .thenReturn(Observable.just(bladeRunner2049))

        viewModel = MSMainVm(mockApp, mockMovieRepo)

        val eventTester = PublishSubject.create<MSMovieEvent>()
        val viewModelTester = viewModel.send(eventTester).test()

        eventTester.onNext(SearchMovieEvent("blade runner 2049"))

        viewModelTester.awaitTerminalEvent(20L, TimeUnit.MILLISECONDS)

        assertThat(viewModelTester.valueCount()).isEqualTo(3)

        viewModelTester.assertValueAt(1) {
            assertThat(it.searchedMovieTitle).isEqualTo("Searching Movie...")
            true
        }

        viewModelTester.assertValueAt(2) {
            assertThat(it.searchedMovieTitle).isEqualTo("Blade Runner 2049")
            assertThat(it.searchedMoviePoster)
                .isEqualTo("https://m.media-amazon.com/images/M/MV5BNzA1Njg4NzYxOV5BMl5BanBnXkFtZTgwODk5NjU3MzI@._V1_SX300.jpg")
            assertThat(it.searchedMovieRating).isEqualTo("\n8.1/10 (IMDB)\n87% (RT)")

            true
        }
    }

    @Test
    fun onClickiMovieSearchResult_shouldPopulateHistoryList() {

        val mockMovieRepo: MSMovieRepository = mock(MSMovieRepository::class.java)
        whenever(mockMovieRepo.searchMovie("blade runner 2049"))
            .thenReturn(Observable.just(bladeRunner2049))

        viewModel = MSMainVm(mockApp, mockMovieRepo)

        val eventTester = PublishSubject.create<MSMovieEvent>()
        val viewModelTester = viewModel.send(eventTester).test()

        eventTester.onNext(SearchMovieEvent("blade runner 2049"))

        viewModelTester.awaitTerminalEvent(20L, TimeUnit.MILLISECONDS)

        assertThat(viewModelTester.valueCount()).isEqualTo(3)

        eventTester.onNext(MSMovieEvent.ClickMovieEvent)

        viewModelTester.awaitTerminalEvent(20L, TimeUnit.MILLISECONDS)

        assertThat(viewModelTester.valueCount()).isEqualTo(4)

        viewModelTester.assertValueAt(3) {
            assertThat(it.searchBoxText).isEqualTo(null) // prevents search box from reset
            assertThat(it.adapterList).hasSize(1)
            assertThat(it.adapterList.get(0)).isEqualTo(bladeRunner2049)
            true
        }
    }

    private val mockApp: MSApp by lazy { mock(MSApp::class.java) }

    private val bladeRunner2049 by lazy {
        val ratingImdb = MSRating(
            source = "Internet Movie Database",
            rating = "8.1/10"
        )

        val ratingRottenTomatoes = MSRating(
            source = "Rotten Tomatoes",
            rating = "87%"
        )

        MSMovie(
            result = true,
            errorMessage = null,
            title = "Blade Runner 2049",
            ratings = listOf(ratingImdb, ratingRottenTomatoes),
            posterUrl = "https://m.media-amazon.com/images/M/MV5BNzA1Njg4NzYxOV5BMl5BanBnXkFtZTgwODk5NjU3MzI@._V1_SX300.jpg"
        )
    }
}