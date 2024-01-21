package com.example.blinkitadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkitadmin.databinding.ItemProductsBinding
import com.example.blinkitadmin.models.ProductModel
import com.example.blinkitadmin.utils.FilterProducts

class ProductsAdapter(
    private val onCategorySelected: (product: ProductModel) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>(), Filterable {
    class ProductsViewHolder(val binding: ItemProductsBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffutil = object : DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffutil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val view = ItemProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            val imagesList = ArrayList<SlideModel>()
            val productImage = product.productImageUris

            for (i in 0 until productImage?.size!!) {
                imagesList.add(SlideModel(product.productImageUris!![i].toString()))
            }

            productImages.setImageList(imagesList)

            productTitle.text = product.productTitle
            val quantity = product.productQuantity.toString() + product.productUnit
            productQuantity.text = quantity
            productPrice.text = "₹" + product.productPrice.toString()
        }

        holder.itemView.setOnClickListener {
            onCategorySelected(product)
        }
    }

    val filteredList: FilterProducts? = null
    var originalList = ArrayList<ProductModel>()
    override fun getFilter(): Filter {
        if (filteredList == null) {
            return FilterProducts(this, originalList)
        }
        return filteredList
    }
}