package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.entity.Decision
import com.catprogrammer.cogedimscanner.repository.LotRepository
import com.catprogrammer.cogedimscanner.service.LotService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
open class LotController {

    private val logger = LoggerFactory.getLogger(LotController::class.java)

    @Autowired
    private lateinit var lotService: LotService

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PutMapping("/program/{programNumber}/lot/{lotNumber}")
    open fun setLotProperty(@PathVariable programNumber: String,
                            @PathVariable lotNumber: String,
                            @RequestParam(value = "remark", required = false) remark: String?,
                            @RequestParam(value = "decision", required = false) decision: String?) {
        val lots = lotService.findAllByProgramNumberAndLotNumber(programNumber, lotNumber)
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
}
