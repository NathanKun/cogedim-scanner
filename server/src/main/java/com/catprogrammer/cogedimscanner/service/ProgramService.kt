package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.model.ProgramDateLotDto

interface ProgramService {
    fun findProgramsGroupByProgramNumber(): List<ProgramDateLotDto>
}
