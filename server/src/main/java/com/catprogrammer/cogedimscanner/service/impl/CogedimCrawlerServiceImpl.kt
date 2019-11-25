package com.catprogrammer.cogedimscanner.service.impl

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.FormGetResult
import com.catprogrammer.cogedimscanner.model.NearbyProgram
import com.catprogrammer.cogedimscanner.model.SearchResult
import com.catprogrammer.cogedimscanner.repository.LotRepository
import com.catprogrammer.cogedimscanner.repository.ProgramRepository
import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream


@Service
class CogedimCrawlerServiceImpl : CogedimCrawlerService {

    private val logger = LoggerFactory.getLogger(CogedimCrawlerServiceImpl::class.java)

    private val gson = Gson()
    private val type: Type = object : TypeToken<List<NearbyProgram>>() {}.type
    private val baseurl = "https://www.cogedim.com"
    private val contactinfo = "contact_info_known=true&contact_info%5BformCivility%5D=02&contact_info%5BformFirstName%5D=Not&contact_info%5BformLastName%5D=APerson&contact_info%5BformPhone%5D=0660600660&contact_info%5BformEmail%5D=notanemail%40gmail.com&contact_info%5BformLocation%5D=Paris&contact_info%5BformCity%5D=Paris&contact_info%5BformPostalCode%5D=75000&contact_info%5BformRegion%5D=%C3%8Ele-de-France&contact_info%5BformCountry%5D=France&contact_info%5BformDestination%5D=habiter"

    @Autowired
    private lateinit var programRepository: ProgramRepository
    @Autowired
    private lateinit var lotRepository: LotRepository

    /**
     * Send one or multiple requests to fetch all program from Cogedim.
     * Return a list of SearchResult object.
     */
    override fun requestSearchResults(): List<SearchResult> {
        val crawlData = arrayOf(
                "location=Hauts-de-Seine&department=Hauts-de-Seine&rooms=2,3,4,5", // 92
                "location=ile-de-france&department=Paris&rooms=3,4,5", // Paris
                "location=Le Vésinet&city=Le Vésinet&department=Yvelines&region=Île-de-France&rooms=2,3,4,5" // L’ Accord Parfait - 78 Le Vésinet
        )
        val results = mutableListOf<SearchResult>()
        var page = 0
        var res: SearchResult?

        for (data in crawlData) {
            logger.info("Crawling $data")
            do {
                res = fetchSearchResult(page++, data)
                if (res != null) {
                    results.add(res)
                }
            } while (res?.hasMore != null && res.hasMore!!)
        }

        return results
        // return mutableListOf(gson.fromJson<SearchResult>(mokeSearchResult, SearchResult::class.javaObjectType))
    }

    /**
     * Parse a list of SearchResult object.
     * Find and save all programs and lots.
     */
    override fun parseSearchResuls(results: List<SearchResult>, onlyRequestMissingBlueprintPdf: Boolean) {
        results.filter { it.results != null && it.results.size() > 0 }.forEach { searchResult ->
            val drupalSettings = searchResult.drupalSettings
            val nearbyPrograms: List<NearbyProgram> =
                    if (drupalSettings != null && drupalSettings.has("nearbyPrograms")) {
                        val nearbyProgramsJsonObject = drupalSettings.get("nearbyPrograms")
                        gson.fromJson<List<NearbyProgram>>(nearbyProgramsJsonObject, type)
                    } else {
                        emptyList()
                    }

            searchResult.results?.forEach { result ->
                val article = Jsoup.parse(result.asString)
                // val article = Jsoup.parse(mokeArticle)
                val program = parseSearchResultProgram(article, nearbyPrograms)
                parseSearchResultLot(article, program, onlyRequestMissingBlueprintPdf)
                programRepository.save(program)
                logger.info("saved program ${program.programName} ${program.programNumber}")
            }
        }

    }

