package quova.compiler

import quova.Either
import java.io.File

data class QuovaModule(
    val project: String,
    val group: String,
    val version: String,
    val buildContext: BuildContext
) {
    data class BuildContext(
        val plugins: MutableList<Either<Plugin, String>>,
        val repositories: MutableList<Pair<String, Boolean /* Raw */>>,
        val dependencies: List<Either<Dependency, String>>,
        val variables: List<Variable>,
        val others: List<OtherOption>
    ) {
        init {
            if (!repositories.contains("mavenCentral"))
                repositories.add("mavenCentral" to false)
            val plugin: Either<Plugin, String> = Either.A(Plugin("org.jetbrains.kotlin.jvm", "1.4.10"))
            if (!plugins.contains(plugin))
                plugins.add(plugin)
        }

        data class Plugin(
            val id: String,
            val version: String?
        ) {
            override fun toString(): String = "id \"$id\"${version?.let { " version \"$version\"" } ?: ""}"
        }

        data class Dependency(
            val type: Type,
            val group: String,
            val artifact: String,
            val version: String
        ) {
            enum class Type(val gradleName: String) {
                IMPL("implementation"), COMPILE("compileOnly"), RUNTIME("runtimeOnly")
            }

            override fun toString(): String = "${type.gradleName} \"$group:$artifact:$version\""
        }

        data class Variable(
            val name: String,
            val value: String
        ) {
            override fun toString(): String = "$name = $value"
        }

        data class OtherOption(
            val name: String,
            val value: String
        ) {
            override fun toString(): String = "$name = $value"
        }
    }

    fun gradleBuild(libs: File): String = buildString {
        buildContext.plugins.joinTo(
            this, "\n\t", "plugins {\n\t", "\n}"
        )
        append("\n\ngroup = \"")
        append(group)
        append("\"\nversion = \"")
        append(version)
        append("\"\n\n")
        buildContext.variables.takeUnless { it.isEmpty() }?.joinTo(
            this, "\n\t", "ext {\n\t", "\n}\n\n"
        )
        buildContext.repositories.joinTo(
            this, "\n\t", "repositories {\n\tflatDir {\n\t\tdirs '${libs.path.replace("\\", "\\\\")}'\n\t}\n\t", "\n}\n\n"
        ) { if (it.second) it.first else "${it.first}()" }
        append("dependencies {\n\timplementation name: 'stdlib-1.0-SNAPSHOT'")
        buildContext.dependencies.forEach {
            append("\n\t")
            append(it)
        }
        append("\n}")
        buildContext.others.forEach {
            append("\n\n")
            append(it)
        }
    }

    fun gradleSettings(): String = "rootProject.name = '$project'"
}