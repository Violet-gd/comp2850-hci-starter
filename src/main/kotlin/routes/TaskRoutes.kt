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

data class Page<T>(val items: List<T>, val page: Int, val pages: Int, val total: Int)

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

        val html = call.renderTemplate("tasks/index.peb", mapOf(
            "page" to pageData,
            "q" to q,
            "title" to "Tasks"
        ))
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
        val title = call.receiveParameters()["title"]?.trim().orEmpty()
        val task = Task(title = title)
        if (call.isHtmx()) {
            if (title.isBlank()) return@post call.respondText("""<div id="status" hx-swap-oob="true">Title required.</div>""")
            store.add(task)
            val li = call.renderTemplate("tasks/_item.peb", mapOf("task" to task))
            val status = """<div id="status" hx-swap-oob="true">Added “$title”.</div>"""
            return@post call.respondText(li + status, ContentType.Text.Html)
        }
        if (title.isNotBlank()) store.add(task)
        call.respondRedirect("/tasks")
    }
//    post("/tasks/{id}/delete") {
//        val id = call.parameters["id"] ?: return@delete
//        store.delete(id)
//        if (call.isHtmx()) return@post call.respondText("", ContentType.Text.Html)
//        call.respondRedirect("/tasks")
//    }


//    delete("/tasks/{id}") {
//        val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
//        val task = store.getById(id)
//        store.delete(id)
//        val status = """<div id="status" hx-swap-oob="true" role="status">Deleted "${task?.title ?: "task"}".</div>"""
//        call.respondText(status, ContentType.Text.Html)
//    }

// No-JS fallback (POST)
//    post("/tasks/{id}/delete") {
//        val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
//        val task = store.getById(id)
//        store.delete(id)
//
//        if (call.isHtmxRequest()) {
//            val statusHtml = """<div id="status" hx-swap-oob="true" role="status">Task "${task?.title ?: "Unknown"}" deleted.</div>"""
//            call.respondText(statusHtml, ContentType.Text.Html)
//        } else {
//            call.respondRedirect("/tasks")
//        }
//    }
}
