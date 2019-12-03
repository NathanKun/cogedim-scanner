package com.catprogrammer.cogedimscanner.repository

import com.catprogrammer.cogedimscanner.entity.Program
import com.catprogrammer.cogedimscanner.model.RealEstateDeveloper
import org.springframework.data.jpa.repository.JpaRepository

interface ProgramRepository : JpaRepository<Program, Long> {
    fun findAllByDeveloperAndProgramNumber(developer: RealEstateDeveloper, programNumber: String): List<Program>
}
