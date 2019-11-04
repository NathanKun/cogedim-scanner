package com.catprogrammer.cogedimscanner.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
open class IndexController {

    @GetMapping("/")
    open fun index() = "Cogedim Scanner Api"
}
