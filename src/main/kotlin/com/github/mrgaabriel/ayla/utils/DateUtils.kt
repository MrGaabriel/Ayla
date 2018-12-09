package com.github.mrgaabriel.ayla.utils

import java.time.Month
import java.time.OffsetDateTime
import java.util.*

object DateUtils {
    private val maxYears = 100000

    fun dateDiff(type: Int, fromDate: Calendar, toDate: Calendar, future: Boolean): Int {
        val year = Calendar.YEAR

        val fromYear = fromDate.get(year)
        val toYear = toDate.get(year)
        if (Math.abs(fromYear - toYear) > maxYears) {
            toDate.set(year, fromYear + if (future) maxYears else -maxYears)
        }

        var diff = 0
        var savedDate = fromDate.timeInMillis
        while (future && !fromDate.after(toDate) || !future && !fromDate.before(toDate)) {
            savedDate = fromDate.timeInMillis
            fromDate.add(type, if (future) 1 else -1)
            diff++
        }
        diff--
        fromDate.timeInMillis = savedDate
        return diff
    }

    fun formatDateDiff(date: Long): String {
        val c = GregorianCalendar()
        c.timeInMillis = date
        val now = GregorianCalendar()
        return formatDateDiff(now, c)
    }

    fun formatDateDiff(fromDate: Long, toDate: Long): String {
        val c = GregorianCalendar()
        c.timeInMillis = fromDate
        val now = GregorianCalendar()
        now.timeInMillis = toDate
        return formatDateDiff(now, c)
    }

    fun formatDateDiff(fromDate: Calendar, toDate: Calendar): String {
        var future = false
        if (toDate == fromDate) {
            return "alguns milisegundos"
        }
        if (toDate.after(fromDate)) {
            future = true
        }
        val sb = StringBuilder()
        val types = intArrayOf(
            Calendar.YEAR,
            Calendar.MONTH,
            Calendar.DAY_OF_MONTH,
            Calendar.HOUR_OF_DAY,
            Calendar.MINUTE,
            Calendar.SECOND
        )
        val names = arrayOf(
            "ano",
            "anos",
            "mês",
            "meses",
            "dia",
            "dias",
            "hora",
            "horas",
            "minuto",
            "minutos",
            "segundo",
            "segundos"
        )
        var accuracy = 0
        for (i in types.indices) {
            if (accuracy > 2) {
                break
            }
            val diff = dateDiff(types[i], fromDate, toDate, future)
            if (diff > 0) {
                accuracy++
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + (if (diff > 1) 1 else 0)])
            }
        }
        return if (sb.length == 0) {
            "alguns milisegundos"
        } else sb.toString().trim { it <= ' ' }
    }
}

fun OffsetDateTime.humanize(): String {
    val month = when (this.month) {
        Month.JANUARY -> "Janeiro"
        Month.FEBRUARY -> "Fevereiro"
        Month.MARCH -> "Março"
        Month.APRIL -> "Abril"
        Month.MAY -> "Maio"
        Month.JUNE -> "Junho"
        Month.JULY -> "Julho"
        Month.AUGUST -> "Agosto"
        Month.SEPTEMBER -> "Setembro"
        Month.OCTOBER -> "Outubro"
        Month.NOVEMBER -> "Novembro"
        Month.DECEMBER -> "Dezembro"

        else -> "Irineu, você não sabe e nem eu!"
    }

    return "${this.dayOfMonth} de $month de ${this.year} às ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}:${this.second.toString().padStart(2, '0')}"
}