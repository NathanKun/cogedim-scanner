package com.catprogrammer.cogedimscanner.controller

import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.service.ProgramService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
open class ProgramController {
    @Autowired
    lateinit var programService: ProgramService

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @GetMapping("/programs")
    open fun findAllGroupByProgramNumber(): Map<String, List<Program>> {
        return programService.findProgramsGroupByProgramNumber()
    }
}
