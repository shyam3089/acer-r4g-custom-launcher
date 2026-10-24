package com.shyam.acerlauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherApp

class ManageAppsAdapter(
    private val iconLoader: AppIconLoader,
    private val onToggle: (LauncherApp) -> Boolean,
) : RecyclerView.Adapter<ManageAppsAdapter.Holder>() {
    private var apps: List<LauncherApp> = emptyList()
    private val selected = mutableSetOf<String>()

    init {
        setHasStableIds(true)
    }

    fun submitList(value: List<LauncherApp>, pinned: Collection<String>) {
        apps = value
        selected.clear()
        selected.addAll(pinned)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = apps[position].component.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_app, parent, false)
        FocusAnimator.attach(view)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = apps[position]
        holder.label.text = app.label
        iconLoader.load(app.componentName, holder.icon)
        renderSelection(holder, app.component in selected)
        holder.itemView.setOnClickListener {
            val isSelected = onToggle(app)
            if (isSelected) selected.add(app.component) else selected.remove(app.component)
            renderSelection(holder, isSelected)
        }
    }

    private fun renderSelection(holder: Holder, isSelected: Boolean) {
        holder.check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        holder.itemView.contentDescription = holder.itemView.context.getString(
            if (isSelected) R.string.selected else R.string.not_selected
        ) + ", " + holder.label.text
    }

    override fun getItemCount(): Int = apps.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
        val check: ImageView = view.findViewById(R.id.checkIcon)
    }
}
