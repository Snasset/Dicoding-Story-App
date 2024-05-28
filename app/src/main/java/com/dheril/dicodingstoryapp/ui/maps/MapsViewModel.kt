package com.dheril.dicodingstoryapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dheril.dicodingstoryapp.data.UserModel
import com.dheril.dicodingstoryapp.data.UserRepository

class MapsViewModel(private val repository: UserRepository): ViewModel() {

    fun getStoriesWithLocation(token: String) = repository.getStoriesWithLocation(token)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}