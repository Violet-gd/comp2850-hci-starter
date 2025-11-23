package route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import storage.TaskStore

data class Page<T>(val items: List<T>, val page: Int, val pages: Int, val total: Int)

interface Repo {
    fun add(title: String): Task
    fun delete(id: Int)
    fun search(q: String, page: Int, size: Int): Page<Task>
}
data class Task(val id: Int, val title: String)

fun ApplicationCall.isHtmx() = request.headers["HX-Request"] == "true"
fun render(tpl: String, model: Map<String, Any?>): String = "<!-- render $tpl with $model -->" // replace

fun Route.taskRoutes(repo: Repo) {
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
        val data = repo.search(q, page, 10)
        val list = render("tasks/_list.peb", mapOf("page" to data, "q" to q))
        val pager = render("tasks/_pager.peb", mapOf("page" to data, "q" to q))
        call.respondText(list + pager + """<div id="status" hx-swap-oob="true">Updated.</div>""", ContentType.Text.Html)
    }
    post("/tasks") {
        val title = call.receiveParameters()["title"]?.trim().orEmpty()
        if (call.isHtmx()) {
            if (title.isBlank()) return@post call.respondText("""<div id="status" hx-swap-oob="true">Title required.</div>""")
            val t = repo.add(title)
            val li = render("tasks/_item.peb", mapOf("t" to t))
            val status = """<div id="status" hx-swap-oob="true">Added “$title”.</div>"""
            return@post call.respondText(li + status, ContentType.Text.Html)
        }
        if (title.isNotBlank()) repo.add(title)
        call.respondRedirect("/tasks")
    }
    post("/tasks/{id}/delete") {
        val id = call.parameters["id"]!!.toInt()
        repo.delete(id)
        if (call.isHtmx()) return@post call.respondText("", ContentType.Text.Html)
        call.respondRedirect("/tasks")
    }

    fun Routing.configureTaskRoutes(store: TaskStore = TaskStore()) {
        post("/tasks") {
            val title = call.receiveParameters()["title"] ?: ""
            val task = Task(title = title)  // UUID auto-generated
            store.add(task)
            // ...
        }

        delete("/tasks/{id}") {
            val id = call.parameters["id"] ?: return@delete  // No toIntOrNull
            store.delete(id)
        }
    }

}

