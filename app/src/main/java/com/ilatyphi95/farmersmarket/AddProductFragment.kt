package com.ilatyphi95.farmersmarket

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.ilatyphi95.farmersmarket.databinding.FragmentAddProductBinding
import com.ilatyphi95.farmersmarket.utils.EventObserver
import com.ilatyphi95.farmersmarket.utils.LocationProvider
import com.ilatyphi95.farmersmarket.utils.NetworkAvailabilityUtils
import com.ilatyphi95.farmersmarket.utils.SampleRepository
import org.joda.money.CurrencyUnit
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 */
class AddProductFragment : Fragment() {
    private lateinit var binding: FragmentAddProductBinding
    private val viewmodel by viewModels<AddProductViewModel> {
        AddProductViewModelFactory(SampleRepository())
    }

    private val handlePictures = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { imageList ->
        viewmodel.addImages(imageList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_product, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = viewmodel
            btSubmit.setOnClickListener {
                verifyFields()
            }
        }

        NetworkAvailabilityUtils.setNetworkAvailabilityListener(requireContext()) { isConnected ->
            if (isConnected) {
                LocationProvider(requireActivity()) { isSuccessful, address ->
                    if (isSuccessful) {
                        viewmodel.updateAddress(address)
                    }
                    //handle failure case
                }
            } else {
                Snackbar.make(requireView(), getString(R.string.no_network_connection), Snackbar.LENGTH_LONG).show()
            }
        }

        viewmodel.apply {
            events.observe(viewLifecycleOwner, EventObserver {

                finishLoading()

                when (it) {
                    Loads.ADD_PICTURES -> handlePictures.launch("image/*")

                    Loads.LOAD_CURRENCY -> (setupSpinnerDialog(R.string.search_currency)
                    { position -> viewmodel.updateCurrency(position) }).showSpinerDialog()

                    Loads.LOAD_CATEGORY -> (setupSpinnerDialog(R.string.search_currency)
                    { position -> viewmodel.selectCategory(position) }).showSpinerDialog()
                    Loads.NAVIGATE_PRODUCT -> {
                        // navigate to product page
                    }
                }
            })
        }

        initializeFields()
        return binding.root
    }

    private fun initializeFields() {
        viewmodel.setCategory(getString(R.string.select_category))
        viewmodel.currency.value = CurrencyUnit.of(Locale.getDefault()).toCurrency()
    }

    private fun verifyFields() {
        val title = binding.etName
        val desc = binding.edDesc
        val price = binding.edPrice
        val availQty = binding.qtyAvail
        val region = binding.spinnerRegion
        val category = binding.spinnerCategory

        if (title.text!!.isBlank()) title.error = getString(R.string.no_title)
        if (desc.text!!.isBlank()) desc.error = getString(R.string.no_desc)
        if (price.text!!.isBlank()) price.error = getString(R.string.no_price)
        if (availQty.text!!.isBlank()) availQty.error = getString(R.string.no_avail_specify)

        if (category.text == getString(R.string.select_category)) Snackbar
            .make(requireView(), getString(R.string.select_category), Snackbar.LENGTH_LONG).show()

        if (region.text.isNullOrEmpty()) Snackbar
            .make(requireView(), getString(R.string.turn_on_location), Snackbar.LENGTH_LONG).show()

        if (viewmodel.pictures.value?.size!! == 1) Snackbar
            .make(requireView(), getString(R.string.upload_min_1_picture), Snackbar.LENGTH_LONG)
            .show()

        if (title.text!!.isNotBlank() && desc.text!!.isNotBlank() && price.text!!.isNotBlank()
            && availQty.text!!.isNotBlank() && (category.text != getString(R.string.select_category))
            && (region.text != getString(R.string.acquiring_location)) && viewmodel.pictures.value?.size!! > 1
        )
            viewmodel.postAd()
    }

    private fun setupSpinnerDialog(
        @StringRes searchName: Int, @StringRes noList: Int = R.string.no_items,
        usePosition: (Int) -> Unit
    ): SpinnerDialog {
        val myList = viewmodel.loadedList ?: listOf(getString(noList))
        val spinnerDialog = SpinnerDialog(activity, ArrayList(myList), getString(searchName))
        spinnerDialog.setCancellable(true)
        spinnerDialog.setShowKeyboard(false)

        spinnerDialog.bindOnSpinerListener { _, position ->
            usePosition(position)
        }
        return spinnerDialog
    }
}
