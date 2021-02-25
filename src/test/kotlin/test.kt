@file:JvmName("Tests")

import quova.asIntersection
import quova.compiler.QuovaCompiler
import quova.type
import java.io.File
import java.io.FileReader
import java.io.Serializable

fun main() {
    //quova.compiler.Compiler.compile(module = File("D:\\IdeaProjects\\Quova\\compileTest\\CompileTest.qvm"))
    val file = QuovaCompiler(FileReader(File("./Test.qv")).use { it.readText() }).compile()
    print(file)
}