package com.example.unifind

import android.app.Application
import com.example.unifind.data.CampusRepository
import com.example.unifind.search.SearchEngine

/**
 * OOP Concept: Singleton / Encapsulation
 * Application class to hold application-level dependencies.
 */
class UniFindApplication : Application() {

    lateinit var repository: CampusRepository
        private set

    lateinit var searchEngine: SearchEngine
        private set

    override fun onCreate() {
        super.onCreate()
        repository = CampusRepository(this)
        searchEngine = SearchEngine(repository.getAllLocations())
    }
}
