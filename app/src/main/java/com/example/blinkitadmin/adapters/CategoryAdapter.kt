package com.example.blinkitadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkitadmin.databinding.ItemProductCategoryBinding
import com.example.blinkitadmin.models.CategoryModel

class CategoryAdapter(
    private val categoryLists: ArrayList<CategoryModel>,
    private val onCategorySelected: (category: CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(val binding: ItemProductCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            ItemProductCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoryLists.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryLists[position]

        holder.binding.apply {
            categoryTitle.text = category.category
            categoryImage.setImageResource(category.icon)
        }

        holder.itemView.setOnClickListener {
            onCategorySelected(category)
        }
    }
}