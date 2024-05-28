package com.dheril.dicodingstoryapp.uicomponent

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.dheril.dicodingstoryapp.R

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs){
    private var textType: TextType = TextType.PLAIN
    enum class TextType {
        PLAIN,
        EMAIL,
        PASSWORD
    }

    init{
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)
        textType = when (typedArray.getInt(R.styleable.CustomEditText_textType, 0)) {
            0 -> TextType.PLAIN
            1 -> TextType.EMAIL
            2 -> TextType.PASSWORD
            else -> TextType.PLAIN
        }
        typedArray.recycle()

        inputType = when (textType) {
            TextType.PLAIN -> InputType.TYPE_CLASS_TEXT
            TextType.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            TextType.PASSWORD -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (textType) {
                    TextType.EMAIL -> validateEmail(s.toString())
                    TextType.PASSWORD -> validatePassword(s.toString())
                    else -> {}
                }

            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun validateEmail(text: String) {
        val isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()
        error = if (!isValidEmail) {
            context.getString(R.string.email_not_valid)
        } else {
            null
        }
    }

    private fun validatePassword(text: String) {
        val isValidPassword = text.length >= 8
        error = if (!isValidPassword) {
            context.getString(R.string.pw_not_valid)
        } else {
            null
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textSize = 16f
        textAlignment = View.TEXT_ALIGNMENT_TEXT_START

    }


}