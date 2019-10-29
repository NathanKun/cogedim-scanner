package com.catprogrammer.cogedimscanner.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Lot (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int?,
        val lotNumber: String,
        val surface: String,
        val floor: String,
        val price: String,
        val pdfUrl: String?,
        @CreatedDate
        val createdAt: LocalDateTime?,
        @LastModifiedDate
        val modifiedAt: LocalDateTime?
) {
        override fun toString(): String {
                return "Lot(id=$id, lotNumber='$lotNumber', surface='$surface', floor='$floor', price='$price', pdfUrl='$pdfUrl', createdAt=$createdAt, modifiedAt=$modifiedAt)"
        }
}
