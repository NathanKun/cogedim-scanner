package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.ProgramDateLotDto
import com.catprogrammer.cogedimscanner.service.ProgramService
import com.fasterxml.jackson.annotation.JsonView
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import java.nio.charset.StandardCharsets

@RestController
@CacheConfig(cacheNames = ["programCache"])
@EnableScheduling
open class ProgramController {

    @Autowired
    lateinit var programService: ProgramService

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/programs")
    @JsonView(Program.SimpleView::class)
    open fun findAllGroupByProgramNumber(): List<ProgramDateLotDto> {
        return programService.findProgramsGroupByProgramNumber()
    }

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/program")
    @Cacheable(key = "#url")
    open fun fetchProgramPageHtml(url: String): String {
        return internalFetchProgramPageHtml(url);
    }

    @CachePut(key = "#url")
    open fun internalFetchProgramPageHtml(url: String): String {
        if (url.startsWith("https://www.cogedim.com/")) {
            return IOUtils.toString(
                    URL(url).openConnection().getInputStream(),
                    StandardCharsets.UTF_8
            )
        }
        return "URL must starts with https://www.cogedim.com/"
    }

    @CacheEvict(allEntries = true)
    @Scheduled(fixedDelay = (60 * 60 * 1000).toLong(), initialDelay = 1000)
    open fun reportCacheEvict() {
        println("Flush Cache")
    }
}
