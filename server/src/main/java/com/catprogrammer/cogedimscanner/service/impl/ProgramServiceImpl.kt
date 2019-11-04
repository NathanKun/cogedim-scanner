package com.catprogrammer.cogedimscanner.service.impl

import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.repository.ProgramRepository
import com.catprogrammer.cogedimscanner.service.ProgramService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProgramServiceImpl : ProgramService {
    @Autowired
    lateinit var programRepository: ProgramRepository

    override fun findProgramsGroupByProgramNumber(): Map<String, List<Program>> = programRepository.findAll().groupBy({ it.programNumber }, { it })
}
