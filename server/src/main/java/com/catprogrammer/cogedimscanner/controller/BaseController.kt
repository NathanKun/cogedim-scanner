package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.model.RealEstateDeveloper

open class BaseController {

    protected fun getDeveloperFromUrl(url: String): RealEstateDeveloper {
        return when {
            url.contains("cogedim.com", ignoreCase = true) -> {
                RealEstateDeveloper.COGEDIM
            }
            url.contains("kaufmanbroad", ignoreCase = true) -> {
                RealEstateDeveloper.KAUFMANBROAD
            }
            url.contains("bouygues", ignoreCase = true) -> {
                RealEstateDeveloper.BOUYGUESIMMO
            }
            else -> {
                throw Exception("url does not belongs to any developer. Url = $url")
            }
        }
    }
}
