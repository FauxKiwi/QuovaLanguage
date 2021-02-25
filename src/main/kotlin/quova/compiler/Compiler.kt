package quova.compiler

import quova.internal.system
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files

object Compiler {
    fun compile(bins: File = File("").absoluteFile, module: File) {
        if (!module.exists()) error("This module file doesn't exist: ${module.path}")
        if (module.isDirectory) error("This is a directory")

        val startTimestamp = System.currentTimeMillis()

        val moduleReader = FileReader(module)
        println("Compiling Quova module ${module.nameWithoutExtension} ...") // (@ ${module.absolutePath})
        val quovaModule = ModuleCompiler(moduleReader.readText()).compile()
        moduleReader.close()
        println("... Done")

        val rootDir = module.absoluteFile.parentFile.absolutePath

        File("$rootDir\\build\\").mkdir()

        println("Checking gradle installation ...")
        copyGradleWrapperFiles(bins.parent, rootDir)
        println("... Done")

        val gradleScript = File("$rootDir\\build\\build.gradle")
        FileWriter(gradleScript).use { writer ->
            writer.write(quovaModule.gradleBuild(File("${bins.parent}\\lib\\")))
        }
        val gradleSettings = File("$rootDir\\build\\settings.gradle")
        FileWriter(gradleSettings).use { writer ->
            writer.write(quovaModule.gradleSettings())
        }

        val quovaSources = File("$rootDir\\src\\main\\quova\\")
        val compiledSources = File("$rootDir\\build\\src\\main\\kotlin\\")
        compiledSources.mkdirs()
        compiledSources.listFiles()?.forEach { it.delete() }

        visitWithAllChildren(quovaSources, {
            println("Compiling file ${it.name}") // (@ ${it.absolutePath}) ...
            true
        }, {
            if (it.isDirectory) {
                File("$rootDir\\build\\src\\main\\kotlin\\${it.name}").mkdirs()
                return@visitWithAllChildren
            }
            val reader = FileReader(it)
            val quovaSrc = reader.readText()
            reader.close()

            val compiledFile = File("$rootDir\\build\\src\\main\\kotlin\\${it.name}.kt")
            compiledFile.createNewFile()

            FileWriter(compiledFile).use { writer ->
                writer.write("@file:JvmName(\"${it.nameWithoutExtension}Qv\")\n${QuovaCompiler(quovaSrc).compile()}")
            }

            println("... Done") // (@ ${compiledFile.absolutePath})
        })

        println("Building classes ...")

        system("gradlew compileKotlin", "./gradlew compileKotlin", "$rootDir\\build\\"/*, redirectInput = false, redirectOutput = false*/)

        println("... Done")

        println("Compilation finished in ${System.currentTimeMillis() - startTimestamp}ms")
    }

    /*private fun quovaToKt(fileName: String, quova: String): String {
        val lexer = QuovaLexer(CharStreams.fromString(quova))
        val parser = QuovaParser(CommonTokenStream(lexer))
        return """
            @file:JvmName("${fileName}Qv")
            fun main() {
                println("Hello World!");
            }
        """.trimIndent()
    }*/

    private fun visitWithAllChildren(dir: File, enter: (File) -> Boolean, leave: (File) -> Unit) {
        val children = dir.listFiles()
        for (child in children) {
            if (!enter(child))
                continue
            if (child.isDirectory)
                visitWithAllChildren(child, enter, leave)
            leave(child)
        }
    }

    private fun copyGradleWrapperFiles(quovaDir: String, rootDir: String) {
        val gradleFolder = File("$rootDir\\build\\gradle\\wrapper\\")
        if (!gradleFolder.exists())
            gradleFolder.mkdirs()
        val wrapperJar = File("$rootDir\\build\\gradle\\wrapper\\gradle-wrapper.jar")
        if (!wrapperJar.exists())
            Files.copy(File("$quovaDir\\ext\\gradleRuntime\\gradle-wrapper.jar").toPath(),
                wrapperJar.toPath()).also { println("Copied Gradle wrapper jar") }
        val wrapperProperties = File("$rootDir\\build\\gradle\\wrapper\\gradle-wrapper.properties")
        if (!wrapperProperties.exists())
            Files.copy(File("$quovaDir\\ext\\gradleRuntime\\gradle-wrapper.properties").toPath(),
                wrapperProperties.toPath()).also { println("Copied Gradle wrapper properties") }
        val gradlewBatch = File("$rootDir\\build\\gradlew.bat")
        if (!gradlewBatch.exists())
            Files.copy(File("$quovaDir\\ext\\gradleRuntime\\gradlew.bat").toPath(),
                gradlewBatch.toPath()).also { println("Copied Gradle wrapper batch") }
        val gradlewUnix = File("$rootDir\\build\\gradlew")
        if (!gradlewUnix.exists())
            Files.copy(File("$quovaDir\\ext\\gradleRuntime\\gradlew").toPath(),
                gradlewUnix.toPath()).also { println("Copied Gradle wrapper unix runnable") }
    }
}

fun main(args: Array<String>) {
    val quovaBinDir = File(args[0]).absoluteFile
    val moduleFile = File(args[1]).absoluteFile
    Compiler.compile(quovaBinDir, moduleFile)
}