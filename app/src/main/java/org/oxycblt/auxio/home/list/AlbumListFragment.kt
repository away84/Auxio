/*
 * Copyright (c) 2021 Auxio Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.home.list

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import java.util.Formatter
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentHomeListBinding
import org.oxycblt.auxio.home.HomeViewModel
import org.oxycblt.auxio.list.*
import org.oxycblt.auxio.list.SelectionFragment
import org.oxycblt.auxio.list.recycler.AlbumViewHolder
import org.oxycblt.auxio.list.recycler.SelectionIndicatorAdapter
import org.oxycblt.auxio.list.recycler.SyncListDiffer
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Music
import org.oxycblt.auxio.music.MusicMode
import org.oxycblt.auxio.music.MusicParent
import org.oxycblt.auxio.music.Sort
import org.oxycblt.auxio.playback.formatDurationMs
import org.oxycblt.auxio.playback.secsToMs
import org.oxycblt.auxio.util.collectImmediately

/**
 * A [HomeListFragment] for showing a list of [Album]s.
 * @author OxygenCobalt
 */
class AlbumListFragment : SelectionFragment<FragmentHomeListBinding>() {
    private val homeModel: HomeViewModel by activityViewModels()

    private val homeAdapter =
        AlbumAdapter(ItemSelectCallback(::handleClick, ::handleOpenMenu, ::handleClick))

    private val formatterSb = StringBuilder(32)
    private val formatter = Formatter(formatterSb)

    override fun onCreateBinding(inflater: LayoutInflater) =
        FragmentHomeListBinding.inflate(inflater)

    override fun onBindingCreated(binding: FragmentHomeListBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)

        binding.homeRecycler.apply {
            id = R.id.home_album_recycler
            adapter = homeAdapter

            popupProvider = ::updatePopup
            fastScrollCallback = { homeModel.setFastScrolling(it) }
        }

        collectImmediately(homeModel.albums, homeAdapter::replaceList)
        collectImmediately(selectionModel.selected, homeAdapter::setSelected)
        collectImmediately(playbackModel.parent, playbackModel.isPlaying, ::updatePlayback)
    }

    override fun onDestroyBinding(binding: FragmentHomeListBinding) {
        super.onDestroyBinding(binding)
        binding.homeRecycler.adapter = null
    }

    override fun onClick(music: Music) {
        check(music is Album) { "Unexpected datatype: ${music::class.java}" }
        navModel.exploreNavigateTo(music)
    }

    private fun handleOpenMenu(item: Item, anchor: View) {
        check(item is Album) { "Unexpected datatype: ${item::class.java}" }
        openMusicMenu(anchor, R.menu.menu_album_actions, item)
    }

    private fun updatePopup(pos: Int): String? {
        val album = homeModel.albums.value[pos]

        // Change how we display the popup depending on the mode.
        return when (homeModel.getSortForTab(MusicMode.ALBUMS).mode) {
            // By Name -> Use Name
            is Sort.Mode.ByName -> album.collationKey?.run { sourceString.first().uppercase() }

            // By Artist -> Use name of first artist
            is Sort.Mode.ByArtist ->
                album.artists[0].collationKey?.run { sourceString.first().uppercase() }

            // Year -> Use Full Year
            is Sort.Mode.ByDate -> album.date?.resolveDate(requireContext())

            // Duration -> Use formatted duration
            is Sort.Mode.ByDuration -> album.durationMs.formatDurationMs(false)

            // Count -> Use song count
            is Sort.Mode.ByCount -> album.songs.size.toString()

            // Last added -> Format as date
            is Sort.Mode.ByDateAdded -> {
                val dateAddedMillis = album.dateAdded.secsToMs()
                formatterSb.setLength(0)
                DateUtils.formatDateRange(
                        context,
                        formatter,
                        dateAddedMillis,
                        dateAddedMillis,
                        DateUtils.FORMAT_ABBREV_ALL)
                    .toString()
            }

            // Unsupported sort, error gracefully
            else -> null
        }
    }
    private fun updatePlayback(parent: MusicParent?, isPlaying: Boolean) {
        if (parent is Album) {
            homeAdapter.updateIndicator(parent, isPlaying)
        } else {
            // Ignore playback not from albums
            homeAdapter.updateIndicator(null, isPlaying)
        }
    }

    private class AlbumAdapter(private val callback: ItemSelectCallback) :
        SelectionIndicatorAdapter<AlbumViewHolder>() {
        private val differ = SyncListDiffer(this, AlbumViewHolder.DIFFER)

        override val currentList: List<Item>
            get() = differ.currentList

        override fun getItemCount() = differ.currentList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AlbumViewHolder.new(parent)

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int, payloads: List<Any>) {
            super.onBindViewHolder(holder, position, payloads)

            if (payloads.isEmpty()) {
                holder.bind(differ.currentList[position], callback)
            }
        }

        fun replaceList(newList: List<Album>) {
            differ.replaceList(newList)
        }
    }
}
