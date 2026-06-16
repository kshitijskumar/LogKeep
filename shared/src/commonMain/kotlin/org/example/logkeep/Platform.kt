package org.example.logkeep

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform