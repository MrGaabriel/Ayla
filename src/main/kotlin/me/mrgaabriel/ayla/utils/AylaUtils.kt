package me.mrgaabriel.ayla.utils

import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*

object AylaUtils {

    fun randomColor(): Color {
        val random = SplittableRandom()

        return Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @Throws(ClassNotFoundException::class, IOException::class)
    fun getClasses(packageName: String): Array<Class<*>> {
        val classLoader = Thread.currentThread().contextClassLoader!!
        val path = packageName.replace('.', '/')
        val resources = classLoader.getResources(path)
        val dirs = ArrayList<File>()
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            dirs.add(File(resource.file))
        }
        val classes = ArrayList<Class<*>>()
        for (directory in dirs) {
            classes.addAll(findClasses(directory, packageName))
        }
        return classes.toTypedArray()
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    @Throws(ClassNotFoundException::class)
    fun findClasses(directory: File, packageName: String): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        if (!directory.exists()) {
            return classes
        }
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory()) {
                assert(!file.getName().contains("."))
                classes.addAll(findClasses(file, packageName + "." + file.getName()))
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length - 6)))
            }
        }
        return classes
    }

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
        val types = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND)
        val names = arrayOf("ano", "anos", "mês", "meses", "dia", "dias", "hora", "horas", "minuto", "minutos", "segundo", "segundos")
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