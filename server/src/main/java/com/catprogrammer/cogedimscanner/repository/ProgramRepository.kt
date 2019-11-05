package com.catprogrammer.cogedimscanner.repository

import com.catprogrammer.cogedimscanner.entity.Program
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProgramRepository : JpaRepository<Program, Long>
