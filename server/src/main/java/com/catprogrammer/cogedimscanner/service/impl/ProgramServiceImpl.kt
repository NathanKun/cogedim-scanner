package com.catprogrammer.cogedimscanner.service.impl

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.model.ProgramDateLotDto
import com.catprogrammer.cogedimscanner.repository.ProgramRepository
import com.catprogrammer.cogedimscanner.service.ProgramService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProgramServiceImpl : ProgramService {
    @Autowired
    lateinit var programRepository: ProgramRepository

    override fun findProgramsGroupByProgramNumber(): List<ProgramDateLotDto> {
        val res = mutableListOf<ProgramDateLotDto>()
        // a map of programName => same program of different date
        val programNumberToProgramMap = programRepository.findAll().groupBy({ it.programNumber }, { it })
        programNumberToProgramMap.values.forEach { list -> // list of same program of different day
            val program = list.last()
            val map = mutableMapOf<LocalDate, List<Lot>>()
            list.forEach {
                // assuming only one program in one date
                map[it.createdAt!!.toLocalDate()] = it.lots.toList()
            }
            res.add(ProgramDateLotDto(program, map))
        }

        return res
    }
}