    /**
     * Parse the <article> dom object, find all lot and link to the given Program object and save
     */
    private fun parseSearchResultLot(article: Document, program: Program, onlyRequestMissingBlueprintPdf: Boolean) {
        article.select("v-expansion-panel[class^=regulation-] > *").forEach { programTypeTag ->
            // for each lot type. eg: 3 pièces, 4 pièces, etc
            programTypeTag.select("v-expansion-panel v-expansion-panel-content[ripple]").forEach { lotTag ->
                // for each lot
                val lotNumber = lotTag.select("span.lot-lot_number").text()
                val surface = lotTag.select("span.lot-surface").text()
                val floor = lotTag.select("span.lot-floor").text()
                val price = lotTag.select("span.lot-price").text()

                val blueprintDownloadButton = lotTag.select("div.lot-details div.buttons v-btn[@click.native*=blueprint]")
                val blueprintDownloadButtonAttr = blueprintDownloadButton.attr("@click.native")
                val blueprintId = Regex("event, ?[0-9]{3,4}, ?([0-9]{4,5})").find(blueprintDownloadButtonAttr)?.groups?.get(1)?.value

                // no need to request blueprint pdf if no blueprint id
                var requestPdf = blueprintId != null
                var oldPdfUrl: String? = null
                // check if already request pdf of this blueprint
                if (requestPdf) {
                    val sameLots = lotRepository.findByBlueprintIdOrderByIdDesc(blueprintId!!)
                    if (sameLots.isNotEmpty()) {
                        val sameLot = sameLots.first()
                        if (sameLot.blueprintId != null) {
                            // if already have the pdf url, not requesting it again if onlyRequestMissingBlueprintPdf is true
                            requestPdf = !onlyRequestMissingBlueprintPdf
                            oldPdfUrl = sameLot.pdfUrl
                        }
                    }
                }

                val pdfUrl =
                        if (requestPdf) {
                            if (blueprintId != null && blueprintId.length == 5) {
                                // pause 10 sec to reduce request per sec
                                // avoid being banned
                                Thread.sleep(10000)

                                val blueprintUrl = fetchFormBlueprint(program.programNumber, blueprintId)
                                if (blueprintUrl != null) {
                                    baseurl + parseFormGetResultGetPdfUrl(blueprintUrl)
                                } else null
                            } else null
                        } else {
                            oldPdfUrl
                        }

                val lot = Lot(null, lotNumber, surface, floor, price, blueprintId, pdfUrl, null, null)
                program.lots.add(lot)
                lotRepository.save(lot)
                logger.info("saved lot ${lot.lotNumber}")
            }
        }
    }

    /**
     * Parse the <article> dom object, find it's program, instantiate, save and return the Program object
     */
    private fun parseSearchResultProgram(article: Document, nearbyPrograms: List<NearbyProgram>): Program {
        val programName = article.select("div.info-box h2 span").text()
        val programId = article.select("article[is=program-card-std]").attr("class")
                .split(" ")
                .first { s -> s.startsWith("program-") }
                .replace("program-", "")
        var postalCode = article.select("span[itemprop=postalCode]").text()
        var address = article.select("span[itemprop=addressLocality]").text()

        if (postalCode == "") {
            val regexPostalCode = Regex("[0-9]{5}").find(address)?.value
            if (regexPostalCode != null) {
                postalCode = regexPostalCode
                address = address.replace(regexPostalCode, "").replace(" - ", "")
            }
        }

        val url = baseurl + article.select("div.visual .gradient a.more-link").attr("href")
        val imgUrl = baseurl + article.select("div.visual img").attr("src")
                .replace("/styles/visual_327x188/public", "") // remove resize
                .replace(Regex("\\?itok.*"), "") // remove param

        val leafletForm = fetchFormLeaflet(programId)
        val pdfUrl = if (leafletForm != null) (baseurl + parseFormGetResultGetPdfUrl(leafletForm)) else null

        val nearbyProgram = nearbyPrograms.first { p -> p.nid == programId }
        val latitude = nearbyProgram.lat
        val longitude = nearbyProgram.lng

        val program = Program(null, programName, programId, postalCode, address, url, imgUrl, pdfUrl,
                latitude, longitude, mutableListOf(), null, null)
        programRepository.save(program)

        return program
    }

