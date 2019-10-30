package com.catprogrammer.cogedimscanner.service.impl

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.FormGetResult
import com.catprogrammer.cogedimscanner.model.NearbyProgram
import com.catprogrammer.cogedimscanner.model.SearchResult
import com.catprogrammer.cogedimscanner.repository.ProgramRepository
import com.catprogrammer.cogedimscanner.service.CogedimCrawlerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream


@Service
class CogedimCrawlerServiceImpl : CogedimCrawlerService {

    // @Value("\${catprogrammer.article}")
    // lateinit var mokeArticle: String

    private val gson = Gson()
    private val type: Type = object : TypeToken<List<NearbyProgram>>() {}.type
    private val baseurl = "https://www.cogedim.com"
    // private val contactinfo = "contact_info_known=true&contact_info%5BformCivility%5D=02&contact_info%5BformFirstName%5D=Not&contact_info%5BformLastName%5D=APerson&contact_info%5BformPhone%5D=0660600660&contact_info%5BformEmail%5D=notanemail%40gmail.com&contact_info%5BformLocation%5D=Paris&contact_info%5BformCity%5D=Paris&contact_info%5BformPostalCode%5D=75000&contact_info%5BformRegion%5D=%C3%8Ele-de-France&contact_info%5BformCountry%5D=France&contact_info%5BformDestination%5D=habiter"
    private val contactinfo = "contact_info_known=true"

    @Autowired
    private lateinit var programRepository: ProgramRepository

    /**
     * Send one or multiple requests to fetch all program from Cogedim.
     * Return a list of SearchResult object.
     */
    // TODO: remove @Suppress after test
    @Suppress("CanBeVal")
    override fun requestSearchResults(): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        var page = 0
        var res: SearchResult?

        // TODO: remove @Suppress after test
        @Suppress("UNREACHABLE_CODE")
        @Suppress("UNUSED_CHANGED_VALUE")
        do {
            res = fetchSearchResult(page++)
            if (res != null) {
                results.add(res)
            }
            // TODO: remove next line after test
            break
        } while (res?.hasMore != null && res.hasMore!!)

        return results
    }

    /**
     * Parse a list of SearchResult object.
     * Find and save all programs and lots.
     */
    override fun parseSearchResuls(results: List<SearchResult>) {
        results.filter { it.results != null && it.results.size() > 0 }.forEach { searchResult ->
            val drupalSettings = searchResult.drupalSettings
            val nearbyPrograms: List<NearbyProgram> =
                    if (drupalSettings != null && drupalSettings.has("nearbyPrograms")) {
                        val nearbyProgramsJsonObject = drupalSettings.get("nearbyPrograms")
                        gson.fromJson<List<NearbyProgram>>(nearbyProgramsJsonObject, type)
                    } else
                        emptyList()


            searchResult.results?.forEach { result ->
                val article = Jsoup.parse(result.asString)
                // val article = Jsoup.parse(mokeArticle)
                val program = parseSearchResultProgram(article, nearbyPrograms)
                programRepository.save(program)
                parseSearchResultLot(article, program)
                programRepository.save(program)
            }
        }

    }

    /**
     * Parse the <article> dom object, find all lot and link to the given Program object
     */
    private fun parseSearchResultLot(article: Document, program: Program) {
        article.select("v-expansion-panel.regulation-4 > *").forEach { programTypeTag ->
            // for each lot type. eg: 3 pièces, 4 pièces, etc
            programTypeTag.select("v-expansion-panel v-expansion-panel-content[ripple]").forEach { lotTag ->
                // for each lot
                val lotNumber = lotTag.select("span.lot-lot_number").text()
                val surface = lotTag.select("span.lot-surface").text()
                val floor = lotTag.select("span.lot-floor").text()
                val price = lotTag.select("span.lot-price").text()

                val blueprintUrl = fetchFormBlueprint(program.programNumber, lotNumber)
                val pdfUrl = if (blueprintUrl != null) (baseurl + parseFormGetResultGetPdfUrl(blueprintUrl)) else null

                val lot = Lot(null, lotNumber, surface, floor, price, pdfUrl, null, null)
                program.lots.add(lot)
            }
        }
    }

    /**
     * Parse the <article> dom object, find it's program, instantiate and return the Program object
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

        // TODO: next 2 lines comments after test
        // val leafletForm = fetchFormLeaflet(programId)
        // val pdfUrl = if (leafletForm != null) (baseurl + parseFormGetResultGetPdfUrl(leafletForm)) else null
        // TODO: remove next line after test
        val pdfUrl = null
        val lots = mutableListOf<Lot>()

        val nearbyProgram = nearbyPrograms.first { p -> p.nid == programId }
        val latitude = nearbyProgram.lat
        val longitude = nearbyProgram.lng

        return Program(null, programName, programId, postalCode, address, url, imgUrl, pdfUrl,
                latitude, longitude, lots, null, null)
    }

    private fun parseFormGetResultGetPdfUrl(result: FormGetResult): String {
        return Jsoup.parse(result.form).select("div.confirmation a").attr("href")
    }

    private fun fetchSearchResult(page: Int): SearchResult? {
        return postRequest(
                "https://www.cogedim.com/search-results?page=$page",
                "location=Hauts-de-Seine&department=Hauts-de-Seine&rooms=3,4,5",
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
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Pragma", "no-cache")
            setRequestProperty("Sec-Fetch-Site", "same-origin")
            setRequestProperty("Origin", "https://www.cogedim.com")
            setRequestProperty("Accept-Encoding", "gzip, deflate, br")
            setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,fr;q=0.6,zh-TW;q=0.5")
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Mobile Safari/537.36")
            setRequestProperty("Sec-Fetch-Mode", "cors")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("Referer", "https://www.cogedim.com/programme-immobilier-neuf/m8j9/")
            setRequestProperty("Cookie", "BACKENDID=COGEDIM-WEB-01; _gcl_au=1.1.25026551.1571652669; _ga=GA1.2.952906528.1571652669; _gid=GA1.2.1470064266.1571652669; __sonar=198838560166151756; _fbp=fb.1.1571652669541.1487313776; gwcc=%7B%22fallback%22%3A%220970255255%22%2C%22clabel%22%3A%22gO6yCLyn9ooBEN_DucMD%22%2C%22backoff%22%3A86400%2C%22backoff_expires%22%3A1571739069%7D; CookieConsent={stamp:\\'QVcpBClEOrYbHJsETUMHoiUDAGjhCNRwq538sc/7iFVZZ0pFv/CWGw==\\'%2Cnecessary:true%2Cpreferences:true%2Cstatistics:true%2Cmarketing:true%2Cver:1%2Cutc:1571652681875}; _gat_UA-57280140-1=1")
            setRequestProperty("Connection", "keep-alive")
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
                gson.fromJson(result, gsonType)
            }
        } catch (e: java.io.IOException) {
            println("Request error. Url = $url, data = $writeData")
            null
        }
    }
}
