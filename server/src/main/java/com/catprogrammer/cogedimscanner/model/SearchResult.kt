package com.catprogrammer.cogedimscanner.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class SearchResult(
        val results: JsonArray?,
        val hasMore: Boolean?,
        val drupalSettings: JsonObject?,
        var developer: RealEstateDeveloper?
)
