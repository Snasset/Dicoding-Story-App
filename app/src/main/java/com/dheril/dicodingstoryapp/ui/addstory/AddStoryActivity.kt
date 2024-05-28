package com.dheril.dicodingstoryapp.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.dheril.dicodingstoryapp.R
import com.dheril.dicodingstoryapp.ViewModelFactory
import com.dheril.dicodingstoryapp.data.Result
import com.dheril.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.dheril.dicodingstoryapp.getImageUri
import com.dheril.dicodingstoryapp.reduceFileImage
import com.dheril.dicodingstoryapp.ui.main.MainActivity
import com.dheril.dicodingstoryapp.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentImageUri: Uri
    private var lat = 0f
    private var lon = 0f
    private var isLocation = false
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast(getString(R.string.permission_s))

            } else {
                showToast(getString(R.string.permission_e))
            }
        }

    private val requestPermissionLocationLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    showToast(getString(R.string.permission_s))
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    showToast(getString(R.string.permission_s))
                    getMyLastLocation()
                }
                else -> {
                    showToast(getString(R.string.permission_e))
                }
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()
        setupAction()
        binding.edStoryDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun getMyLastLocation() {
        if     (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("addstoryact", "laslocation lat: ${location.latitude.toFloat()}")
                    lat = location.latitude.toFloat()
                    lon = location.longitude.toFloat()
                } else {
                    showToast("Location is not found. Try Again")
                }
            }
        } else {
            requestPermissionLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun setupAction() {
        binding.btnGallery.setOnClickListener {
            startGallery()
        }
        binding.btnCamera.setOnClickListener {
            startCamera()
        }
        binding.cbAddLocation.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                isLocation = true
            } else {
                isLocation = false
            }

        }
        binding.btnUpload.setOnClickListener {
            uploadImage(isLocation)
        }


    }

    private fun setButtonEnable() {
        val isImageValid = binding.ivPreview != null
        val isDescValid =
            binding.edStoryDesc.text != null && binding.edStoryDesc.text.toString().isNotEmpty()
        binding.btnUpload.isEnabled = isDescValid && isImageValid
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showToast(getString(R.string.no_media))
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun uploadImage(isLocation: Boolean) {
        currentImageUri.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.edStoryDesc.text.toString()
            val latString = lat.toString()
            val lonString = lon.toString()
            Log.d("addstoryact", "uploadImage lat: $lat")
            viewModel.getToken().observe(this) { token ->
                val requestBody = description.toRequestBody("text/plain".toMediaType())
                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val requestLat = latString.toRequestBody("text/plain".toMediaType())
                val requestLon = lonString.toRequestBody("text/plain".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "photo",
                    imageFile.name,
                    requestImageFile
                )
                if (token != null) {
                    if (isLocation == true) {
                        viewModel.uploadStoryWithLocation(token, multipartBody, requestBody, requestLat, requestLon)
                            .observe(this) { result ->
                                if (result != null) {
                                    when (result) {
                                        is Result.Loading -> {
                                            showLoading(true)
                                        }

                                        is Result.Success -> {
                                            showLoading(false)
                                            val intent = Intent(this, MainActivity::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                        }

                                        is Result.Error -> {
                                            showLoading(false)
                                            val errorMessage = result.error
                                            showToast(errorMessage)
                                        }

                                    }
                                }
                            }

                    } else {
                        viewModel.uploadStory(token, multipartBody, requestBody)
                            .observe(this) { result ->
                                if (result != null) {
                                    when (result) {
                                        is Result.Loading -> {
                                            showLoading(true)
                                        }

                                        is Result.Success -> {
                                            showLoading(false)
                                            val intent = Intent(this, MainActivity::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                        }

                                        is Result.Error -> {
                                            showLoading(false)
                                            val errorMessage = result.error
                                            showToast(errorMessage)
                                        }

                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun showImage() {
        currentImageUri.let {
            binding.ivPreview.setImageURI(it)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE

        } else {
            binding.progressBar.visibility = View.GONE
        }
    }


    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}