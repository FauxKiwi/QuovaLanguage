package quova.compiler

data class QuovaModule(
    val project: String,
    val group: String,
    val version: String,
    val buildContext: BuildContext
) {
    data class BuildContext(
        val repositories: MutableList<String>,
        val dependencies: List<Dependency>,
        val variables: List<Variable>,
        val others: List<OtherOption>
    ) {
        init {
            if (!repositories.contains("mavenCentral"))
                repositories.add("mavenCentral")
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

    fun gradleBuild(): String = buildString {
        append("""
            plugins {
                id 'org.jetbrains.kotlin.jvm' version '1.4.10'
            }
        """.trimIndent())
        append("\n\ngroup = \"")
        append(group)
        append("\"\nversion = \"")
        append(version)
        append("\"\n\n")
        append(buildContext.variables.takeUnless { it.isEmpty() }?.joinToString(
            "\n\t", "ext {\n\t", "\n}\n\n"
        ) ?: "")
        append(buildContext.repositories.takeUnless { it.isEmpty() }?.joinToString(
            "\n\t", "repositories {\n\t", "\n}\n\n", transform = { "$it()" }
        ) ?: "")
        append(buildContext.dependencies.takeUnless { it.isEmpty() }?.joinToString(
            "\n\t", "dependencies {\n\t", "\n}\n\n"
        ) ?: "")
        append(buildContext.others.takeUnless { it.isEmpty() }?.joinToString("\n\n") ?: "")
    }

    fun gradleSettings(): String = "rootProject.name = '$project'"
}