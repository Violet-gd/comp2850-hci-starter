package routes

import storage.TaskStore
import model.Task
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.pebbletemplates.pebble.PebbleEngine
import java.io.StringWriter
import renderTemplate
import isHtmxRequest
/**
 * NOTE FOR NON-INTELLIJ IDEs (VSCode, Eclipse, etc.):
 * IntelliJ IDEA automatically adds imports as you type. If using a different IDE,
 * you may need to manually add imports. The commented imports below show what you'll need
 * for future weeks. Uncomment them as needed when following the lab instructions.
 *
 * When using IntelliJ: You can ignore the commented imports below - your IDE will handle them.
 */

// Week 7+ imports (inline edit, toggle completion):
// import model.Task               // When Task becomes separate model class
// import model.ValidationResult   // For validation errors
// import renderTemplate            // Extension function from Main.kt
// import isHtmxRequest             // Extension function from Main.kt

// Week 8+ imports (pagination, search, URL encoding):
// import io.ktor.http.encodeURLParameter  // For query parameter encoding
// import utils.Page                       // Pagination helper class

// Week 9+ imports (metrics logging, instrumentation):
// import utils.jsMode              // Detect JS mode (htmx/nojs)
// import utils.logValidationError  // Log validation failures
// import utils.timed               // Measure request timing

// Note: Solution repo uses storage.TaskStore instead of data.TaskRepository
// You may refactor to this in Week 10 for production readiness

/**
 * Week 6 Lab 1: Simple task routes with HTMX progressive enhancement.
 *
 * **Teaching approach**: Start simple, evolve incrementally
 * - Week 6: Basic CRUD with Int IDs
 * - Week 7: Add toggle, inline edit
 * - Week 8: Add pagination, search
 */

fun Route.taskRoutes(store: TaskStore = TaskStore()) {
    val pebble =
        PebbleEngine
            .Builder()
            .loader(
                io.pebbletemplates.pebble.loader.ClasspathLoader().apply {
                    prefix = "templates/"
                },
            ).build()

    /**
     * Helper: Check if request is from HTMX
     */
    fun ApplicationCall.isHtmx(): Boolean = request.headers["HX-Request"]?.equals("true", ignoreCase = true) == true

    /**
     * GET /tasks - List all tasks
     * Returns full page (no HTMX differentiation in Week 6)
     */
    get("/tasks") {
        val q = call.request.queryParameters["q"]?.trim().orEmpty()
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageData = store.search(q, page, 10)

        val html = call.renderTemplate("tasks/index.peb", mapOf(
            "page" to pageData,
            "q" to q,
            "title" to "Tasks"
        ))
        call.respondText(html, ContentType.Text.Html)
    }

    /**
     * POST /tasks - Add new task
     * Dual-mode: HTMX fragment or PRG redirect
     */


    post("/tasks") {
        val title = call.receiveParameters()["title"].orEmpty().trim()
        if (title.isBlank()) return@post call.respondRedirect("/tasks")

        val task = Task(title = title)
        store.add(task)

        if (call.isHtmx()) {

            val q = call.request.queryParameters["q"]?.trim().orEmpty()
            val pageData = store.search(q, 1, 10)

            val list = call.renderTemplate("tasks/_list.peb", mapOf(
                "page" to pageData,
                "q" to q
            ))
            val pager = call.renderTemplate("tasks/_pager.peb", mapOf(
                "page" to pageData,
                "q" to q
            ))

            val status = """<div id="status" hx-swap-oob="true">Added "${task.title}".</div>"""

            return@post call.respondText(
                list + pager + status,
                ContentType.Text.Html
            )
        }

        // No-JS fallback
        call.respondRedirect("/tasks")
    }//get by chatgpt


//    post("/tasks") {
//        val title = call.receiveParameters()["title"].orEmpty().trim()
//
//        // Validation
//        if (title.isBlank()) {
//            if (call.isHtmx()) {
//                val status = """<div id="status" hx-swap-oob="true">Title is required.</div>"""
//                return@post call.respondText(status, ContentType.Text.Html, HttpStatusCode.BadRequest)
//            } else {
//                // No-JS: redirect with error query param
//                return@post call.respondRedirect("/tasks?error=title")
//            }
//        }
//
//        if (title.length > 200) {
//            if (call.isHtmx()) {
//                val status = """<div id="status" hx-swap-oob="true">Title too long (max 200 chars).</div>"""
//                return@post call.respondText(status, ContentType.Text.Html, HttpStatusCode.BadRequest)
//            } else {
//                return@post call.respondRedirect("/tasks?error=title&msg=too_long")
//            }
//        }
//
//        // Success path
//        val task = Task(title = title)
//        store.add(task)
//        if (call.isHtmx()) {
//            val item = PebbleRender.render("tasks/_item.peb", mapOf("t" to task))
//            val status = """<div id="status" hx-swap-oob="true">Added "${task.title}".</div>"""
//            return@post call.respondText(item + status, ContentType.Text.Html)
//        }
//        call.respondRedirect("/tasks")
//    }


    /**
     * POST /tasks/{id}/delete - Delete task
     * Dual-mode: HTMX empty response or PRG redirect
     */
//    post("/tasks/{id}/delete") {
//        val id = call.parameters["id"]?.toIntOrNull()
//        val removed = id?.let { store.delete(it) } ?: false
//
//        if (call.isHtmx()) {
//            val message = if (removed) "Task deleted." else "Could not delete task."
//            val status = """<div id="status" hx-swap-oob="true">$message</div>"""
//            // Return empty content to trigger outerHTML swap (removes the <li>)
//            return@post call.respondText(status, ContentType.Text.Html)
//        }
//
//        // No-JS: POST-Redirect-GET pattern (303 See Other)
//        call.response.headers.append("Location", "/tasks")
//        call.respond(HttpStatusCode.SeeOther)
//    }


    get("/tasks/fragment") {
        val q = call.request.queryParameters["q"]?.trim().orEmpty()
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageData = store.search(q, page, 10)

        val list = call.renderTemplate("tasks/_list.peb", mapOf("page" to pageData, "q" to q))
        val pager = call.renderTemplate("tasks/_pager.peb", mapOf("page" to pageData, "q" to q))
        val status = """<div id="status" hx-swap-oob="true">Updated: showing ${pageData.items.size} of ${pageData.total} tasks</div>"""

        call.respondText(list + pager + status, ContentType.Text.Html)
    }



}
