package com.shyam.acerlauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.WallpaperOption

class WallpaperAdapter(
    private val wallpapers: List<WallpaperOption>,
    selectedId: String,
    private val onSelected: (WallpaperOption) -> Unit,
) : RecyclerView.Adapter<WallpaperAdapter.Holder>() {
    private var selected = selectedId

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = wallpapers[position].id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallpaper, parent, false)
        FocusAnimator.attach(view)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val option = wallpapers[position]
        holder.thumbnail.setImageResource(option.thumbnail)
        holder.label.setText(option.label)
        holder.mark.visibility = if (option.id == selected) View.VISIBLE else View.GONE
        holder.itemView.contentDescription = holder.itemView.context.getString(option.label)
        holder.itemView.setOnClickListener {
            val previous = wallpapers.indexOfFirst { item -> item.id == selected }
            selected = option.id
            if (previous >= 0) notifyItemChanged(previous)
            notifyItemChanged(holder.bindingAdapterPosition)
            onSelected(option)
        }
    }

    override fun getItemCount(): Int = wallpapers.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.wallpaperThumb)
        val label: TextView = view.findViewById(R.id.wallpaperLabel)
        val mark: TextView = view.findViewById(R.id.selectedMark)
    }
}
