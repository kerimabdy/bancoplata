package tech.bashga.plata

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform