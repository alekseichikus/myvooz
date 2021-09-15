package ru.createtogether.myVooz.user.presentation.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoozkotlin.data.db.realmModels.AuthUserModel
import com.example.myvoozkotlin.helpers.Event
import com.example.myvoozkotlin.helpers.Utils
import com.example.myvoozkotlin.searchEmptyAuditory.model.Classroom
import com.example.myvoozkotlin.user.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val changeFullNameUseCase: ChangeFullNameUseCase,
    private val emptyAuditoryUseCase: EmptyAuditoryUseCase,
    private val changeIdGroupUserUseCase: ChangeIdGroupUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    var authUserChangeResponse = MutableLiveData<Any>()
    init {
        authUserChangeResponse = userRepository.getCurrentUserMutableLD()
        userRepository.addChangeListener()
    }

    fun setCurrentUser(authUserModel: AuthUserModel) = userRepository.setCurrentUser(authUserModel)

    fun removeCurrentUser() = userRepository.removeCurrentUser()

    val changeFullNameResponse = MutableLiveData<Event<Boolean>>()
    fun changeFullName(accessToken: String, idUser: Int, firstName: String, secondName: String) {
        viewModelScope.launch {
            changeFullNameUseCase(accessToken, idUser, firstName, secondName).collect {
                changeFullNameResponse.postValue(it)
            }
        }
    }

    val changeIdGroupUserResponse = MutableLiveData<Event<Boolean>>()
    fun changeIdGroupUser(accessToken: String, idUser: Int, nameGroup: String, idGroup: Int) {
        viewModelScope.launch {
            changeIdGroupUserUseCase(accessToken, idUser, nameGroup, idGroup).collect {
                changeIdGroupUserResponse.postValue(it)
            }
        }
    }

    val emptyClassroomResponse = MutableLiveData<Event<List<List<Classroom>>>>()
    fun getEmptyClassroom(date: String, idCorpus: Int, lowNumber: Int, upperNumber: Int, idUniversity: Int) {
        viewModelScope.launch {
            emptyAuditoryUseCase(date, idCorpus, lowNumber, upperNumber, idUniversity).collect {
                emptyClassroomResponse.postValue(it)
            }
        }
    }

    val uploadImageResponse = MutableLiveData<Event<Boolean>>()
    fun uploadImage(bitmap: Bitmap, accessToken: String, idUser: Int, type: String) {
        viewModelScope.launch {
            uploadImageResponse.postValue(Event.loading())
            Utils.uploadImage(bitmap, accessToken, idUser, type)
            uploadImageResponse.postValue(Event.success(true))
        }
    }

    fun getIdUniversity(): Int = userRepository.getIdUniversity()

    fun getNameGroup(): String = userRepository.getNameGroup()

    fun getNameUniversity(): String = userRepository.getNameUniversity()

    fun getIdGroup(): Int = userRepository.getIdGroup()

    fun getCurrentAuthUser(): AuthUserModel? = userRepository.getCurrentUser()

    override fun onCleared() {
        super.onCleared()
        userRepository.removeChangeListener()
    }
}