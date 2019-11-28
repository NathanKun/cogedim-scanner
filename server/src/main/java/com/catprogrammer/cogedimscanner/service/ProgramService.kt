package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.ProgramDateLotDto

interface ProgramService {
    fun findProgramsGroupByProgramNumber(): List<ProgramDateLotDto>

    fun save(p: Program): Program
}
