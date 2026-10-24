package com.shyam.acerlauncher.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.shyam.acerlauncher.R

data class WallpaperOption(
    val id: String,
    @StringRes val label: Int,
    @DrawableRes val image: Int,
    @DrawableRes val thumbnail: Int,
)

object WallpaperCatalog {
    val all = listOf(
        WallpaperOption("mountain", R.string.wallpaper_mountain, R.drawable.wallpaper_mountain, R.drawable.thumb_mountain),
        WallpaperOption("squirrel", R.string.wallpaper_squirrel, R.drawable.wallpaper_squirrel, R.drawable.thumb_squirrel),
        WallpaperOption("hills", R.string.wallpaper_hills, R.drawable.wallpaper_hills, R.drawable.thumb_hills),
        WallpaperOption("waterfall", R.string.wallpaper_waterfall, R.drawable.wallpaper_waterfall, R.drawable.thumb_waterfall),
        WallpaperOption("aurora", R.string.wallpaper_aurora, R.drawable.wallpaper_aurora, R.drawable.thumb_aurora),
        WallpaperOption("desert", R.string.wallpaper_desert, R.drawable.wallpaper_desert, R.drawable.thumb_desert),
    )

    fun find(id: String?): WallpaperOption = all.firstOrNull { it.id == id } ?: all.first()
}
