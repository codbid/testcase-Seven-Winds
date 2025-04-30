package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, CreateAuthorRequest>(info("Добавить запись")) { _, body ->
            val newAuthor = AuthorService.addAuthor(body)
            respond(newAuthor)
        }
    }
}

data class CreateAuthorRequest(val name: String)

data class AuthorRecord(
    val id: Int,
    val name: String,
    val createdAt: String
)
