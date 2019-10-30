package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @Autowired
    val cogedimCrawlerService: CogedimCrawlerService? = null

    @GetMapping("/test")
    fun test(): String {
        Thread {
            val res = cogedimCrawlerService?.requestSearchResults()
            cogedimCrawlerService?.parseSearchResuls(res!!, true)
        }.start()

        return "OK"
    }
}
