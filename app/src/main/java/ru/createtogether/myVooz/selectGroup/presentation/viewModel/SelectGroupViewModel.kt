package ru.createtogether.myVooz.selectGroup.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoozkotlin.helpers.Event
import com.example.myvoozkotlin.search.domain.SearchUniversityUseCase
import com.example.myvoozkotlin.models.SearchItem
import com.example.myvoozkotlin.search.domain.SearchCorpusUseCase
import com.example.myvoozkotlin.search.domain.SearchGroupUseCase
import com.example.myvoozkotlin.search.domain.SearchObjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectGroupViewModel @Inject constructor() : ViewModel() {
    var idUniversity:Int? = null
    var nameUniversity:String? = null
    var idGroup:Int? = null
    var nameGroup: String? = null
}