package route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.Task
import data.Page
import storage.TaskStore
import renderTemplate
import isHtmxRequest
import model.ValidationResult
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import model.toPebbleContext

//data class Page<T>(val items: List<T>, val page: Int, val pages: Int, val total: Int)

//interface Repo {
//    fun add(title: String): Task
//    fun delete(id: Int)
//    fun search(q: String, page: Int, size: Int): Page<Task>
//}

//data class Task(val id: Int, val title: String)


fun ApplicationCall.isHtmx() = request.headers["HX-Request"] == "true"
//fun render(tpl: String, model: Map<String, Any?>): String = "<!-- render $tpl with $model -->" // replace

fun Route.taskRoutes(store: TaskStore = TaskStore()) {
    get("/tasks") {
        val q = call.request.queryParameters["q"]?.trim().orEmpty()
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageData = store.search(q, page, 10)
        val msg = call.request.queryParameters["msg"]
        val error = call.request.queryParameters["error"]

        val html = call.renderTemplate("tasks/index.peb", mapOf(
            "page" to pageData,
            "q" to q,
            "title" to "Tasks",
            "error" to (error ?: ""),
            "msg" to (msg ?: ""),
        )
        )
        call.respondText(html, ContentType.Text.Html)
    }

    get("/tasks/fragment") {
        val q = call.request.queryParameters["q"]?.trim().orEmpty()
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageData = store.search(q, page, 10)

        val list = call.renderTemplate("tasks/_list.peb", mapOf("page" to pageData, "q" to q))
        val pager = call.renderTemplate("tasks/_pager.peb", mapOf("page" to pageData, "q" to q))
        val status = """<div id="status" hx-swap-oob="true">Updated: showing ${pageData.items.size} of ${pageData.total} tasks</div>"""

        call.respondText(list + pager + status, ContentType.Text.Html)
    }
    post("/tasks") {
        val params = call.receiveParameters()
        val title = params["title"]?.trim().orEmpty()
        val q = params["q"]?.trim().orEmpty()

        // ---- 1. Validation ----
        when (val validation = Task.validate(title)) {
            is ValidationResult.Error -> {
                if (call.isHtmxRequest()) {
                    val pageData = store.search(q, 1, 10)

                    val list = call.renderTemplate("tasks/_list.peb", mapOf("page" to pageData, "q" to q))
                    val pager = call.renderTemplate("tasks/_pager.peb", mapOf("page" to pageData, "q" to q))

                    val status = """<div id="status" hx-swap-oob="true" role="alert" class="error">${validation.message}
                    </div>
                """

                    return@post call.respondText(list + pager + status, ContentType.Text.Html)
                } else {
                    return@post call.respondRedirect("/tasks?error=${validation.message}")
                }
            }

            ValidationResult.Success -> {
                // ---- 2. Create task ----
                val task = Task(title = title)
                store.add(task)

                if (call.isHtmxRequest()) {
                    val pageData = store.search(q, 1, 10)

                    val list = call.renderTemplate(
                        "tasks/_list.peb",
                        mapOf("page" to pageData, "q" to q)
                    )
                    val pager = call.renderTemplate(
                        "tasks/_pager.peb",
                        mapOf("page" to pageData, "q" to q)
                    )

                    val status = """
                    <div id="status" hx-swap-oob="true" role="status">
                        Added “${task.title}”
                    </div>
                """

                    return@post call.respondText(list + pager + status, ContentType.Text.Html)
                } else {
                    return@post call.respondRedirect("/tasks")
                }
            }
        }
    }


    get("/tasks/{id}/edit") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val task = store.getById(id)

        if (task == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        if (call.isHtmxRequest()) {
            // HTMX: return inline edit fragment
            val html = call.renderTemplate("tasks/_edit.peb", mapOf("task" to task.toPebbleContext()))
            call.respondText(html, ContentType.Text.Html)
        } else {
            // No-JS: redirect to list (edit not supported without JS)
            call.respondRedirect("/tasks")
        }
    }

// POST /tasks/{id}/edit - Save edits with validation
    post("/tasks/{id}/edit") {
        val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val task = store.getById(id)

        if (task == null) {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        val newTitle = call.receiveParameters()["title"]?.trim() ?: ""
        val validation = Task.validate(newTitle)

        if (validation is ValidationResult.Error) {
            if (call.isHtmxRequest()) {
                // HTMX: return edit form with error
                val html = call.renderTemplate("tasks/_edit.peb", mapOf(
                    "task" to task.toPebbleContext(),
                    "error" to validation.message
                ))
                call.respondText(html, ContentType.Text.Html)
            } else {
                // No-JS: redirect back
                call.respondRedirect("/tasks")
            }
            return@post
        }

        // Update task
        val updated = task.copy(title = newTitle)
        store.update(updated)

        if (call.isHtmxRequest()) {
            // HTMX: return view fragment
            val html = call.renderTemplate("tasks/_item.peb", mapOf("task" to updated.toPebbleContext()))
            val status = """<div id="status" hx-swap-oob="true" role="status">Task updated successfully.</div>"""
            call.respondText(html + status, ContentType.Text.Html)
        } else {
            // No-JS: redirect to list
            call.respondRedirect("/tasks")
        }
    }

    delete("/tasks/{id}") {
        val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val task = store.getById(id)
        store.delete(id)
        val status = """<div id="status" hx-swap-oob="true" role="status">Deleted "${task?.title ?: "task"}".</div>"""
        call.respondText(status, ContentType.Text.Html)
    }

// No-JS fallback (POST)
    post("/tasks/{id}/delete") {
        val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val task = store.getById(id)
        store.delete(id)

        if (call.isHtmxRequest()) {
            val statusHtml = """<div id="status" hx-swap-oob="true" role="status">Task "${task?.title ?: "Unknown"}" deleted.</div>"""
            call.respondText(statusHtml, ContentType.Text.Html)
        } else {
            call.respondRedirect("/tasks")
        }
    }
}
