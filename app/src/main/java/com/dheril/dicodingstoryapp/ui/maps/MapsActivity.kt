package com.dheril.dicodingstoryapp.ui.maps

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.dheril.dicodingstoryapp.R
import com.dheril.dicodingstoryapp.ViewModelFactory
import com.dheril.dicodingstoryapp.data.Result
import com.dheril.dicodingstoryapp.data.remote.response.ListStoryItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dheril.dicodingstoryapp.databinding.ActivityMapsBinding
import com.dheril.dicodingstoryapp.ui.main.MainActivity
import com.google.android.gms.maps.model.MapStyleOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        setMapStyle()
        val extraToken = intent.getStringExtra(MainActivity.EXTRA_TOKEN).toString()
        viewModel.getSession().observe(this) { user ->
            var token = user.token
            if (token == null) {
                token = extraToken
            }
            viewModel.getStoriesWithLocation(token).observe(this@MapsActivity) { result ->
                when (result) {
                    is Result.Success -> {
                        val stories: List<ListStoryItem> = result.data.listStory
                        stories.forEach { data ->
                            val latLng = LatLng(data.lat, data.lon)
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(data.name)
                                    .snippet(data.description)
                            )
                        }

                    }

                    is Result.Error -> {
                        val errorMessage: String = result.error
                        showToast(errorMessage)
                    }

                    else -> {}
                }
            }
        }

    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                showToast(getString(R.string.mapStyle_e))
            }
        } catch (exception: Resources.NotFoundException) {
            showToast(exception.toString())
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }

}