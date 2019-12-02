package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.entity.Decision
import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.ProgramDateLotDto
import com.catprogrammer.cogedimscanner.model.RealEstateDeveloper
import com.catprogrammer.cogedimscanner.service.LotService
import com.catprogrammer.cogedimscanner.service.ProgramService
import com.catprogrammer.cogedimscanner.service.impl.CogedimCrawlerServiceImpl.Companion.applyRequestHeaders
import com.catprogrammer.cogedimscanner.utils.JwtTokenUtil
import com.fasterxml.jackson.annotation.JsonView
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.bind.annotation.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import javax.servlet.http.HttpServletRequest


@RestController
@CacheConfig(cacheNames = ["programCache"])
@EnableScheduling
open class ProgramController {

    private val logger = LoggerFactory.getLogger(ProgramController::class.java)

    private var lastFetchAt: Long = 0L

    private val lock = Any()

    @Autowired
    private lateinit var programService: ProgramService

    @Autowired
    private lateinit var lotService: LotService

    @Autowired
    private lateinit var userDetailsService: UserDetailsService

    @Autowired
    private lateinit var jwtTokenUtil: JwtTokenUtil


    /**
     * /programs
     */

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/programs")
    @JsonView(Program.SimpleView::class)
    @Cacheable(cacheNames = ["programs"], key = "'programsCacheKey'")
    open fun findAllGroupByProgramNumber(): List<ProgramDateLotDto> {
        return internalFindAllGroupByProgramNumber()
    }

    @CachePut(cacheNames = ["programs"], key = "'programsCacheKey'")
    open fun internalFindAllGroupByProgramNumber(): List<ProgramDateLotDto> {
        return programService.findProgramsGroupByProgramNumber()
    }

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/flushPrograms")
    @CacheEvict(cacheNames = ["programs"], key = "'programsCacheKey'")
    open fun flushCachePrograms() {
        logger.info("Flush Cache /programs")
    }

    @PostMapping("/internalFlushPrograms")
    @CacheEvict(cacheNames = ["programs"], key = "'programsCacheKey'")
    open fun internalFlushCachePrograms(request: HttpServletRequest): String {
        logger.info("Internal flush Cache /programs")
        return "Internal flush Cache /programs OK"
    }


    /**
     * update lot
     */

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PutMapping("/program/{developer}/{programNumber}/lot/{lotNumber}")
    @CacheEvict(cacheNames = ["programs"], key = "'programsCacheKey'")
    open fun setLotProperty(@PathVariable developer: RealEstateDeveloper,
                            @PathVariable programNumber: String,
                            @PathVariable lotNumber: String,
                            @RequestParam(value = "remark", required = false) remark: String?,
                            @RequestParam(value = "decision", required = false) decision: String?) {
        val lots = lotService.findAllByDeveloperAndProgramNumberAndLotNumber(developer, programNumber, lotNumber)
        lots.forEach { lot ->
            if (remark != null) {
                lot.remark = remark
            }
            if (decision != null) {
                try {
                    lot.decision = Decision.valueOf(decision)
                } catch (e: IllegalArgumentException) {
                    logger.warn("parameter decision = '$decision' is not a valid value of enum Decision")
                }
            }
            lotService.save(lot)
        }
    }


    /**
     * /program, /resource
     */

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/program")
    @ResponseBody
    @Cacheable(key = "#url")
    open fun fetchProgramPageHtml(url: String): ResponseEntity<String> {
        return internalFetchProgramPageHtml(url)
    }

