package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.model.BouyguesImmoDatalayerResult
import com.catprogrammer.cogedimscanner.model.BouyguesImmoProgramModel
import com.catprogrammer.cogedimscanner.model.BouyguesImmoSearchResult
import com.catprogrammer.cogedimscanner.service.impl.CogedimCrawlerServiceImpl
import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

@CacheConfig(cacheNames = ["bouyguesImmoCache"])
@RestController
open class BouyguesImmoController : BaseController() {

    private val logger = LoggerFactory.getLogger(BouyguesImmoController::class.java)

    private val getDetailUrl = "https://www.bouygues-immobilier.com/bi-youmap-v2/ajax/getTeaser?nid="
    private val searchUrl = "https://www.bouygues-immobilier.com/bi-youmap-v2/ajax/getFilteredPrograms?datalayer_search=%7B%2522france_items%2522%3A%2522%5B%255C%2522120%255C%2522%5D%2522%252C%2522dispositif%2522%3A%5B%5D%252C%2522typeProjet%2522%3A%2522%2522%252C%2522typeLogement%2522%3A%25221%2520et%2520%2B%2522%252C%2522choixDispositif%2522%3A%2522%2522%252C%2522lieuRecherche%2522%3A%2522Hauts-de-Seine%2522%252C%2522budgetMax%2522%3A%2522%2522%252C%2522scoreInvestisseur%2522%3A%2522%2522%7D&france_items=%5B%22120%22%5D&budget=2000%3B533000&nb_piece_prog=2"
    private val datalayerUrl = "https://www.bouygues-immobilier.com/bi-token/ajax/get-datalayer"
    private val mapUrl = "https://www.bouygues-immobilier.com/bi-kelquartier/"

    private val gson = Gson()

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/bouygues-immo/search")
    open fun search(): ResponseEntity<List<BouyguesImmoProgramModel>> {

        val programs = mutableListOf<BouyguesImmoProgramModel>()

        gson.fromJson(sendRequest(searchUrl), BouyguesImmoSearchResult::class.javaObjectType)
                .items
                .forEach { nid ->
                    /*val articleHtml = gson.fromJson(sendRequest(getDetailUrl + nid), BouyguesImmoGetDetailResult::class.javaObjectType).html
                    val article = Jsoup.parse(articleHtml)
                    val aTag = article.select("div.program-title > a")
                    val programTitle = aTag.text()
                    val programUrl = "https://www.bouygues-immobilier.com" + aTag.attr("href")
                    val programPage = Jsoup.parse(sendRequest(programUrl))
                    */

                    val dataLayer = getDatalayer(nid)
                    val mapPageStr = sendRequest(mapUrl + dataLayer.programId)
                    // search   "geoloc":{"lat":"48.9254119","lng":"2.28396629999997",
                    val regex = Regex(""""geoloc":\{"lat":"(\d{1,2}.\d{1,99})","lng":"(\d{1,2}.\d{1,99})",""")
                    val match = regex.find(mapPageStr) ?: throw Exception("latlng not found for url = $mapUrl")

                    val lat = match.groups[1].toString()
                    val lng = match.groups[2].toString()

                    val program = BouyguesImmoProgramModel(nid, dataLayer.programId, dataLayer.programName, lat, lng)
                    programs.add(program)
                }

        return programs
    }

    private fun getDatalayer(nid: String): BouyguesImmoDatalayerResult {
        val conn = CogedimCrawlerServiceImpl.applyRequestHeaders(getDeveloperFromUrl(datalayerUrl), URL(datalayerUrl).openConnection(), true) as HttpURLConnection
        conn.outputStream.use { os ->
            OutputStreamWriter(os, "UTF-8").use { osw ->
                osw.write("datalayer_page=programme&nid=$nid")
                osw.flush()
            }
        }
        conn.inputStream.use {
            val result = IOUtils.toString(GZIPInputStream(it), StandardCharsets.UTF_8)
            return gson.fromJson(result, BouyguesImmoDatalayerResult::class.javaObjectType)
        }
    }

    private fun sendRequest(url: String): String {
        val conn = CogedimCrawlerServiceImpl.applyRequestHeaders(getDeveloperFromUrl(url), URL(url).openConnection(), false) as HttpURLConnection

        return if (conn.responseCode >= 400) {
            val resp = IOUtils.toString(conn.errorStream, StandardCharsets.UTF_8)
            logger.warn(resp)
            throw Exception(resp)
        } else {
            val inputStream = if (conn.contentEncoding == "gzip") {
                GZIPInputStream(conn.inputStream)
            } else {
                conn.inputStream
            }
            IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        }
    }


    /**
     * evict cache schedule
     */
    @CacheEvict(allEntries = true)
    @Scheduled(fixedDelay = (24 * 60 * 60 * 1000).toLong(), initialDelay = 2000)
    open fun flushCacheAllResources() {
        logger.info("Flush Cache Bouygues Immo")
    }
}
