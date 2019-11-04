package com.catprogrammer.cogedimscanner.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Program(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long?,
        val programName: String,
        val programNumber: String,
        val postalCode: String,
        val address: String,
        val url: String,
        val imgUrl: String,
        val pdfUrl: String?,
        val latitude: String,
        val longitude: String,
        @OneToMany(fetch = FetchType.EAGER)
        val lots: MutableList<Lot>,
        @CreationTimestamp
        val createdAt: LocalDateTime?,
        @UpdateTimestamp
        val modifiedAt: LocalDateTime?
) {
    override fun toString(): String {
        return "Program(id=$id, programName='$programName', programNumber='$programNumber', postalCode='$postalCode', address='$address', url='$url', imgUrl='$imgUrl', pdfUrl=$pdfUrl, latitude='$latitude', longitude='$longitude', lots=$lots, createdAt=$createdAt, modifiedAt=$modifiedAt)"
    }
}
