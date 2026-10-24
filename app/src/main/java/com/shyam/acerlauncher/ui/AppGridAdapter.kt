package com.shyam.acerlauncher.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherApp

class AppGridAdapter(
    private val iconLoader: AppIconLoader,
    private val onLaunch: (LauncherApp) -> Unit,
    private val onLongPress: (LauncherApp) -> Unit,
    private val onBottomOverscroll: () -> Unit,
) : RecyclerView.Adapter<AppGridAdapter.Holder>() {
    private var apps: List<LauncherApp> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun submitList(value: List<LauncherApp>) {
        apps = value
        notifyDataSetChanged()
    }

    fun appAt(position: Int): LauncherApp? = apps.getOrNull(position)

    override fun getItemId(position: Int): Long = apps[position].component.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_tile, parent, false)
        FocusAnimator.attach(view, focusedScale = 1f)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = apps[position]
        holder.label.text = app.label
        holder.itemView.contentDescription = app.label
        iconLoader.load(app.componentName, holder.icon, holder.labelBlur)
        holder.itemView.setOnClickListener { onLaunch(app) }
        holder.itemView.setOnLongClickListener {
            onLongPress(app)
            true
        }
        holder.itemView.setOnKeyListener { _, keyCode, event ->
            val adapterPosition = holder.bindingAdapterPosition
            val lastRowStart = if (itemCount == 0) 0 else ((itemCount - 1) / COLUMN_COUNT) * COLUMN_COUNT
            val atBottomEdge = adapterPosition >= lastRowStart
            if (atBottomEdge && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    onBottomOverscroll()
                }
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = apps.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val labelBlur: ImageView = view.findViewById(R.id.appLabelBlur)
        val label: TextView = view.findViewById(R.id.appLabel)

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                labelBlur.setRenderEffect(
                    RenderEffect.createBlurEffect(22f, 22f, Shader.TileMode.CLAMP),
                )
            }
        }
    }

    private companion object {
        const val COLUMN_COUNT = 4
    }
}
