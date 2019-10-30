package com.catprogrammer.cogedimscanner.component

import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CronCrawler {

    @Autowired
    val cogedimCrawlerService: CogedimCrawlerService? = null

    @Scheduled(cron="0 0 5 ? * *")
    fun cron() {
        val res = cogedimCrawlerService?.requestSearchResults()
        cogedimCrawlerService?.parseSearchResuls(res!!, true)
    }
}