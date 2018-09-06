package com.koalatea.thehollidayinn.softwareengineeringdaily.data

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastHelper
import org.junit.Test
import kotlin.test.assertEquals

class PostcastHelperUnitTest {
  @Test
  fun returnsFalseWhenNull() {
    val isValid = PodcastHelper.hasValidMp3(null)
    assertEquals(false, isValid)
  }

    @Test
    fun returnsFalseWhenMP3IsNull() {
        val post = Post()
        val isValid = PodcastHelper.hasValidMp3(post)
        assertEquals(false, isValid)
    }

    @Test
    fun returnsFalseWhenMP3IsEmptyString() {
        val post = Post()
        post.mp3 = ""
        val isValid = PodcastHelper.hasValidMp3(post)
        assertEquals(false, isValid)
    }
}