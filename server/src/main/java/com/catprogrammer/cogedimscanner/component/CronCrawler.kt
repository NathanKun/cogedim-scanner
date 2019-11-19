package com.catprogrammer.cogedimscanner.component

import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import com.catprogrammer.cogedimscanner.service.impl.CogedimCrawlerServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CronCrawler {

    private val logger = LoggerFactory.getLogger(CronCrawler::class.java)

    @Autowired
    val cogedimCrawlerService: CogedimCrawlerService? = null

    @Scheduled(cron="0 0 5 ? * *")
    fun cron() {
        logger.info("Crawler cron starts")
        val res = cogedimCrawlerService?.requestSearchResults()
        cogedimCrawlerService?.parseSearchResuls(res!!, true)
        logger.info("Crawler cron ends")
    }
}
