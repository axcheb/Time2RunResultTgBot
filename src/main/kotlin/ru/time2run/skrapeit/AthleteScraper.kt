package ru.time2run.skrapeit

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h2
import mu.KotlinLogging
import ru.time2run.AppProps
import ru.time2run.model.Athlete

private val logger = KotlinLogging.logger {}

suspend fun scrapeAthlete(athleteId: Int): Athlete {
    return skrape(HttpFetcher) {
        request {
            timeout = 10_000
            url = "https://${AppProps.domain}/${AppProps.urlPart}/$athleteId/"
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:98.0) Gecko/20100101 Firefox/98.0",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                "Accept-Language" to "en-GB,en;q=0.9",
//                    "Accept-Encoding" to "gzip, deflate, br", через HttpFetcher нельзя настроить ktor с поддержкой gzip
                "DNT" to "1",
                "Connection" to "keep-alive",
                "Upgrade-Insecure-Requests" to "1",
                "Sec-Fetch-Dest" to "document",
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-Site" to "same-origin",
                "Sec-Fetch-User" to "?1"
            )
        }
        response {
            logger.info("Athlete $athleteId - Response code: ${status { code }}")
            htmlDocument {
                Athlete(
                    athleteId,
                    div {
                        withId = "content"
                        h2 {
                            findFirst {
                                logger.info("Athlete $athleteId - OK")
                                text
                            }
                        }
                    }
                )
            }
        }
    }
}

