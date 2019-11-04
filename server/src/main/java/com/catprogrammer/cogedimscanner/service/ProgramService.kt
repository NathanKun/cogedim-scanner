package com.catprogrammer.cogedimscanner.service

import com.catprogrammer.cogedimscanner.entity.Program

interface ProgramService {
    fun findProgramsGroupByProgramNumber(): Map<String, List<Program>>
}
