package com.example.vuforiamod.international.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.vuforiamod.R
import kotlinx.android.synthetic.main.fragment_international_image_scroll.view.*


class UniversitiesImagesAdapter (private val listImages: ArrayList<Int>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<UniversitiesImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_international_image_scroll, parent, false)

        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listImages.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.universityiIV.setImageResource(listImages[position])
    }


    class ImageViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val universityiIV: ImageView = view.imgPhotoInternationalU
    }
}