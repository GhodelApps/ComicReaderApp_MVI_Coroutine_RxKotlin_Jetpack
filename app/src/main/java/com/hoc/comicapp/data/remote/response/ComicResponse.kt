package com.hoc.comicapp.data.remote.response

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ComicResponse(
  @field:Json(name = "last_chapters")
  val lastChapters: List<LastChapter>,
  @field:Json(name = "link")
  val link: String, // https://ww2.mangafox.online/volcanic-age
  @field:Json(name = "thumbnail")
  val thumbnail: String, // https://cdn1.mangafox.online/132/857/695/330/341/5/volcanic-age.jpg
  @field:Json(name = "title")
  val title: String, // Volcanic Age
  @field:Json(name = "view")
  val view: String, // 1.1k
) {
  @Keep
  data class LastChapter(
    @field:Json(name = "chapter_link")
    val chapterLink: String, // https://ww2.mangafox.online/volcanic-age/chapter-90-512420270425665
    @field:Json(name = "chapter_name")
    val chapterName: String, // Chapter 90
    @field:Json(name = "time")
    val time: String, // 2 days ago
  )
}
