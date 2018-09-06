package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post

class PodcastHelper {
    companion object {
        fun hasValidMp3(post: Post?): Boolean {
            return post != null && post.mp3 != null && !post.mp3.isEmpty()
        }
    }
}