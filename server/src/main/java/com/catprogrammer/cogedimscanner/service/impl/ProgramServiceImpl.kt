package com.catprogrammer.cogedimscanner.service.impl

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.ProgramDateLotDto
import com.catprogrammer.cogedimscanner.model.RealEstateDeveloper
import com.catprogrammer.cogedimscanner.repository.ProgramRepository
import com.catprogrammer.cogedimscanner.service.ProgramService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
open class ProgramServiceImpl : ProgramService {
    @Autowired
    lateinit var programRepository: ProgramRepository

    override fun findProgramsGroupByProgramNumber(): List<ProgramDateLotDto> {
        val programs = programRepository.findAll()
        programs.forEach { p ->
            @Suppress("SENSELESS_COMPARISON")
            if (p.developer == null) p.developer = RealEstateDeveloper.COGEDIM
        }

        val res = mutableListOf<ProgramDateLotDto>()
        // a map of programName => same program of different date
        val programNumberToProgramMap = programs.groupBy({ it.programNumber }, { it })
        programNumberToProgramMap.values.forEach { list ->
            // list of same program of different day
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

    override fun save(p: Program): Program {
        return programRepository.save(p)
    }
}
