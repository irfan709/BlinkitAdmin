package com.example.blinkitadmin.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.blinkitadmin.models.ProductModel
import com.example.blinkitadmin.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class AdminViewModel : ViewModel() {

    private val _isImageUploaded = MutableStateFlow(false)
    val isImageUploaded: StateFlow<Boolean> = _isImageUploaded

    private val _downloadedUrls = MutableStateFlow<ArrayList<String?>>(arrayListOf())
    val downloadedUrls: StateFlow<ArrayList<String?>> = _downloadedUrls

    private val _isProductSaved = MutableStateFlow(false)
    val isProductSaved: StateFlow<Boolean> = _isProductSaved

    fun saveImagesInDb(imageUri: ArrayList<Uri>) {
        val downloadUrls = ArrayList<String?>()

        imageUri.forEach { uri ->
            val imageRef =
                FirebaseStorage.getInstance().reference.child(Utils.getCurrentUserId())
                    .child("Images")
                    .child(UUID.randomUUID().toString())
            imageRef.putFile(uri).continueWithTask {
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                val url = task.result
                downloadUrls.add(url.toString())

                if (downloadUrls.size == imageUri.size) {
                    _isImageUploaded.value = true
                    _downloadedUrls.value = downloadUrls
                }
            }
        }
    }

    fun saveProduct(productModel: ProductModel) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("All Products/${productModel.productId}").setValue(productModel)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Admins")
                    .child("Product Category/${productModel.productCategory}/${productModel.productId}")
                    .setValue(productModel)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("Admins")
                            .child("Product Type/${productModel.productType}/${productModel.productId}")
                            .setValue(productModel)
                            .addOnSuccessListener {
                                _isProductSaved.value = true
                            }
                    }
            }
    }

    fun fetchAllProducts(category: String): Flow<List<ProductModel>> = callbackFlow {
        val database = FirebaseDatabase.getInstance().getReference("Admins").child("All Products")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = ArrayList<ProductModel>()
                for (product in snapshot.children) {
                    val products = product.getValue(ProductModel::class.java)

                    if (category == "All" || products!!.productCategory == category) {
                        productsList.add(products!!)
                    }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        database.addValueEventListener(eventListener)
        awaitClose { database.removeEventListener(eventListener) }
    }

    fun updateProductInfo(product: ProductModel) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("All Products/${product.productId}").setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Category/${product.productCategory}/${product.productId}")
            .setValue(product)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("Product Type/${product.productType}/${product.productId}")
            .setValue(product)
    }
}