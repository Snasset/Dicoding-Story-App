package com.dheril.dicodingstoryapp

import com.dheril.dicodingstoryapp.data.remote.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "photo $i",
                "createdAt + $i",
                "name $i",
                "desc $i",
                i.toDouble(),
                i.toString(),
                i.toDouble()

            )
            items.add(story)
        }
        return items
    }
}