    @GetMapping("/resource")
    @Cacheable(key = "#resourceUrl")
    open fun fetchResource(resourceUrl: String?, token: String?, request: HttpServletRequest): ResponseEntity<ByteArray> {
        var auth = false

        if (token != null) {
            val username = jwtTokenUtil.getUsernameFromToken(token)
            if (username != null) {
                val userDetails = this.userDetailsService.loadUserByUsername(username)
                if (jwtTokenUtil.validateToken(token, userDetails)!!) {
                    val authentication = UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(
                            request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }

            if (SecurityContextHolder.getContext().authentication.authorities.toTypedArray().any {
                        it.authority == "WRITE_PRIVILEGE"
                    }) {
                auth = true
            }
        }

        return if (resourceUrl != null) {
            if ((resourceUrl.startsWith("https://www.cogedim.com/marker/") && auth) || // big map pin detail
                    (resourceUrl.startsWith("https://www.cogedim.com/sites/") && auth) || // program image
                    (resourceUrl.startsWith("https://www.cogedim.com/themes/") &&
                            (resourceUrl.endsWith(".png") ||
                                    resourceUrl.endsWith("jpg") ||
                                    resourceUrl.endsWith("jpeg"))) // images for css
            ) {
                internalFetchResource(resourceUrl)
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/flush")
    @CacheEvict(key = "#url", beforeInvocation = true)
    open fun flushUrl(url: String): String {
        logger.info("Flushed url $url")
        return "OK"
    }

    @Suppress("SpringElInspection")
    @CachePut(key = "#url", unless = "#result.statusCode != 200")
    open fun internalFetchProgramPageHtml(url: String): ResponseEntity<String> {
        return if (url.startsWith("https://www.cogedim.com/")) {
            // avoid requesting cogedim's server concurrently
            synchronized(lock) {
                val lastFetchDiff = System.currentTimeMillis() - lastFetchAt
                if (lastFetchDiff < 5000) {
                    Thread.sleep(5000 - lastFetchDiff)
                }
                lastFetchAt = System.currentTimeMillis()

                val urlEncoded = encodeUrl(url)
                logger.info("requesting $urlEncoded")
                val conn = applyRequestHeaders(getDeveloperFromUrl(url), URL(urlEncoded).openConnection(), false)
                val str = IOUtils.toString(GZIPInputStream(conn.getInputStream()), StandardCharsets.UTF_8)
                ResponseEntity
                        .ok()
                        .contentType(MediaType.parseMediaType(conn.contentType))
                        .body(str)
            }
        } else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    @Suppress("SpringElInspection")
    @CachePut(key = "#resourceUrl", unless = "#result.statusCode != 200")
    open fun internalFetchResource(resourceUrl: String): ResponseEntity<ByteArray> {
        val url = encodeUrl(resourceUrl)
        val conn = applyRequestHeaders(getDeveloperFromUrl(url), URL(url).openConnection(), false) as HttpURLConnection

        return if (conn.responseCode >= 400) {
            val resp = IOUtils.toString(conn.errorStream, StandardCharsets.UTF_8)
            logger.error("internalFetchResource error\nurl = ${conn.url}\ncode = ${conn.responseCode}\n$resp")
            ResponseEntity(HttpStatus.valueOf(conn.responseCode))
        } else {
            val inputStream = if (conn.contentEncoding == "gzip") {
                GZIPInputStream(conn.inputStream)
            } else {
                conn.inputStream
            }
            ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(conn.contentType))
                    .body(IOUtils.toByteArray(inputStream))
        }
    }


    /**
     * evict cache schedule
     */
    @CacheEvict(allEntries = true)
    @Scheduled(fixedDelay = (7 * 24 * 60 * 60 * 1000).toLong(), initialDelay = 2000)
    open fun flushCacheAllResources() {
        logger.info("Flush Cache Resources")
    }

    private fun encodeUrl(url: String): String {
        return "https://" + url
                .replace("https://", "") // avoid : being encoded
                .split("/")
                .joinToString("/") {
                    URLEncoder.encode(it, "UTF-8")
                }
    }

    private fun getDeveloperFromUrl(url: String): RealEstateDeveloper {
        return when {
            url.contains("cogedim.com", ignoreCase = true) -> {
                RealEstateDeveloper.COGEDIM
            }
            url.contains("kaufmanbroad", ignoreCase = true) -> {
                RealEstateDeveloper.KAUFMANBROAD
            }
            else -> {
                throw Exception("url does not belongs to any developer. Url = $url")
            }
        }
    }
}
