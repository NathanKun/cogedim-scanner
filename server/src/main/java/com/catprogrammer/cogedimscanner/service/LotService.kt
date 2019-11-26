package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.entity.Lot

interface LotService {
    fun findAllByProgramNumberAndLotNumber(programNumber: String, lotNumer: String): List<Lot>

    fun save(lot: Lot): Lot
}
