package com.dheril.dicodingstoryapp.ui.auth.login

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.dheril.dicodingstoryapp.R
import com.dheril.dicodingstoryapp.ViewModelFactory
import com.dheril.dicodingstoryapp.data.Result
import com.dheril.dicodingstoryapp.databinding.ActivityLoginBinding
import com.dheril.dicodingstoryapp.ui.main.MainActivity
import com.dheril.dicodingstoryapp.ui.auth.AuthViewModel
import com.dheril.dicodingstoryapp.ui.main.MainActivity.Companion.EXTRA_TOKEN

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positiveButtonTitle: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun setButtonEnable() {
        val isEmailValid =
            binding.emailEditText.text != null && binding.emailEditText.text.toString().isNotEmpty()
        val isPasswordValid =
            binding.passwordEditText.text != null && binding.passwordEditText.text.toString()
                .isNotEmpty()
        binding.loginButton.isEnabled = isEmailValid && isPasswordValid
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
        val factory = ViewModelFactory.getInstance(this)
        val viewModel: AuthViewModel by viewModels { factory }
        val context = this@LoginActivity

        with(binding) {
            loginButton.setOnClickListener {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                viewModel.login(email, password).observe(this@LoginActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            is Result.Success -> {
                                showLoading(false)
                                val token = result.data.loginResult.token
                                viewModel.saveSession(token)
                                if (!result.data.error) {
                                    title = getString(R.string.yeah)
                                    message = getString(R.string.msg_login_s)
                                    positiveButtonTitle = getString(R.string.positive_btn)
                                    showAlertDialog(context, title, message, positiveButtonTitle) {
                                        val intent = Intent(context, MainActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        viewModel.saveSession(token)
                                        intent.putExtra(EXTRA_TOKEN, token)
                                        startActivity(intent)
                                        finish()
                                    }

                                } else {
                                    title = getString(R.string.no)
                                    message = getString(R.string.msg_login_e)
                                    positiveButtonTitle = getString(R.string.positive_btn_e)
                                    showAlertDialog(context, title, message, positiveButtonTitle) {}

                                }
                            }

                            is Result.Error -> {
                                showLoading(false)
                                title = getString(R.string.no)
                                message = getString(R.string.msg_login_e)
                                positiveButtonTitle = getString(R.string.positive_btn_e)
                                showAlertDialog(context, title, message, positiveButtonTitle) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE

        } else {
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonTitle: String = "OK",
        positiveButtonAction: () -> Unit
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButtonTitle) { dialog, _ ->
                positiveButtonAction.invoke()
                dialog.dismiss()
            }
            create()
            show()
        }
    }
}