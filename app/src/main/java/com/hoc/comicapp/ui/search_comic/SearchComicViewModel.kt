package com.hoc.comicapp.ui.search_comic

import androidx.lifecycle.viewModelScope
import com.hoc.comicapp.base.BaseViewModel
import com.hoc.comicapp.domain.models.getMessage
import com.hoc.comicapp.utils.Event
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchComicViewModel(private val interactor: SearchComicInteractor) :
  BaseViewModel<SearchComicViewIntent, SearchComicViewState, SearchComicSingleEvent>() {
  private val intentS = PublishRelay.create<SearchComicViewIntent>()
  private val compositeDisposable = CompositeDisposable()

  override val initialState = SearchComicViewState.initialState()

  override fun processIntents(intents: Observable<SearchComicViewIntent>) = intents.subscribe(intentS)!!

  init {
    val searchTerm = intentS
      .ofType<SearchComicViewIntent.SearchIntent>()
      .map { it.term }
      .doOnNext { Timber.d("[1] $it") }
      .debounce(600, TimeUnit.MILLISECONDS)
      .distinctUntilChanged()
      .doOnNext { Timber.d("[2] $it") }

    val retryPartialChange = intentS
      .ofType<SearchComicViewIntent.RetryIntent>()
      .withLatestFrom(searchTerm)
      .map { it.second }
      .switchMap { term ->
        interactor.searchComic(
          coroutineScope = viewModelScope,
          term = term
        ).doOnNext {
          val messageFromError =
            (it as? SearchComicPartialChange.Error ?: return@doOnNext).error.getMessage()
          sendEvent(Event(SearchComicSingleEvent.MessageEvent("Search for '$term', error occurred: $messageFromError")))
        }
      }
    val searchPartialChange = searchTerm
      .switchMap { term ->
        interactor.searchComic(
          coroutineScope = viewModelScope,
          term = term
        ).doOnNext {
          val messageFromError =
            (it as? SearchComicPartialChange.Error ?: return@doOnNext).error.getMessage()
          sendEvent(Event(SearchComicSingleEvent.MessageEvent("Retry search for '$term', error occurred: $messageFromError")))
        }
      }
    Observable.mergeArray(searchPartialChange, retryPartialChange)
      .scan(initialState) { state, change -> change.reducer(state) }
      .distinctUntilChanged()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(onNext = ::setNewState)
      .addTo(compositeDisposable = compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.dispose()
    Timber.d("SearchComicViewModel::onCleared")
  }
}