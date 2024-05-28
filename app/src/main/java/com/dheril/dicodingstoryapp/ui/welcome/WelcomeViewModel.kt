package com.dheril.dicodingstoryapp.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dheril.dicodingstoryapp.data.UserModel
import com.dheril.dicodingstoryapp.data.UserRepository

class WelcomeViewModel(private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}