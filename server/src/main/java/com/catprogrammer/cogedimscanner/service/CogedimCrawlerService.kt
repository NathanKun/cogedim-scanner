package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.model.SearchResult

interface CogedimCrawlerService {
    fun requestSearchResults(): List<SearchResult>
    fun parseSearchResults(results: List<SearchResult>, onlyRequestMissingBlueprintPdf: Boolean)
    fun flushPrograms()
}