    private fun parseFormGetResultGetPdfUrl(result: FormGetResult): String {
        return Jsoup.parse(result.form).select("div.confirmation a").attr("href")
    }

    private fun fetchSearchResult(page: Int, data: String): SearchResult? {
        return postRequest(
                "https://www.cogedim.com/search-results?page=$page",
                data,
                SearchResult::class.javaObjectType
        )
    }

    private fun fetchFormBlueprint(programNumber: String, lotNumber: String): FormGetResult? {
        return postRequest(
                "https://www.cogedim.com/form-get",
                "form=re_forms_blueprint&program_nid=$programNumber&lot_id=$lotNumber&$contactinfo",
                FormGetResult::class.javaObjectType
        )
    }

    private fun fetchFormLeaflet(programNumber: String): FormGetResult? {
        return postRequest(
                "https://www.cogedim.com/form-get",
                "form=re_forms_leaflet&program_nid=$programNumber&$contactinfo",
                FormGetResult::class.javaObjectType
        )
    }

    private fun <T> postRequest(url: String, writeData: String, gsonType: Class<T>): T? {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            applyRequestHeaders(this, true)
        }

        conn.outputStream.use { os ->
            OutputStreamWriter(os, "UTF-8").use { osw ->
                osw.write(writeData)
                osw.flush()
            }
        }

        return try {
            conn.inputStream.use {
                val result = IOUtils.toString(GZIPInputStream(it), StandardCharsets.UTF_8)
                try {
                    gson.fromJson(result, gsonType)
                } catch (e: java.lang.IllegalStateException) {
                    logger.error("Error converting response to json. " +
                            "Url = $url, writeData = $writeData, gsonType = ${gsonType.toGenericString()}",
                            e)
                    null
                }
            }
        } catch (e: java.io.IOException) {
            // if pdf not found it response 500, so skip the error 500
            if (conn.responseCode != 500) {
                logger.info("Request error. Url = $url, data = $writeData")
                logger.info(IOUtils.toString(GZIPInputStream(conn.errorStream), StandardCharsets.UTF_8))
            }
            null
        }
    }

    companion object {
        fun applyRequestHeaders(conn: URLConnection, isPost: Boolean): URLConnection = conn.apply {
            if (isPost) {
                (this as HttpURLConnection).requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            }
            setRequestProperty("Pragma", "no-cache")
            setRequestProperty("Sec-Fetch-Site", "same-origin")
            setRequestProperty("Origin", "https://www.cogedim.com")
            setRequestProperty("Accept-Encoding", "gzip, deflate, br")
            setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,fr;q=0.6,zh-TW;q=0.5")
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Mobile Safari/537.36")
            setRequestProperty("Sec-Fetch-Mode", "cors")
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("Referer", "https://www.cogedim.com/programme-immobilier-neuf/m8j9/")
            setRequestProperty("Cookie", "BACKENDID=COGEDIM-WEB-01; _gcl_au=1.1.25026551.1571652669; _ga=GA1.2.952906528.1571652669; _gid=GA1.2.1470064266.1571652669; __sonar=198838560166151756; _fbp=fb.1.1571652669541.1487313776; gwcc=%7B%22fallback%22%3A%220970255255%22%2C%22clabel%22%3A%22gO6yCLyn9ooBEN_DucMD%22%2C%22backoff%22%3A86400%2C%22backoff_expires%22%3A1571739069%7D; CookieConsent={stamp:\\'QVcpBClEOrYbHJsETUMHoiUDAGjhCNRwq538sc/7iFVZZ0pFv/CWGw==\\'%2Cnecessary:true%2Cpreferences:true%2Cstatistics:true%2Cmarketing:true%2Cver:1%2Cutc:1571652681875}; _gat_UA-57280140-1=1")
            setRequestProperty("Connection", "keep-alive")
        }
    }
}
