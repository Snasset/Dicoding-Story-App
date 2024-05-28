package com.dheril.dicodingstoryapp.data


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import com.dheril.dicodingstoryapp.data.database.StoryDatabase
import com.dheril.dicodingstoryapp.data.remote.response.ListStoryItem
import com.dheril.dicodingstoryapp.data.remote.response.LoginResponse
import com.dheril.dicodingstoryapp.data.remote.response.SignupResponse
import com.dheril.dicodingstoryapp.data.remote.response.StoryResponse
import com.dheril.dicodingstoryapp.data.remote.response.UploadResponse
import com.dheril.dicodingstoryapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {

    suspend fun saveSession(token: String) {
        userPreference.saveSession(token)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun getToken(): LiveData<String?> {
        return userPreference.getToken().asLiveData()
    }

    private val resultSignup = MediatorLiveData<Result<SignupResponse>>()
    private val resultLogin = MediatorLiveData<Result<LoginResponse>>()
    private val resultStory = MediatorLiveData<Result<StoryResponse>>()
    private val resultUpload = MediatorLiveData<Result<UploadResponse>>()

    fun getStoriesWithLocation(token: String): LiveData<Result<StoryResponse>> {
        resultStory.value = Result.Loading
        userPreference.getSession()
        val bearerToken = "Bearer $token"
        Log.d("repo loca", "getStoriesWithLocation: $bearerToken")
        val client = apiService.getStoriesWithLocation(bearerToken)
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        resultStory.value = Result.Success(res)
                    }
                } else {
                    resultStory.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                resultStory.value = Result.Error("${t.message}")
            }
        })
        return resultStory
    }


    fun signup(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<SignupResponse>> {
        resultSignup.value = Result.Loading
        val client = apiService.signup(name, email, password)
        client.enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        resultSignup.value = Result.Success(res)
                    }
                } else {
                    resultSignup.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                resultSignup.value = Result.Error("${t.message}")
            }
        })
        return resultSignup
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> {
        resultLogin.value = Result.Loading
        val client = apiService.login(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>

            ) {
                val res = response.body()
                CoroutineScope(Dispatchers.Main).launch {
                    if (response.isSuccessful) {
                        if (res != null) {
                            saveSession(res.loginResult.token)
                            resultLogin.value = Result.Success(res)
                        }
                    } else {
                        resultLogin.value = Result.Error("${res?.message}")
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                resultLogin.value = Result.Error("${t.message}")
            }
        })
        return resultLogin
    }

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        val bearerToken = "Bearer $token"
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, bearerToken),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    fun uploadStory(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody
    ): LiveData<Result<UploadResponse>> {
        resultUpload.value = Result.Loading
        val bearerToken = "Bearer $token"
        val client = apiService.uploadStory(bearerToken, image, description)
        client.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        resultUpload.value = Result.Success(res)
                    }
                } else {
                    resultUpload.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                resultUpload.value = Result.Error("${t.message}")
            }
        })
        return resultUpload
    }

    fun uploadStoryWithLocation(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ): LiveData<Result<UploadResponse>> {
        resultUpload.value = Result.Loading
        val bearerToken = "Bearer $token"
        val client = apiService.uploadStoryWithLocation(bearerToken, image, description, lat, lon)
        client.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        resultUpload.value = Result.Success(res)
                    }
                } else {
                    resultUpload.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                resultUpload.value = Result.Error("${t.message}")
            }
        })
        return resultUpload
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService, storyDatabase)
            }.also { instance = it }
    }
}