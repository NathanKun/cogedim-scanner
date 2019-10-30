package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CrawlerController {

    @Autowired
    val cogedimCrawlerService: CogedimCrawlerService? = null

    @GetMapping("/crawler")
    fun index() = "POST /crawler/run to run the crawler"

    @PostMapping("/crawler/run")
    fun runCrawler(): String {
        Thread {
            val res = cogedimCrawlerService?.requestSearchResults()
            cogedimCrawlerService?.parseSearchResuls(res!!, true)
        }.start()

        return "OK"
    }
}
