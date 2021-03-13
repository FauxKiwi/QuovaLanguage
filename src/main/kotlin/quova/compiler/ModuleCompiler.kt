package quova.compiler

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import quova.Either
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
        val plugins = mutableListOf<Either<QuovaModule.BuildContext.Plugin, String>>()
        val repositories = mutableListOf<Pair<String, Boolean>>()
        val dependencies = mutableListOf<Either<QuovaModule.BuildContext.Dependency, String>>()
        val variables = mutableListOf<QuovaModule.BuildContext.Variable>()
        val others = mutableListOf<QuovaModule.BuildContext.OtherOption>()
        for (element in ctx) {
            element.plugins()?.let { plugins.addAll(visit(it)) }
            element.repositories()?.let { repositories.addAll(visit(it)) }
            element.dependencies()?.let { dependencies.addAll(visit(it)) }
            element.variableDeclaration()?.let { variables.add(visit(it)) }
            element.otherOption()?.let { others.add(visit(it)) }
        }
        return QuovaModule.BuildContext(
            plugins, repositories, dependencies, variables, others
        )
    }

    private fun visit(ctx: QuovaModuleParser.PluginsContext): List<Either<QuovaModule.BuildContext.Plugin, String>> =
        ctx.plugin().map { p ->
            p.raw()?.let { Either.B(it.rawContent().text) } ?:
            Either.A(QuovaModule.BuildContext.Plugin(
                p.name().text,
                p.versionName()?.text
            ))
        }

    private fun visit(ctx: QuovaModuleParser.RepositoriesContext): List<Pair<String, Boolean>> =
        ctx.repository().map { r ->
            r.raw()?.let { it.rawContent().text to true } ?:
            r.text to false
        }

    private fun visit(ctx: QuovaModuleParser.DependenciesContext): List<Either<QuovaModule.BuildContext.Dependency, String>> =
        ctx.dependency().map { d ->
            d.raw()?.let { Either.B(it.rawContent().text) } ?:
            Either.A(QuovaModule.BuildContext.Dependency(
                d.IMPL()?.let { QuovaModule.BuildContext.Dependency.Type.IMPL } ?:
                d.ONLY_COMPILE()?.let { QuovaModule.BuildContext.Dependency.Type.COMPILE } ?:
                QuovaModule.BuildContext.Dependency.Type.RUNTIME,
                d.name(0).text,
                d.name(1).text,
                d.versionName().text
            ))
        }

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