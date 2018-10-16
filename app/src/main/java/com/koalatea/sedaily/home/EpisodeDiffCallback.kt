package com.koalatea.sedaily.home

import androidx.recyclerview.widget.DiffUtil
import com.koalatea.sedaily.models.Episode

class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
    override fun areItemsTheSame(
            oldItem: Episode,
            newItem: Episode
    ): Boolean {
        return oldItem?._id == newItem?._id
    }

    override fun areContentsTheSame(
            oldItem: Episode,
            newItem: Episode
    ): Boolean {
        return oldItem._id == newItem._id
    }
}