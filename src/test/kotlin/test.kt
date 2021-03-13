@file:JvmName("Tests")

import quova.compiler.ModuleCompiler
import quova.compiler.QuovaCompiler
import java.io.File
import java.io.FileReader

/*@ExperimentalTime
@ExperimentalUnsignedTypes*/
fun main() {
    //quova.compiler.Compiler.compile(module = File("D:\\IdeaProjects\\Quova\\compileTest\\CompileTest.qvm"))
    //val file = QuovaCompiler(FileReader(File("./Test.qv")).use { it.readText() }).compile()
    //print(file)
    /*measureTime {
        println(factorialR(60uL))
    }.also { println(it) }
    measureTime {
        println(factorialT(60uL))
    }.also { println(it) }*/

    val qvm = """
        project: P
        group: g
        version: 1.0
        
        plugins: [ java, com.github.johnrengelman.shadow 2.0.4 ]
        
        repositories:
            - %%{maven {
                url = 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/'

                content {
                    includeGroup 'org.bukkit'
                    includeGroup 'org.spigotmc'
                }
            }}
            - %%{maven {
                url = 'https://oss.sonatype.org/content/repositories/snapshots'
            }}
        
        dependencies:
            - %%{compileOnly "org.bukkit:bukkit:1.14.4-R0.1-SNAPSHOT"}
            - %%{implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"}
        
        sourceCompatibility: 1.8
        targetCompatibility: 1.8
    """.trimIndent()

    val module = ModuleCompiler(qvm).compile()

    val gradleBuild = module.gradleBuild(File(".").absoluteFile)

    println(gradleBuild)
}

/*@ExperimentalUnsignedTypes
fun factorialR(n: ULong, a: ULong = 1uL): ULong {
    if (n <= 1uL)
        return a
    return factorialR(n - 1uL, n * a)
}

@ExperimentalUnsignedTypes
tailrec fun factorialT(n: ULong, a: ULong = 1uL): ULong {
    if (n <= 1uL)
        return a
    return factorialT(n - 1uL, n * a)
}*/