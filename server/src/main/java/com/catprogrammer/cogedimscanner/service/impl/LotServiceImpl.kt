package com.catprogrammer.cogedimscanner.service.impl

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.repository.LotRepository
import com.catprogrammer.cogedimscanner.repository.ProgramRepository
import com.catprogrammer.cogedimscanner.service.LotService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
open class LotServiceImpl : LotService {

    @Autowired
    lateinit var programRepository: ProgramRepository

    @Autowired
    lateinit var lotRepository: LotRepository

    override fun findAllByProgramNumberAndLotNumber(programNumber: String, lotNumer: String): List<Lot> {
        val programs = programRepository.findAllByProgramNumber(programNumber)
        val lots = mutableListOf<Lot>()

        programs.forEach { p ->
            lots.addAll(p.lots.filter { l -> l.lotNumber == lotNumer })
        }

        return lots
    }

    override fun save(lot: Lot): Lot {
        return lotRepository.save(lot)
    }
}
