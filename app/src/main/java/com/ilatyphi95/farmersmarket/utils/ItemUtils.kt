package com.ilatyphi95.farmersmarket.utils

import androidx.annotation.LayoutRes
import com.ilatyphi95.farmersmarket.BR
import com.ilatyphi95.farmersmarket.R
import com.ilatyphi95.farmersmarket.data.entities.AddItem
import com.ilatyphi95.farmersmarket.data.universaladapter.RecyclerItem

sealed class AddItemViewModel(val item: AddItem, @LayoutRes val layoutId: Int) {
    lateinit var removeItemHandler: (itemId: String) -> Unit
    lateinit var itemClickHandler: (itemId: String) -> Unit

    fun removeItem() {
        removeItemHandler(item.itemId)
    }

    fun itemClicked() {
        itemClickHandler(item.itemId)
    }
}

class PostedAdsViewModel(thisItem: AddItem) :
    AddItemViewModel(thisItem, R.layout.ads_product_item) {

}

class InterestedAdsViewModel(thisItem: AddItem) :
    AddItemViewModel(thisItem, R.layout.interested_product_item) {
}

fun AddItemViewModel.toRecyclerItem() = RecyclerItem(
    data = this,
    layoutId = layoutId,
    variableId = BR.viewModel
)