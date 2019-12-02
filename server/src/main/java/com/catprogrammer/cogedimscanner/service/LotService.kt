package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.model.RealEstateDeveloper

interface LotService {
    fun findAllByDeveloperAndProgramNumberAndLotNumber(developer: RealEstateDeveloper, programNumber: String, lotNumer: String): List<Lot>

    fun save(lot: Lot): Lot
}
