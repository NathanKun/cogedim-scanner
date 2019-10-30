package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.model.SearchResult

interface CogedimCrawlerService {
    fun requestSearchResults(): List<SearchResult>
    fun parseSearchResuls(results: List<SearchResult>)
}