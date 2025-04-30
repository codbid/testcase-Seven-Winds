package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addAuthor(body: CreateAuthorRequest): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                name = body.name
            }

            return@transaction AuthorRecord(
                id = entity.id.value,
                name = entity.name,
                createdAt = entity.createdAt.toString()
            )
        }
    }
}
