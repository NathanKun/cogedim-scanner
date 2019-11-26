package com.catprogrammer.cogedimscanner.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Lot(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long?,
        val lotNumber: String,
        val surface: String,
        val floor: String,
        val price: String,
        val blueprintId: String?,
        val pdfUrl: String?,
        var remark: String?,
        var decision: Decision,
        @CreationTimestamp
        val createdAt: LocalDateTime?,
        @UpdateTimestamp
        val modifiedAt: LocalDateTime?
) {
    override fun toString(): String {
        return "Lot(id=$id, lotNumber='$lotNumber', surface='$surface', floor='$floor', price='$price', pdfUrl='$pdfUrl', createdAt=$createdAt, modifiedAt=$modifiedAt)"
    }
}

enum class Decision(s: String) {
    GOOD("GOOD"), SECONDARY("SECONDARY"), BAD("BAD"), NONE("NONE")
}
