package com.dheril.dicodingstoryapp.ui.main

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dheril.dicodingstoryapp.R
import com.dheril.dicodingstoryapp.ViewModelFactory
import com.dheril.dicodingstoryapp.ui.welcome.WelcomeActivity
import com.dheril.dicodingstoryapp.databinding.ActivityMainBinding
import com.dheril.dicodingstoryapp.ui.addstory.AddStoryActivity
import android.provider.Settings
import com.dheril.dicodingstoryapp.ui.LoadingStateAdapter
import com.dheril.dicodingstoryapp.ui.maps.MapsActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        getStory()
        supportActionBar?.hide()
    }

    private fun getStory(){
        val adapter = MainAdapter()
        val extraToken = intent.getStringExtra(EXTRA_TOKEN).toString()
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )


        viewModel.getSession().observe(this){user ->
            var token = user.token
            if (token == null){
                token = extraToken
            }
            viewModel.getStory(token).observe(this, {
                adapter.submitData(lifecycle, it)
            })
        }
    }

    private fun setupAction() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    viewModel.logout()
                    true

                }

                R.id.menu_language -> {
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }

                R.id.menu_map -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    val extraToken = intent.getStringExtra(EXTRA_TOKEN).toString()
                    intent.putExtra(MapsActivity.EXTRA_TOKEN, extraToken)
                    startActivity(intent)
                    true
                }


                else -> false
            }
        }
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }


    override fun onResume() {
        super.onResume()
        getStory()
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }
}