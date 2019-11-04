package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
open class CrawlerController {

    @Autowired
    lateinit var cogedimCrawlerService: CogedimCrawlerService

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/crawler")
    open fun index() = "POST /crawler/run to run the crawler"

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/crawler/run")
    open fun runCrawler(): String {
        Thread {
            val res = cogedimCrawlerService.requestSearchResults()
            cogedimCrawlerService.parseSearchResuls(res, true)
        }.start()

        return "OK"
    }
}
