package com.catprogrammer.cogedimscanner.repository

import com.catprogrammer.cogedimscanner.entity.Program
import org.springframework.data.jpa.repository.JpaRepository

interface ProgramRepository : JpaRepository<Program, Long>
