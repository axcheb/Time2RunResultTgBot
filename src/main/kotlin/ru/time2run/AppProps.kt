package ru.time2run

import java.util.*
import java.util.concurrent.TimeUnit

object AppProps {

    private val appProperties = Properties()

    init {
        val appPropertiesFile = this.javaClass.getResourceAsStream("/properties/app.properties")
        appProperties.load(appPropertiesFile)
    }

    val botToken: String
        get() = appProperties.getProperty("botToken")

    val domain: String
        get() = appProperties.getProperty("domain")

    val urlPart: String
        get() = appProperties.getProperty("urlPart")

    val requestDelay: Long
        get() = appProperties.getProperty("requestDelay").toLong()

    val storageLifetime: Long
        get() = TimeUnit.DAYS.toMillis(appProperties.getProperty("storageLifetimeDay").toLong())

}