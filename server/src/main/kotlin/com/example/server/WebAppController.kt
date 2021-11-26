package com.example.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebAppController {
    @GetMapping("api/v1")
    fun webApi(): Data {
        return Data()
    }
}