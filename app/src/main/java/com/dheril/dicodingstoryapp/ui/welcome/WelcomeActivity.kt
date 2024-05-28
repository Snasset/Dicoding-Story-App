package com.dheril.dicodingstoryapp.ui.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import com.dheril.dicodingstoryapp.ViewModelFactory
import com.dheril.dicodingstoryapp.databinding.ActivityWelcomeBinding
import com.dheril.dicodingstoryapp.ui.main.MainActivity
import com.dheril.dicodingstoryapp.ui.auth.login.LoginActivity
import com.dheril.dicodingstoryapp.ui.auth.signup.SignupActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private val viewModel by viewModels<WelcomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        setContentView(binding.root)
        setupView()
        setupAction()
        playAnimation()

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
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
        with(binding) {
            val login = ObjectAnimator.ofFloat(loginButton, View.ALPHA, 1f).setDuration(600)
            val signup = ObjectAnimator.ofFloat(signupButton, View.ALPHA, 1f).setDuration(600)
            val title = ObjectAnimator.ofFloat(titleTextView, View.ALPHA, 1f).setDuration(600)
            val desc = ObjectAnimator.ofFloat(descTextView, View.ALPHA, 1f).setDuration(600)
            val together = AnimatorSet().apply {
                playTogether(login, signup)
            }
            AnimatorSet().apply {
                playSequentially(title, desc, together)
                start()
            }
        }

    }

}