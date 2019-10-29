package com.catprogrammer.cogedimscanner.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Program(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int?,
        val programName: String,
        val programNumber: String,
        val postalCode: String,
        val address: String,
        val url: String,
        val imgUrl: String,
        val pdfUrl: String?,
        val latitude: String,
        val longitude: String,
        @OneToMany
        val lots: MutableList<Lot>,
        @CreatedDate
        val createdAt: LocalDateTime?,
        @LastModifiedDate
        val modifiedAt: LocalDateTime?
) {
    override fun toString(): String {
        return "Program(id=$id, programName='$programName', programNumber='$programNumber', postalCode='$postalCode', address='$address', url='$url', imgUrl='$imgUrl', pdfUrl=$pdfUrl, latitude='$latitude', longitude='$longitude', lots=$lots, createdAt=$createdAt, modifiedAt=$modifiedAt)"
    }
}
