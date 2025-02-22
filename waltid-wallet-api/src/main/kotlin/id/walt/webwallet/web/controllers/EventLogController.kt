package id.walt.webwallet.web.controllers

import id.walt.web.controllers.getWalletService
import id.walt.webwallet.service.events.EventLogFilter
import id.walt.webwallet.service.events.EventLogFilterResult
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.eventLogs() = walletRoute {
    route("eventlog", {
        tags = listOf("Event Log")
    }) {
        get({
            summary = "Retrieve event logs for currently signed in account wallet"
            request {
                queryParameter<String>("limit") {
                    description = "Page size"
                    example = "10"
                    required = false
                }
                queryParameter<List<String>>("filter") {
                    description = "List of key=value pairs for filtering"
                    example = "key=value"
                    required = false
                }
                queryParameter<String>("startingAfter") {
                    description = "Starting after page"
                    example = "<hash>"
                    required = false
                }
                queryParameter<String>("sortBy") {
                    description = "The property to sort by"
                    example = "tenant"
                    required = false
                }
                queryParameter<String>("sortOrder") {
                    description = "The sort order [ASC|DESC]"
                    example = "ASC"
                    required = false
                }
            }
            response {
                HttpStatusCode.OK to {
                    body<EventLogFilterResult> {
                        description = "The event log result"
                    }
                }
            }
        }) {
            val wallet = getWalletService()
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: -1
            val data = call.request.queryParameters.getAll("filter")
                ?.associate { it.substringBefore("=") to it.substringAfter("=") } ?: emptyMap()
            val startingAfter = call.request.queryParameters["startingAfter"]
            val sortBy = call.request.queryParameters["sortBy"]
            val sortOrder = call.request.queryParameters["sortOrder"]
            context.respond(transaction {
                wallet.filterEventLog(
                    EventLogFilter(
                        limit = limit,
                        startingAfter = startingAfter,
                        sortBy = sortBy,
                        sortOrder = sortOrder,
                        data = data,
                    )
                )
            })
        }
    }
}
