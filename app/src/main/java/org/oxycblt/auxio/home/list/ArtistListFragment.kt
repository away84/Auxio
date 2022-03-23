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
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentHomeListBinding
import org.oxycblt.auxio.home.HomeFragmentDirections
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.ui.ArtistViewHolder
import org.oxycblt.auxio.ui.newMenu
import org.oxycblt.auxio.ui.sliceArticle

/**
 * A [HomeListFragment] for showing a list of [Artist]s.
 * @author
 */
class ArtistListFragment : HomeListFragment() {
    override fun onBindingCreated(binding: FragmentHomeListBinding, savedInstanceState: Bundle?) {
        val homeAdapter =
            ArtistAdapter(
                doOnClick = { artist ->
                    findNavController().navigate(HomeFragmentDirections.actionShowArtist(artist.id))
                },
                ::newMenu)

        setupRecycler(R.id.home_artist_list, homeAdapter, homeModel.artists)
    }

    override val listPopupProvider: (Int) -> String
        get() = { idx ->
            homeModel.artists.value!![idx].resolvedName.sliceArticle().first().uppercase()
        }

    class ArtistAdapter(
        private val doOnClick: (data: Artist) -> Unit,
        private val doOnLongClick: (view: View, data: Artist) -> Unit,
    ) : HomeAdapter<Artist, ArtistViewHolder>() {
        override fun getItemCount(): Int = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
            return ArtistViewHolder.from(parent.context, doOnClick, doOnLongClick)
        }

        override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
            holder.bind(data[position])
        }
    }
}
