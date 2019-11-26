package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.entity.Decision
import com.catprogrammer.cogedimscanner.repository.LotRepository
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
    private lateinit var lotRepository: LotRepository

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PutMapping("/lot/{lotNumber}")
    open fun setLotProperty(@PathVariable lotNumber: String,
                            @RequestParam(value = "remark", required = false) remark: String?,
                            @RequestParam(value = "decision", required = false) decision: String?) {
        val lots = lotRepository.findAllByLotNumber(lotNumber)
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
            lotRepository.save(lot)
        }
    }
}
