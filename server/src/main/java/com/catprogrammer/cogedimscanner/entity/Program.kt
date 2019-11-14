package com.catprogrammer.cogedimscanner.entity

import com.fasterxml.jackson.annotation.JsonView
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
        val address: String,// city
        val url: String,
        val imgUrl: String,
        val pdfUrl: String?,
        val latitude: String,
        val longitude: String,
        val addressGeoDecoded: String?,
        @JsonView(LotsView::class)
        @OneToMany(fetch = FetchType.EAGER)
        val lots: MutableList<Lot>,
        @CreationTimestamp
        val createdAt: LocalDateTime?,
        @UpdateTimestamp
        val modifiedAt: LocalDateTime?
) {
    interface SimpleView
    interface LotsView: SimpleView
}
