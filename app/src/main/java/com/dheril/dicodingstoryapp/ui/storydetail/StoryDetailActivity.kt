package com.dheril.dicodingstoryapp.ui.storydetail

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.dheril.dicodingstoryapp.data.remote.response.ListStoryItem
import com.dheril.dicodingstoryapp.databinding.ActivityStoryDetailBinding

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryDetailBinding
    private  var story: ListStoryItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            story = intent.getParcelableExtra(EXTRA_STORY)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            story = intent.getParcelableExtra(EXTRA_STORY , ListStoryItem::class.java)

        }
        showDetail()
    }

    private fun showDetail(){
        binding.tvStoryName.text = story?.name
        binding.tvStoryDesc.text = story?.description
        Glide.with(this)
            .load(story?.photoUrl)
            .into(binding.ivPhoto)
    }

    companion object{
        const val EXTRA_STORY = "extra_story"
    }
}