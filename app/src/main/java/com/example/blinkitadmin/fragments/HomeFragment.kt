package com.example.blinkitadmin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitadmin.R
import com.example.blinkitadmin.adapters.CategoryAdapter
import com.example.blinkitadmin.adapters.ProductsAdapter
import com.example.blinkitadmin.databinding.EditProductLayoutBinding
import com.example.blinkitadmin.databinding.FragmentHomeBinding
import com.example.blinkitadmin.models.CategoryModel
import com.example.blinkitadmin.models.ProductModel
import com.example.blinkitadmin.utils.Constants
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var productAdapter: ProductsAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAllCategories()
        showAllProducts("All")
        searchProducts()
    }

    private fun searchProducts() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                productAdapter.filter.filter(query)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun showAllCategories() {
        val categoryList = ArrayList<CategoryModel>()

        for (i in Constants.allProductsCategory.indices) {
            categoryList.add(
                CategoryModel(
                    Constants.allProductsCategory[i],
                    Constants.allProductCategoryImage[i]
                )
            )
        }
        categoryAdapter = CategoryAdapter(categoryList) {
            showAllProducts(it.category)
        }
        binding.rvProductCategory.adapter = categoryAdapter
    }

    private fun showAllProducts(category: String) {
        binding.shimmerFrameLayout.visibility = View.VISIBLE

//        if (!::productAdapter.isInitialized) {
//            productAdapter = ProductsAdapter { product ->
//                editProductInfo(product)
//            }
//            binding.rvProducts.adapter = productAdapter
//        }

        productAdapter = ProductsAdapter { product ->
            editProductInfo(product)
        }
        binding.rvProducts.adapter = productAdapter

        lifecycleScope.launch {
            adminViewModel.fetchAllProducts(category).collect {

                if (it.isEmpty()) {
                    binding.rvProducts.visibility = View.GONE
                    binding.noProductsText.visibility = View.VISIBLE
                } else {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.noProductsText.visibility = View.GONE
                }

                productAdapter.differ.submitList(it)
                productAdapter.originalList = it as ArrayList<ProductModel>
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        }
    }

    private fun editProductInfo(product: ProductModel) {
        val editProduct = EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))

        editProduct.apply {
            editProductTitle.setText(product.productTitle)
            editProductQuantity.setText(product.productQuantity?.toString())
            editProductUnit.setText(product.productUnit)
            editProductPrice.setText(product.productPrice?.toString())
            editProductStock.setText(product.productStock?.toString())
            editProductCategory.setText(product.productCategory)
            editProductType.setText(product.productType)
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(editProduct.root)
            .create()
        alertDialog.show()

        editProduct.editProductBtn.setOnClickListener {
            editProduct.apply {
                editProductTitle.isEnabled = true
                editProductQuantity.isEnabled = true
                editProductUnit.isEnabled = true
                editProductPrice.isEnabled = true
                editProductStock.isEnabled = true
                editProductCategory.isEnabled = true
                editProductType.isEnabled = true
            }
        }

        setAutoCompleteTextView(editProduct)

        editProduct.saveProductBtn.setOnClickListener {
            lifecycleScope.launch {
                product.productTitle = editProduct.editProductTitle.text.toString()
                product.productQuantity = editProduct.editProductQuantity.text.toString().toInt()
                product.productUnit = editProduct.editProductUnit.text.toString()
                product.productPrice = editProduct.editProductPrice.text.toString().toInt()
                product.productStock = editProduct.editProductStock.text.toString().toInt()
                product.productCategory = editProduct.editProductCategory.text.toString()
                product.productType = editProduct.editProductType.text.toString()
                adminViewModel.updateProductInfo(product)
            }

            Utils.showToast(requireContext(), "Saved changes...")
            alertDialog.dismiss()
        }
    }

    private fun setAutoCompleteTextView(editProduct: EditProductLayoutBinding) {
        val units = ArrayAdapter(requireContext(), R.layout.list_items, Constants.allProductsUnits)
        val category =
            ArrayAdapter(requireContext(), R.layout.list_items, Constants.allProductsCategory)
        val productType =
            ArrayAdapter(requireContext(), R.layout.list_items, Constants.allProductTypes)

        editProduct.apply {
            editProductUnit.setAdapter(units)
            editProductCategory.setAdapter(category)
            editProductType.setAdapter(productType)
        }
    }

    override fun onResume() {
        super.onResume()
        showAllProducts("All")
    }
}