@file:JvmName("Tests")

import quova.compiler.QuovaCompiler
import java.io.File
import java.io.FileReader

fun main() {
    //quova.compiler.Compiler.compile(File("D:\\IdeaProjects\\Quova\\compileTest\\CompileTest.qvm"))
    val file = QuovaCompiler(FileReader(File("./Features.qv")).use { it.readText() }).compile()
    print(file)
}

