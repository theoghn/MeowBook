package com.theo.meowbook.ui.details

import com.theo.meowbook.model.CatDetails
import com.theo.meowbook.service.CatService
import com.theo.meowbook.utils.BaseViewModel
import com.theo.meowbook.utils.dataFlowOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CatDetailsViewModel @Inject constructor(
    private val catService: CatService,
) : BaseViewModel<Unit>() {
    val catDetailsFLow = dataFlowOf<CatDetails>()

    fun loadCatDetails(id: String) {
        catDetailsFLow.execute {
            val cat = catService.getCatDetails(id)
            setValue(cat)
        }
    }
}