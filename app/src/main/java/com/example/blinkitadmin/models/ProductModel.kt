package com.example.blinkitadmin.models

data class ProductModel(

    val productId: String? = null,
    var productTitle: String? = null,
    var productQuantity: Int? = null,
    var productUnit: String? = null,
    var productPrice: Int? = null,
    var productStock: Int? = null,
    var productCategory: String? = null,
    var productType: String? = null,
    val itemCount: Int? = null,
    val adminUid: String? = null,
    var productImageUris: ArrayList<String?>? = null

)
