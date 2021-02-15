import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import quova.antlr.gen.QuovaLexer
import quova.antlr.gen.QuovaParser

fun main() {
    val src = """
        void main() {
            println("Hello World!");
        }
    """.trimIndent()

    val lexer = QuovaLexer(CharStreams.fromString(src))
    val parser = QuovaParser(CommonTokenStream(lexer))

    parser.quovaFile()
}