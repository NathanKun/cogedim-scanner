package com.catprogrammer.cogedimscanner.model

import com.catprogrammer.cogedimscanner.entity.Lot
import com.catprogrammer.cogedimscanner.entity.Program
import java.time.LocalDate

data class ProgramDateLotDto(
        val program: Program,
        val dateMap: Map<LocalDate, List<Lot>>
)
