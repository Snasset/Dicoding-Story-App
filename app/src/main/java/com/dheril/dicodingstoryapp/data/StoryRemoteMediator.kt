package com.dheril.dicodingstoryapp.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dheril.dicodingstoryapp.data.database.RemoteKeys
import com.dheril.dicodingstoryapp.data.database.StoryDatabase
import com.dheril.dicodingstoryapp.data.remote.response.ListStoryItem
import com.dheril.dicodingstoryapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val token: String,
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                Log.d("remote mediator", "load refresh: refresh")
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                Log.d("remote mediator", "load refresh: prepend")
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                Log.d("remote mediator", "load refresh: append")
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getStories(token, page, state.config.pageSize).execute()
            }

            if (response.isSuccessful) {
                val storyResponse = response.body()
                if (storyResponse != null && !storyResponse.error) {
                    val responseData = storyResponse.listStory
                    Log.d("remote mediator", "load data: $responseData")
                    val endOfPaginationReached = responseData.isEmpty()

                    database.withTransaction {
                        if (loadType == LoadType.REFRESH) {
                            Log.d("remote mediator", "load refresh database: $loadType")
                            database.remoteKeysDao().deleteRemoteKeys()
                            database.storyDao().deleteAll()
                        }
                        Log.d("remote mediator", "load database: $loadType")
                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1
                        val keys = responseData.map {
                            RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                        }
                        database.remoteKeysDao().insertAll(keys)
                        database.storyDao().insertStory(responseData)
                        responseData.map {
                            Log.d("remote mediator", "load: ${it.name}")
                        }
                    }
                    return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                } else {
                    Log.d("remote mediator", "API error: ${storyResponse?.message}")
                    return MediatorResult.Error(Exception("API error: ${storyResponse?.message}"))
                }
            } else {
                Log.d("remote mediator", "load failed: ${response.errorBody()}")
                return MediatorResult.Error(HttpException(response))
            }
        } catch (exception: Exception) {
            Log.d("remote mediator", "load catch: $exception")
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

}