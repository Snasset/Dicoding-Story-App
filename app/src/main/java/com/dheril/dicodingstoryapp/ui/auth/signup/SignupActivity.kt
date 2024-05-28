package com.dheril.dicodingstoryapp.ui.auth.signup

import android.content.Context
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
import com.dheril.dicodingstoryapp.databinding.ActivitySignupBinding
import com.dheril.dicodingstoryapp.ui.auth.AuthViewModel

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positiveButtonTitle: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()

        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

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

    private fun setupAction() {

        val factory = ViewModelFactory.getInstance(this)
        val viewModel: AuthViewModel by viewModels { factory }
        val context = this@SignupActivity

        with(binding) {
            signupButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                viewModel.signup(name, email, password).observe(this@SignupActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }

                            is Result.Success -> {
                                showLoading(false)
                                if (!result.data.error) {
                                    title = getString(R.string.yeah)
                                    message = getString(R.string.msg_signup_s)
                                    positiveButtonTitle = getString(R.string.positive_btn)
                                    showAlertDialog(context, title, message, positiveButtonTitle) {
                                        finish()
                                    }
                                } else {
                                    title = getString(R.string.no)
                                    message = getString(R.string.msg_signup_e)
                                    positiveButtonTitle = getString(R.string.positive_btn_e)
                                    showAlertDialog(context, title, message, positiveButtonTitle) {}
                                }
                            }

                            is Result.Error -> {
                                showLoading(false)
                                title = getString(R.string.no)
                                message = getString(R.string.msg_signup_e)
                                positiveButtonTitle = getString(R.string.positive_btn_e)
                                showAlertDialog(context, title, message, positiveButtonTitle) {}
                            }

                        }
                    }
                }
            }
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE

        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setButtonEnable() {
        val isNameValid =
            binding.nameEditText.text != null && binding.nameEditText.text.toString().isNotEmpty()
        val isEmailValid =
            binding.emailEditText.text != null && binding.emailEditText.text.toString().isNotEmpty()
        val isPasswordValid =
            binding.passwordEditText.text != null && binding.passwordEditText.text.toString()
                .isNotEmpty()
        binding.signupButton.isEnabled = isNameValid && isEmailValid && isPasswordValid
    }


    private fun setupView() {
        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}