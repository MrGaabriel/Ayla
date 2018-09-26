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
}