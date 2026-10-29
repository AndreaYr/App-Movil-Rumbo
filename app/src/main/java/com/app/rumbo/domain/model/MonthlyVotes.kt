package com.app.rumbo.domain.model

import java.util.Calendar

/** Votos "Me interesa" recibidos en un mes. [month] va de 1 (enero) a 12 (diciembre). */
data class MonthlyVotes(
    val month: Int,
    val votes: Int
) {
    companion object {
        /** Los últimos [months] meses hasta [now] (el actual incluido), del más antiguo al más reciente, sin votos. */
        fun emptyWindow(now: Long, months: Int = 6): List<MonthlyVotes> {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -(months - 1))
            }
            return List(months) {
                MonthlyVotes(month = calendar.get(Calendar.MONTH) + 1, votes = 0).also {
                    calendar.add(Calendar.MONTH, 1)
                }
            }
        }
    }
}
