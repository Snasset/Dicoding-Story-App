package com.dheril.dicodingstoryapp.ui.addstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dheril.dicodingstoryapp.data.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repository: UserRepository) : ViewModel() {
    fun uploadStory(token: String, image: MultipartBody.Part, description: RequestBody) =
        repository.uploadStory(token, image, description)

    fun uploadStoryWithLocation(token: String, image: MultipartBody.Part, description: RequestBody, lat: RequestBody, lon: RequestBody) =
        repository.uploadStoryWithLocation(token, image, description, lat, lon)

    fun getToken(): LiveData<String?> {
        return repository.getToken()
    }
}