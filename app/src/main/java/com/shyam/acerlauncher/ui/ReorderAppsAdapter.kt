package com.shyam.acerlauncher.ui

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherApp
import java.text.NumberFormat

class ReorderAppsAdapter(
    private val iconLoader: AppIconLoader,
    private val onToggleMoving: (Int) -> Unit,
    private val onKey: (View, Int, KeyEvent, Int) -> Boolean,
) : RecyclerView.Adapter<ReorderAppsAdapter.Holder>() {
    private val apps = mutableListOf<LauncherApp>()
    var movingPosition: Int = RecyclerView.NO_POSITION
        private set

    init {
        setHasStableIds(true)
    }

    fun submitList(value: List<LauncherApp>) {
        apps.clear()
        apps.addAll(value)
        movingPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun components(): List<String> = apps.map(LauncherApp::component)

    fun toggleMoving(position: Int) {
        val old = movingPosition
        movingPosition = if (old == position) RecyclerView.NO_POSITION else position
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        if (movingPosition != RecyclerView.NO_POSITION) notifyItemChanged(movingPosition)
    }

    fun move(from: Int, to: Int) {
        if (from !in apps.indices || to !in apps.indices) return
        apps.add(to, apps.removeAt(from))
        movingPosition = to
        notifyItemMoved(from, to)
        notifyItemRangeChanged(minOf(from, to), kotlin.math.abs(from - to) + 1)
    }

    override fun getItemId(position: Int): Long = apps[position].component.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reorder_app, parent, false)
        FocusAnimator.attach(view)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = apps[position]
        holder.number.text = NumberFormat.getIntegerInstance().format(position + 1)
        holder.label.text = app.label
        holder.moving.visibility = if (position == movingPosition) View.VISIBLE else View.GONE
        iconLoader.load(app.componentName, holder.icon)
        holder.itemView.setOnClickListener {
            val current = holder.bindingAdapterPosition
            if (current != RecyclerView.NO_POSITION) onToggleMoving(current)
        }
        holder.itemView.setOnKeyListener { view, keyCode, event ->
            val current = holder.bindingAdapterPosition
            current != RecyclerView.NO_POSITION && onKey(view, keyCode, event, current)
        }
    }

    override fun getItemCount(): Int = apps.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.orderNumber)
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
        val moving: TextView = view.findViewById(R.id.movingLabel)
    }
}
