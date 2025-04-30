package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetDto = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorEntity.findById(EntityID(it, AuthorTable)) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        var filterExpr: Op<Boolean> = BudgetTable.year eq param.year

        param.authorName?.let { name ->
            val subquery = AuthorTable
                .slice(AuthorTable.id)
                .select { AuthorTable.name.lowerCase() like "%${name.toLowerCase()}%" }

            filterExpr = filterExpr and (BudgetTable.authorId inSubQuery subquery)
        }

        transaction {
            val total = BudgetTable.select { filterExpr }.count()

            val pagedQuery = BudgetTable
                .select { filterExpr }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)

            val items = BudgetEntity.wrapRows(pagedQuery).map { it.toResponse() }

            val sumByType = BudgetEntity
                .find { filterExpr }
                .groupBy { it.type.name }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = items
            )
        }
    }
}