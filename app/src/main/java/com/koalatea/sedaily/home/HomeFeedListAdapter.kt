package com.koalatea.sedaily.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.databinding.ItemEpisodeBinding
import com.koalatea.sedaily.models.Episode

class HomeFeedListAdapter (
        private val homeFeedViewModel: HomeFeedViewModel
): RecyclerView.Adapter<HomeFeedListAdapter.ViewHolder>() {
    // @TODO: Currently public for HomeFeedModel,but we probably need a better way to get last element
    lateinit var postList: List<Episode>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFeedListAdapter.ViewHolder {
        val binding: ItemEpisodeBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_episode, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFeedListAdapter.ViewHolder, position: Int) {
        val episode = postList[position]
        holder.bind(createOnClickListener(episode._id), createPlayClickListener(episode), episode)
    }

    override fun getItemCount(): Int {
        return if(::postList.isInitialized) postList.size else 0
    }

    private fun createOnClickListener(episodeId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = MainFragmentDirections.ActionPlantListFragmentToPlantDetailFragment(episodeId)
            it.findNavController().navigate(direction)
        }
    }

    private fun createPlayClickListener(episode: Episode): View.OnClickListener {
        return View.OnClickListener {
           homeFeedViewModel.play(episode)
        }
    }

    fun updateFeedList(postList: List<Episode>) {
        this.postList = postList
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemEpisodeBinding
    ): RecyclerView.ViewHolder(binding.root) {
        private val viewModel = EpisodeViewModel()

        fun bind(listener: View.OnClickListener, playListener: View.OnClickListener, episode: Episode) {
            viewModel.bind(episode)
            binding.viewModel = viewModel
            binding.clickListener = listener
            binding.playClickListener = playListener
        }
    }
}