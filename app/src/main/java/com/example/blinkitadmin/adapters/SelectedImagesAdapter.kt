package com.example.blinkitadmin.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.blinkitadmin.databinding.ItemSelectedImagesBinding

class SelectedImagesAdapter(
    private val imageUris: ArrayList<Uri>,
    private val onImageRemoved: () -> Unit
) :
    RecyclerView.Adapter<SelectedImagesAdapter.SelectedImagesViewHolder>() {
    class SelectedImagesViewHolder(val binding: ItemSelectedImagesBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedImagesViewHolder {
        val view =
            ItemSelectedImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectedImagesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    override fun onBindViewHolder(holder: SelectedImagesViewHolder, position: Int) {
        holder.binding.apply {
            val image = imageUris[position]
            itemProductImage.setImageURI(image)
        }

        holder.binding.closeImage.setOnClickListener {
            if (position < imageUris.size) {
                imageUris.removeAt(position)
                notifyItemRemoved(position)
                if (position < itemCount) {
                    notifyItemRangeChanged(position, itemCount - position)
                }
                onImageRemoved.invoke()
            }
        }
    }
}