package quova.compiler

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import quova.antlr.gen.QuovaModuleLexer
import quova.antlr.gen.QuovaModuleParser

class ModuleCompiler(val src: String) {
    fun compile(): QuovaModule {
        val lexer = QuovaModuleLexer(CharStreams.fromString(src))
        val parser = QuovaModuleParser(CommonTokenStream(lexer))
        return visit(parser.quovaModule())
    }

    private fun visit(ctx: QuovaModuleParser.QuovaModuleContext): QuovaModule =
        QuovaModule(
            ctx.project().name().text,
            ctx.group().name().text,
            ctx.version().versionName().text,
            visit(ctx.element())
        )

    private fun visit(ctx: List<QuovaModuleParser.ElementContext>): QuovaModule.BuildContext {
        val repositories = mutableListOf<String>()
        val dependencies = mutableListOf<QuovaModule.BuildContext.Dependency>()
        val variables = mutableListOf<QuovaModule.BuildContext.Variable>()
        val others = mutableListOf<QuovaModule.BuildContext.OtherOption>()
        for (element in ctx) {
            element.repositories()?.let { repositories.addAll(visit(it)) }
            element.dependencies()?.let { dependencies.addAll(visit(it)) }
            element.variableDeclaration()?.let { variables.add(visit(it)) }
            element.otherOption()?.let { others.add(visit(it)) }
        }
        return QuovaModule.BuildContext(
            repositories, dependencies, variables, others
        )
    }

    private fun visit(ctx: QuovaModuleParser.RepositoriesContext): List<String> =
        List(ctx.name().size) { i ->
            ctx.name(i).text
        }

    private fun visit(ctx: QuovaModuleParser.DependenciesContext): List<QuovaModule.BuildContext.Dependency> =
        List(ctx.dependency().size) { i ->
            visit(ctx.dependency(i))
        }

    private fun visit(ctx: QuovaModuleParser.DependencyContext): QuovaModule.BuildContext.Dependency =
        QuovaModule.BuildContext.Dependency(
            ctx.IMPL()?.let { QuovaModule.BuildContext.Dependency.Type.IMPL } ?:
            ctx.ONLY_COMPILE()?.let { QuovaModule.BuildContext.Dependency.Type.COMPILE } ?:
            QuovaModule.BuildContext.Dependency.Type.RUNTIME,
            ctx.name(0).text,
            ctx.name(1).text,
            ctx.versionName().text
        )

    private fun visit(ctx: QuovaModuleParser.VariableDeclarationContext): QuovaModule.BuildContext.Variable =
        QuovaModule.BuildContext.Variable(
            ctx.name().text,
            ctx.text().text
        )

    private fun visit(ctx: QuovaModuleParser.OtherOptionContext): QuovaModule.BuildContext.OtherOption =
        QuovaModule.BuildContext.OtherOption(
            ctx.name().text,
            ctx.text().text
        )
}