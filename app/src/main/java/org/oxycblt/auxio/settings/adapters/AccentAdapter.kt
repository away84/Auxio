package org.oxycblt.auxio.settings.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.ItemAccentBinding
import org.oxycblt.auxio.utils.ACCENTS
import org.oxycblt.auxio.utils.accent
import org.oxycblt.auxio.utils.getAccentItemSummary
import org.oxycblt.auxio.utils.toColor

class AccentAdapter(
    private val doOnAccentConfirm: (accent: Pair<Int, Int>) -> Unit
) : RecyclerView.Adapter<AccentAdapter.ViewHolder>() {
    override fun getItemCount(): Int = ACCENTS.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAccentBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ACCENTS[position])
    }

    inner class ViewHolder(
        private val binding: ItemAccentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(accentData: Pair<Int, Int>) {
            binding.accent.apply {
                contentDescription = getAccentItemSummary(context, accentData)

                setOnClickListener {
                    doOnAccentConfirm(accentData)
                }

                imageTintList = if (accentData.first == accent.first) {
                    isEnabled = false

                    ColorStateList.valueOf(
                        R.color.background.toColor(context)
                    )
                } else {
                    isEnabled = true

                    ColorStateList.valueOf(
                        android.R.color.transparent.toColor(context)
                    )
                }

                backgroundTintList = ColorStateList.valueOf(
                    accentData.first.toColor(context)
                )
            }
        }
    }
}