package com.catprogrammer.cogedimscanner.repository

import com.catprogrammer.cogedimscanner.entity.Lot
import org.springframework.data.jpa.repository.JpaRepository

interface LotRepository: JpaRepository<Lot, Long> {
    fun findByBlueprintIdOrderByIdDesc(blueprintId: String): List<Lot>

    fun findAllByLotNumber(lotNumber: String): List<Lot>
}
