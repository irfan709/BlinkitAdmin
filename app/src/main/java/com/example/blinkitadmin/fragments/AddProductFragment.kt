package com.example.blinkitadmin.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitadmin.AdminActivity
import com.example.blinkitadmin.R
import com.example.blinkitadmin.adapters.SelectedImagesAdapter
import com.example.blinkitadmin.databinding.FragmentAddProductBinding
import com.example.blinkitadmin.models.ProductModel
import com.example.blinkitadmin.utils.Constants
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private val imageUris: ArrayList<Uri> = arrayListOf()
    private val adminViewModel: AdminViewModel by viewModels()
    private val selectedImage =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { listOfUris ->
            val images = listOfUris.take(5)
            imageUris.clear()
            imageUris.addAll(images)

            val adapter = SelectedImagesAdapter(imageUris) {
                updateRecyclerViewVisibility()
            }

            binding.productImagesRv.adapter = adapter
            updateRecyclerViewVisibility()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pickImageBtn.setOnClickListener {
            selectedImage.launch("image/*")
        }

        binding.pickImageLayout.setOnClickListener {
            selectedImage.launch("image/*")
        }

        binding.addProductBtn.setOnClickListener {
            Utils.showDialog(requireContext(), "Uploading images...")
            val productTitle = binding.etProductTitle.text.toString()
            val productQuantity = binding.etProductQuantity.text.toString()
            val productUnit = binding.etProductUnit.text.toString()
            val productPrice = binding.etProductPrice.text.toString()
            val productStock = binding.etProductStock.text.toString()
            val productCategory = binding.etProductCategory.text.toString()
            val productType = binding.etProductType.text.toString()

            if (productTitle.isEmpty() || productQuantity.isEmpty() || productUnit.isEmpty() ||
                productPrice.isEmpty() || productStock.isEmpty() || productCategory.isEmpty() || productTitle.isEmpty()
            ) {
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(), "All fields are required")
                }
            } else if (imageUris.isEmpty()) {
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(), "Please upload product images...")
                }
            } else {
                val product = ProductModel(
                    productTitle = productTitle,
                    productQuantity = productQuantity.toInt(),
                    productUnit = productUnit,
                    productPrice = productPrice.toInt(),
                    productStock = productStock.toInt(),
                    productCategory = productCategory,
                    productType = productType,
                    itemCount = 0,
                    adminUid = Utils.getCurrentUserId(),
                    productId = Utils.getRandomId()
                )

                saveImage(product)
            }
        }

        setUpAutoCompleteTextView()
        updateRecyclerViewVisibility()
    }

    private fun saveImage(product: ProductModel) {
        adminViewModel.apply {
            saveImagesInDb(imageUris)
            lifecycleScope.launch {
                isImageUploaded.collect {
                    if (it) {
                        Utils.apply {
                            hideDialog()
                            showToast(requireContext(), "Images uploaded...")
                        }
                        getUris(product)
                    }
                }
            }
        }
    }

    private fun getUris(product: ProductModel) {
        Utils.showDialog(requireContext(), "Adding Product...")

        lifecycleScope.launch {
            adminViewModel.downloadedUrls.collect {
                val urls = it
                product.productImageUris = urls
                saveProduct(product)
            }
        }
    }

    private fun saveProduct(product: ProductModel) {
        adminViewModel.apply {
            saveProduct(product)
            lifecycleScope.launch {
                isProductSaved.collect {
                    if (it) {
                        Utils.hideDialog()
                        startActivity(Intent(requireActivity(), AdminActivity::class.java))
                        Utils.showToast(requireContext(), "Product is added...")
                    }
                }
            }
        }
    }

    private fun setUpAutoCompleteTextView() {
        val units =
            ArrayAdapter(requireContext(), R.layout.list_items, Constants.allProductsUnits)
        val category =
            ArrayAdapter(requireContext(), R.layout.list_items, Constants.allProductsCategory)
        val productType =
            ArrayAdapter(requireContext(), R.layout.list_items, Constants.allProductTypes)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
    }

    private fun updateRecyclerViewVisibility() {
        binding.productImagesRv.visibility = if (imageUris.isEmpty()) View.GONE else View.VISIBLE
    }
}