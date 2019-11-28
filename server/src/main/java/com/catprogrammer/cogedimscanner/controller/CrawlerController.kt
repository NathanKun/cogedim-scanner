package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
open class CrawlerController {

    private val logger = LoggerFactory.getLogger(CrawlerController::class.java)

    @Autowired
    lateinit var cogedimCrawlerService: CogedimCrawlerService

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/crawler")
    open fun index() = "POST /crawler/run to run the crawler"

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/crawler/run")
    open fun runCrawler(): String {
        Thread {
            logger.info("Crawler cron starts")
            val res = cogedimCrawlerService.requestSearchResults()
            cogedimCrawlerService.parseSearchResults(res, true)
            logger.info("Crawler cron ends")
        }.start()

        return "OK"
    }
}
