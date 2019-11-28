package com.catprogrammer.cogedimscanner.model

enum class RealEstateDeveloper(val baseurl: String, val leafletParam: String) {
    COGEDIM("https://www.cogedim.com", "re_forms_leaflet"),
    KAUFMANBROAD("https://www.kaufmanbroad.fr", "re_forms_booklet");
}
