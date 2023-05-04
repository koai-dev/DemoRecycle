package com.idance.hocnhayonline.utils
//
//import androidx.lifecycle.MutableLiveData
//import com.android.billingclient.api.BillingClient
//import com.android.billingclient.api.BillingClientStateListener
//import com.android.billingclient.api.BillingResult
//import com.android.billingclient.api.ProductDetails
//import com.android.billingclient.api.QueryProductDetailsParams
//import com.android.billingclient.api.QueryProductDetailsParams.Product
//import com.android.billingclient.api.queryProductDetails
//import com.google.common.collect.ImmutableList
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//object PurchaseUtil {
//    val liveBillingResult = MutableLiveData<BillingResult>()
//    val listProductDetail = MutableLiveData<List<ProductDetails>>()
//    fun firstSetup(billingClient: BillingClient) {
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    // The BillingClient is ready. You can query purchases here.
//                    val queryProductDetailsParams =
//                        QueryProductDetailsParams.newBuilder()
//                            .setProductList(
//                                ImmutableList.of(
//                                    QueryProductDetailsParams.Product.newBuilder()
//                                        .setProductId("com.idance.hocnhayonline.goi1thang")
//                                        .setProductType(BillingClient.ProductType.SUBS)
//                                        .build(),
//                                    QueryProductDetailsParams.Product.newBuilder()
//                                        .setProductId("com.idance.hocnhayonline.goi3thang")
//                                        .setProductType(BillingClient.ProductType.SUBS)
//                                        .build(),
//                                    QueryProductDetailsParams.Product.newBuilder()
//                                        .setProductId("com.idance.hocnhayonline.goi6thang")
//                                        .setProductType(BillingClient.ProductType.SUBS)
//                                        .build(),
//                                )
//                            )
//                            .build()
//
//                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { resultBilling,
//                                                                                        productDetailsList ->
//                        // check billingResult
//                        // process returned productDetailsList
//                        liveBillingResult.postValue(resultBilling)
//                        listProductDetail.postValue(productDetailsList)
//                    }
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                // Try to restart the connection on the next request to
//                // Google Play by calling the startConnection() method.
//            }
//        })
//    }
//
//    suspend fun processPurchases(billingClient: BillingClient) {
//        val productList = ArrayList<Product>()
//        productList.add(
//            Product.newBuilder()
//                .setProductId("product_id_example")
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
//        )
//        val params = QueryProductDetailsParams.newBuilder()
//        params.setProductList(productList)
//
//        // leverage queryProductDetails Kotlin extension function
//        val productDetailsResult = withContext(Dispatchers.IO) {
//            billingClient.queryProductDetails(params.build())
//        }
//
//        // Process the result.
//    }
//}