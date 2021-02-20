package quova.compiler

import quova.Either

data class QuovaFile(
    val shebangLine: String?,
    val packageName: String? /*ID*/,
    val imports: List<Import>,
    val declarations: List<Declaration>
) {
    override fun toString(): String = buildString {
        shebangLine?.let {
            append(it)
            nl()
        }
        packageName?.let {
            append("package ")
            append(it)
            nl()
        }
        imports.forEach {
            append(it)
            nl()
        }
        declarations.forEach {
            append(it)
            nl()
        }
    }

    companion object {
        const val standardImports = """import kotlin.Byte as `Byte-`
import kotlin.UByte as `UByte-`
import kotlin.Short as `Short-`
import kotlin.UShort as `UShort-`
        """
    }
}

data class Import(
    val location: String /*ID*/,
    val all: Boolean,
    val subImports: List<String>,
    val alias: String?
) {
    override fun toString(): String =
        if (subImports.isEmpty()) buildString {
            append("import ")
            append(location)
            if (all)
                append(".*")
            else
                alias?.let {
                    append(" as ")
                    append(it)
                }
        } else subImports.joinToString("\n") {
            "import $location.$it"
        }
}

///////////////////////////////////////////////////////////////////////////
// Declarations
///////////////////////////////////////////////////////////////////////////

interface Declaration : ClassMember, SingletonMember, Statement
// :-> TypeDeclaration, FunctionDeclaration, PropertyDeclaration

data class TypeDeclaration(
    val annotations: List<Annotation>,
    val visibility: VisibilityModifier,
    val type: Type
) : Declaration {
    interface Type
    // :-> ClassDeclaration, SingletonDeclaration, InterfaceDeclaration, EnumClassDeclaration,
    //     PrimitiveEnumDeclaration, RecordDeclaration, InlineClassDeclaration, AnnotationDeclaration

    override fun toString(): String = buildString {
        annotations.joinTo(this, " ")
        if (annotations.isNotEmpty()) nl()
        append(visibility)
        append(' ')
        append(type)
    }
}

data class ClassDeclaration(
    val inheritance: InheritanceModifier?,
    val sealed: Boolean,
    val inner: Boolean,
    val name: String,
    val typeParameters: List<VariantTypeParameter>,
    val parameters: List<ValueParameter>,
    val supertypes: List<Supertype>,
    val members: List<ClassMember>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        inheritance?.let {
            append(it)
            append(' ')
        }
        if (inheritance != InheritanceModifier.FINAL)
            append("open ")
        if (sealed)
            append("sealed ")
        if (inner)
            append("inner ")
        append("class ")
        append(name)
        if (typeParameters.isNotEmpty())
            typeParameters.joinTo(this, prefix = "<", postfix = ">")
        if (parameters.isNotEmpty())
            parameters.joinTo(this, prefix = "(", postfix = ")")
        if (supertypes.isNotEmpty())
            supertypes.joinTo(this, prefix = " : ")
        if (members.isNotEmpty())
            members.joinTo(this, "\n", " {\n", "\n}")
    }
}

data class SingletonDeclaration(
    val name: String,
    val supertypes: List<Supertype>,
    val members: List<SingletonMember>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        append("object ")
        append(name)
        if (supertypes.isNotEmpty())
            supertypes.joinTo(this, prefix = " : ")
        if (members.isNotEmpty())
            members.joinTo(this, "\n", " {\n", "\n}")
    }
}

data class InterfaceDeclaration(
    val name: String,
    val typeParameters: List<VariantTypeParameter>,
    val supertypes: List<UserType>,
    val members: List<Declaration>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        append("interface ")
        append(name)
        if (typeParameters.isNotEmpty())
            typeParameters.joinTo(this, prefix = "<", postfix = ">")
        if (supertypes.isNotEmpty())
            supertypes.joinTo(this, prefix = " : ")
        if (members.isNotEmpty())
            members.joinTo(this, "\n", " {\n", "\n}")
    }
}

data class EnumClassDeclaration(
    val name: String,
    val typeParameters: List<VariantTypeParameter>,
    val parameters: List<ValueParameter>,
    val supertypes: List<Supertype>,
    val enumEntries: List<EnumEntry>,
    val members: List<ClassMember>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        append("enum class ")
        append(name)
        if (typeParameters.isNotEmpty())
            typeParameters.joinTo(this, prefix = "<", postfix = ">")
        if (parameters.isNotEmpty())
            parameters.joinTo(this, prefix = "(", postfix = ")")
        if (supertypes.isNotEmpty())
            supertypes.joinTo(this, prefix = " : ")
        if (enumEntries.isNotEmpty() || members.isNotEmpty()) {
            append(" {\n")
            if (enumEntries.isNotEmpty())
                enumEntries.joinTo(this, ",\n", postfix = ";\n")
            if (members.isNotEmpty())
                members.joinTo(this, "\n")
            append("\n}")
        }
    }
}

data class PrimitiveEnumDeclaration(
    val bitfield: Boolean,
    val type: Type,
    val name: String,
    val entries: List<Entry>
) : TypeDeclaration.Type {
    enum class Type(val string: String) {
        BYTE("UByte"), SBYTE("Byte"), SHORT("Short"), USHORT("UShort"), INT("Int"),
        UINT("UInt"), LONG("Long"), ULONG("ULong");

        override fun toString(): String = string
    }

    data class Entry(
        val name: String,
        val value: Value?
    ) {
        data class Value(
            val type: Type,
            val value: Expression
        ) {
            enum class Type {
                ASSIGN, SHIFT
            }
        }
    }

    override fun toString(): String = buildString {
        append("object ")
        append(name)
        if (entries.isNotEmpty()) {
            append(" {\n")
            var count = 0
            var lastExpression: Expression = LiteralLiteral("0")
            entries.forEach { entry ->
                append("const val ")
                append(entry.name)
                append(": ")
                append(type)
                append(" = ")
                if (entry.value != null) {
                    if (entry.value.type == Entry.Value.Type.SHIFT)
                        append(OperatorExpression.Shift(
                            LiteralLiteral("1"),
                            entry.value.value,
                            OperatorExpression.Shift.Operator.LEFT
                        ))
                    else
                        append(entry.value.value)
                    lastExpression = entry.value.value
                    count = 1
                } else {
                    val value = OperatorExpression.Sum(
                        lastExpression,
                        LiteralLiteral(count.toString()),
                        OperatorExpression.Sum.Operator.PLUS
                    )
                    if (bitfield)
                        append(OperatorExpression.Shift(
                            LiteralLiteral("1"),
                            value,
                            OperatorExpression.Shift.Operator.LEFT
                        ))
                    else
                        append(value)
                    ++count
                }
                append('\n')
            }
            append('}')
        }
    }
}

data class RecordDeclaration(
    val name: String,
    val typeParameters: List<VariantTypeParameter>,
    val parameters: List<ValueParameter>,
    val members: List<ClassMember>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        append("data class ")
        append(name)
        if (typeParameters.isNotEmpty())
            typeParameters.joinTo(this, prefix = "<", postfix = ">")
        if (parameters.isNotEmpty())
            parameters.joinTo(this, prefix = "(", postfix = ")") {
                if (it.variadic) "var $it"
                else "val $it"
            }
        if (members.isNotEmpty())
            members.joinTo(this, "\n", " {\n", "\n}")
    }
}

data class InlineClassDeclaration(
    val name: String,
    val value: ValueParameter,
    val members: List<ClassMember>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        append("inline class ")
        append(name)
        append('(')
        if(value.variadic)
            append("var ")
        else
            append("val ")
        append(value)
        append(')')
        if (members.isNotEmpty())
            members.joinTo(this, "\n", " {\n", "\n}")
    }
}

data class AnnotationDeclaration(
    val name: String,
    val parameters: List<ValueParameter>,
    val members: List<ClassMember>
) : TypeDeclaration.Type {
    override fun toString(): String = buildString {
        append("annotation class ")
        append(name)
        if (parameters.isNotEmpty())
            parameters.joinTo(this, prefix = "(", postfix = ")")
        if (members.isNotEmpty())
            members.joinTo(this, "\n", " {\n", "\n}")
    }
}

data class FunctionDeclaration(
    val annotations: List<Annotation>,
    val visibility: VisibilityModifier,
    val inheritance: InheritanceModifier?,
    val modifiers: List<FunctionModifier>,
    val typeParameters: List<TypeParameter>,
    val type: Either<Type, VOID>?,
    val name: String,
    val parameters: List<ValueParameter>,
    val throwExceptions: List<UserType>,
    val body: Either<Block, Expression>?
) : Declaration {
    override fun toString(): String = buildString {
        if (throwExceptions.isNotEmpty())
            throwExceptions.joinTo(this, prefix = "@Throws(", postfix = ")\n") {
                "$it::class"
            }
        if (modifiers.contains(FunctionModifier.STRICTFP))
            append("@Strictfp\n")
        if (modifiers.contains(FunctionModifier.SYNCHRONIZED))
            append("@Synchronized\n")
        if (annotations.isNotEmpty())
            annotations.joinTo(this, " ", postfix = "\n")
        append(visibility)
        append(' ')
        var override = false
        annotations.forEach {
            if (it.name == "Override")
                override = true
        }
        if (override)
            append("override ")
        inheritance?.let {
            append(it)
            append(' ')
        }
        /*if (inheritance != InheritanceModifier.FINAL)
            append("open ")*/ //TODO: Allow
        modifiers.forEach {
            if (it != FunctionModifier.STRICTFP && it != FunctionModifier.STATIC && it != FunctionModifier.SYNCHRONIZED) {
                append(it)
                append(' ')
            }
        }
        append("fun ")
        if (typeParameters.isNotEmpty())
            typeParameters.joinTo(this, prefix = "<", postfix = "> ")
        append(name)
        parameters.joinTo(this, prefix = "(", postfix = ")")
        type?.let {
            append(": ")
            append(it)
        }
        body?.either({
            append(' ')
            append(it)
        }, {
            append(" = ")
            append(it)
        })
    }
}

data class PropertyDeclaration(
    val annotations: List<Annotation>,
    val visibility: VisibilityModifier,
    val inheritance: InheritanceModifier?,
    val modifiers: List<PropertyModifier>,
    val type: Either<Type, VAR>,
    val properties: List<Property>
) : Declaration {
    override fun toString(): String = buildString {
        val common = buildString {
            if (modifiers.contains(PropertyModifier.TRANSIENT))
                append("@Transient\n")
            if (modifiers.contains(PropertyModifier.VOLATILE))
                append("@Volatile\n")
            if (annotations.isNotEmpty())
                annotations.joinTo(this, " ", postfix = "\n")
            append(visibility)
            append(' ')
            inheritance?.let {
                append(it)
                append(' ')
            }
            if (modifiers.contains(PropertyModifier.CONST))
                append("const ")
            append(
                if (modifiers.contains(PropertyModifier.READONLY) || inheritance == InheritanceModifier.FINAL || modifiers.contains(PropertyModifier.CONST))
                    "val"
                else
                    "var"
            )
        }
        val type = if (type is Either.A) ": $type" else ""
        properties.joinTo(this, "\n") {
            "$common ${it.name}$type$it"
        }
    }
}

data class Property(
    val name: String,
    val getter: Getter?,
    val setter: Setter?,
    val value: Expression?, // value and delegate are exclusive
    val delegate: Expression?
) {
    override fun toString(): String = buildString { // without name
        value?.let {
            append(" = ")
            append(it)
        }
        delegate?.let {
            append(" by ")
            append(it)
        }
        getter?.let {
            nl()
            append(it)
        }
        setter?.let {
            nl()
            append(it)
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Members
///////////////////////////////////////////////////////////////////////////

interface ClassMember
// :-> Declaration, Constructor, InitBlock

interface SingletonMember
// :-> Declaration, InitBlock

data class Constructor(
    val annotations: List<Annotation>,
    val visibility: VisibilityModifier,
    val typeParameters: List<TypeParameter>,
    val parameters: List<ValueParameter>,
    val delegates: List<Delegation>,
    val body: Either<Block, Expression>?
) : ClassMember {
    data class Delegation(
        val by: Either<THIS, SuperLiteral>,
        val arguments: List<ValueArgument>
    ) {
        override fun toString(): String = buildString {
            append(by)
            arguments.joinTo(this, prefix = "(", postfix = ")")
        }
    }

    override fun toString(): String = buildString {
        if (annotations.isNotEmpty())
            annotations.joinTo(this, " ", postfix = "\n")
        append(visibility)
        append(' ')
        append("constructor")
        if (typeParameters.isNotEmpty())
            typeParameters.joinTo(this, prefix = "<", postfix = "> ")
        if (parameters.isNotEmpty())
            parameters.joinTo(this, prefix = "(", postfix = ")")
        if (delegates.isNotEmpty())
            delegates.joinTo(this, prefix = " : ")
        body?.either({
            append(' ')
            append(it)
        }, {
            append(" = ")
            append(it)
        })
    }
}

data class InitBlock(
    val static: Boolean,
    val block: Block
) : ClassMember, SingletonMember {
    override fun toString(): String = "init $block"
}

data class EnumEntry(
    val name: String,
    val arguments: List<ValueArgument>,
    val definedMembers: List<ClassMember>
) {
    override fun toString(): String = buildString {
        append(name)
        arguments.joinTo(this, prefix = "(", postfix = ")")
        if (definedMembers.isNotEmpty())
            definedMembers.joinTo(this, "\n", " {\n", "\n}")
    }
}

///////////////////////////////////////////////////////////////////////////
// Parameters / Arguments
///////////////////////////////////////////////////////////////////////////

data class ValueParameter(
    val annotations: List<Annotation>,
    val variadic: Boolean,
    val type: Type,
    val vararg: Boolean,
    val name: String,
    val default: Expression?
) {
    override fun toString(): String = buildString {
        annotations.forEach {
            append(it)
            append(' ')
        }
        if (vararg)
            append("vararg ")
        append(name)
        //TODO: variadic
        append(": ")
        append(type)
        default?.let {
            append(" = ")
            append(it)
        }
    }
}

data class TypeParameter(
    val annotations: List<Annotation>,
    val reified: Boolean,
    val name: String,
    val constraints: List<Type>
) {
    override fun toString(): String = buildString {
        annotations.forEach {
            append(it)
            append(' ')
        }
        if (reified)
            append("reified ")
        append(name)
        if (constraints.isNotEmpty()) {
            append(" : ")
            append(constraints[0])
        }
    }
}

data class VariantTypeParameter(
    val annotations: List<Annotation>,
    val variance: VarianceModifier?,
    val name: String,
    val constraints: List<Type>
) {
    override fun toString(): String = buildString {
        annotations.forEach {
            append(it)
            append(' ')
        }
        variance?.let {
            append(it)
            append(' ')
        }
        append(name)
        if (constraints.isNotEmpty()) {
            append(" : ")
            append(constraints[0])
        }
    }
}

data class ValueArgument(
    val name: String?,
    val value: Expression
) {
    override fun toString(): String = buildString {
        name?.let {
            append(it)
            append(" = ")
        }
        append(value)
    }
}

data class TypeArgument(
    val known: Boolean , // false means '?'
    val variance: VarianceModifier?,
    val type: Type?
) {
    override fun toString(): String = buildString {
        if (known) {
            variance?.let {
                append(it)
                append(' ')
            }
            append(type)
        } else
            return "*"
    }
}

data class Supertype(
    val type: UserType,
    val argumentsOrDelegate: Either<List<ValueArgument>, Expression>,
) {
    override fun toString(): String = buildString {
        append(type)
        argumentsOrDelegate.either({
            if (it.isNotEmpty())
                it.joinTo(this, prefix = "(", postfix = ")")
        }, {
            append(" by ")
            append(it)
        })
    }
}

data class Getter(
    val annotations: List<Annotation>,
    val body: Either<Block, Expression>?
) {
    override fun toString(): String = buildString {
        if (annotations.isNotEmpty())
            annotations.joinTo(this, " ", postfix = "\n")
        append("get")
        body?.either({
            append("() ")
            append(it)
        }, {
            append("() = ")
            append(it)
        })
    }
}

data class Setter(
    val annotations: List<Annotation>,
    val parameterAnnotations: List<Annotation>,
    val parameterName: String?,
    val body: Either<Block, Expression>?
) {
    override fun toString(): String = buildString {
        if (annotations.isNotEmpty())
            annotations.joinTo(this, " ", postfix = "\n")
        append("set")
        if (parameterName != null) {
            append('(')
            parameterAnnotations.forEach {
                append(it)
                append(' ')
            }
            append(parameterName)
            append(')')
        }
        body?.either({
            append(' ')
            append(it)
        }, {
            append(" = ")
            append(it)
        })
    }
}

///////////////////////////////////////////////////////////////////////////
// Statements
///////////////////////////////////////////////////////////////////////////

interface Statement : StatementBody
// :-> LabeledStatement, Assignment, IfStatement, SwitchExpression, ForStatement, WhileStatement, JumpStatement, Block,
//    Declaration, Expression

interface SimpleStatement
// :-> IfStatement, LoopStatement, Block, Expression

data class LabeledStatement(
    val label: String,
    val statement: Statement
) : Statement {
    override fun toString(): String = "$label@ $statement"
}

data class Assignment(
    val left: Expression,
    val right: Expression,
    val operator: AssignmentOperator
) : Statement {
    override fun toString(): String =
        if (operator.nonNative)
            "$left = $left $operator $right"
        else
            "$left $operator $right"
}

data class IfStatement(
    val condition: Expression,
    val ifBody: StatementBody,
    val elseBody: StatementBody?
) : Statement, SimpleStatement {
    override fun toString(): String = buildString {
        append("if (")
        append(condition)
        append(") ")
        append(ifBody)
        elseBody?.let {
            append(" else ")
            append(it)
        }
    }
}

data class ForStatement(
    val condition: Either<ClassicCondition, ForEachCondition>,
    val body: StatementBody
) : Statement, SimpleStatement {
    data class ClassicCondition(
        val variables: List<VarDeclaration>,
        val expressions: List<Expression>,
        val statements: List<SimpleStatement>
    ) {
        data class VarDeclaration(
            val type: Either<Type, VAR>?,
            val name: String,
            val value: Expression
        ) {
            override fun toString(): String = buildString {
                append(type ?: "var")
                append(' ')
                append(name)
                append(" = ")
                append(value)
            }
        }
    }

    data class ForEachCondition(
        val type: Either<Type, VAR>?,
        val name: String,
        val inValue: Expression
    ) {
        override fun toString(): String = buildString {
            append(name)
            if (type is Either.A) {
                append(": ")
                append(type.value)
            }
            append(" in ")
            append(inValue)
        }
    }

    override fun toString(): String = condition.either({
        buildString {
            append("run {")
            it.variables.forEach {
                append(it)
                nl()
            }
            append("while (")
            if (it.expressions.size <= 1)
                it.expressions.forEach {
                    append(it)
                }
            else
                it.expressions.joinTo(this, "&&") { ex ->
                    "($ex)"
                }
            append(") {\n")
            when (body) {
                is Block -> {
                    body.statements.forEach { st ->
                        append(st)
                        nl()
                    }
                }
                is Statement -> {
                    append(body)
                }
            }
            it.statements.forEach { st ->
                append(st)
                nl()
            }
            append("}}")
        }
    }, {
        buildString {
            append("for (")
            append(it)
            append(") ")
            append(body)
        }
    })
}

data class WhileStatement(
    val bodyFirst: Boolean, // true for do-while, else false
    val condition: Expression,
    val body: StatementBody
) : Statement, SimpleStatement {
    override fun toString(): String = if (bodyFirst) buildString {
        append("do ")
        append(body)
        append(" while (")
        append(condition)
        append(')')
    } else buildString {
        append(" while (")
        append(condition)
        append(") ")
        append(body)
    }
}

data class JumpStatement(
    val order: Order,
    val at: String?, // for break and return
    val value: Expression? // for throw and return
) : Statement {
    enum class Order(val string: String) {
        BREAK("break"), CONTINUE("continue"), THROW("throw"), RETURN("return");

        override fun toString(): String = string
    }

    override fun toString(): String = buildString {
        append(order)
        at?.let {
            append('@')
            append(it)
        }
        value?.let {
            append(' ')
            append(it)
        }
    }
}

interface StatementBody
// :-> Block, Statement, PASS

object PASS : StatementBody {
    override fun toString(): String = ";"
}

///////////////////////////////////////////////////////////////////////////
// Expressions
///////////////////////////////////////////////////////////////////////////

interface Expression : Statement, SimpleStatement {
    val priority: Int

    fun surroundIfHigher(other: Expression): String = (if (other.priority > priority) "($other)" else other.toString())/*.also {
        println("_DEBUG: ${this::class} §$priority ~|~ ${other::class} §${other.priority} '>>> $it")
    }*/
}
// :-> PrimaryExpression, OperatorExpression, ListComprehension

open class Priority(override val priority: Int) : Expression

data class Block(
    val statements: List<Statement>
) : Statement, SimpleStatement, StatementBody {
    override fun toString(): String = statements.joinToString("\n", "{\n", "\n}")
}

sealed class OperatorExpression(priority: Int) : Priority(priority) {
    data class Postfix(
        val value: Expression,
        val postfix: Postfix
    ) : OperatorExpression(0) {
        interface Postfix

        enum class Operator(val string: String) : Postfix {
            INCR("++"), DECR("--"), NOT_NULL("!!");

            override fun toString(): String = string
        }

        data class Invocation(
            val typeArguments: List<TypeArgument>,
            val valueArguments: List<ValueArgument>
        ) : Postfix {
            override fun toString(): String = buildString {
                if (typeArguments.isNotEmpty())
                    typeArguments.joinTo(this, prefix = "<", postfix = ">")
                valueArguments.joinTo(this, prefix = "(", postfix = ")")
            }
        }

        data class Indexing(
            val indices: List<Expression>
        ) : Postfix {
            override fun toString(): String = indices.joinToString(prefix = "[", postfix = "]")
        }

        data class Call(
            val operator: Operator,
            val value: Either<Expression, CLASS>
        ) : Postfix {
            enum class Operator(val string: String) {
                SAFE("?."), DOT("."), REF("::");

                override fun toString(): String = string
            }

            object CLASS {
                override fun toString(): String = "class"
            }

            override fun toString(): String = "$operator$value"
        }

        override fun toString(): String = "${surroundIfHigher(value)}$postfix"
    }

    data class Prefix(
        val operator: Operator,
        val value: Expression
    ) : OperatorExpression(1) {
        enum class Operator(val string: String, val after: String = "") {
            PLUS("+"), MINUS("-"), INCR("++"), DECR("--"), NOT("!"),
            COMP("quova.internal.bitComplement(", ")"), REF("::");

            override fun toString(): String = string
        }

        override fun toString(): String = "$operator${surroundIfHigher(value)}${operator.after}"
    }

    data class Product(
        val left: Expression,
        val right: Expression,
        val operator: Operator
    ) : OperatorExpression(2) {
        enum class Operator(val string: String) {
            TIMES("*"), DIV("/"), REM("%");

            override fun toString(): String = string
        }

        override fun toString(): String = "${surroundIfHigher(left)} $operator ${surroundIfHigher(right)}"
    }

    data class Sum(
        val left: Expression,
        val right: Expression,
        val operator: Operator
    ) : OperatorExpression(3) {
        enum class Operator(val string: String) {
            PLUS("+"), MINUS("-");

            override fun toString(): String = string
        }

        override fun toString(): String = "${surroundIfHigher(left)} $operator ${surroundIfHigher(right)}"
    }

    data class Range(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(4) {
        override fun toString(): String = "${surroundIfHigher(left)}..${surroundIfHigher(right)}"
    }

    data class Elvis(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(5) {
        override fun toString(): String = "${surroundIfHigher(left)} ?: ${surroundIfHigher(right)}"
    }

    data class NamedCheck(
        val left: Expression,
        val right: Expression,
        val operator: Either<InOperator, InstanceOperator>
    ) : OperatorExpression(6) {
        override fun toString(): String = "${surroundIfHigher(left)} $operator ${surroundIfHigher(right)}"
    }

    data class Cast(
        val type: Type,
        val safe: Boolean,
        val value: Expression
    ) : OperatorExpression(7) {
        override fun toString(): String = "${surroundIfHigher(value)} ${if (safe) "as?" else "as"} $type"
    }

    data class Shift(
        val left: Expression,
        val right: Expression,
        val operator: Operator
    ) : OperatorExpression(8) {
        enum class Operator(val string: String) {
            LEFT("shl"), RIGHT("shr"), U_RIGHT("ushr");

            override fun toString(): String = string
        }

        override fun toString(): String = "${surroundIfHigher(left)} $operator ${surroundIfHigher(right)}"
    }

    data class BitAnd(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(9) {
        override fun toString(): String = "${surroundIfHigher(left)} and ${surroundIfHigher(right)}"
    }

    data class BitXor(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(10) {
        override fun toString(): String = "${surroundIfHigher(left)} xor ${surroundIfHigher(right)}"
    }

    data class BitOr(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(11) {
        override fun toString(): String = "${surroundIfHigher(left)} or ${surroundIfHigher(right)}"
    }

    data class Spaceship(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(12) {
        override fun toString(): String = "${surroundIfHigher(left)}.compareTo($right)"
    }

    data class Comparison(
        val left: Expression,
        val right: Expression,
        val operator: Operator
    ) : OperatorExpression(13) {
        enum class Operator(val string: String) {
            LT("<"), GT(">"), LE("<="), GE(">=");

            override fun toString(): String = string
        }

        override fun toString(): String = "${surroundIfHigher(left)} $operator ${surroundIfHigher(right)}"
    }

    data class Equality(
        val left: Expression,
        val right: Expression,
        val operator: Operator
    ) : OperatorExpression(14) {
        enum class Operator(val string: String) {
            EQ("=="), NOT_EQ("!="), IDENTITY("==="), NOT_IDENTITY("!==");

            override fun toString(): String = string
        }

        override fun toString(): String = "${surroundIfHigher(left)} $operator ${surroundIfHigher(right)}"
    }

    data class Conjunction(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(15) {
        override fun toString(): String = "${surroundIfHigher(left)} && ${surroundIfHigher(right)}"
    }

    data class Disjunction(
        val left: Expression,
        val right: Expression
    ) : OperatorExpression(16) {
        override fun toString(): String = "${surroundIfHigher(left)} || ${surroundIfHigher(right)}"
    }

    data class Ternary(
        val condition: Expression,
        val ifTrue: Expression,
        val ifFalse: Expression
    ) : OperatorExpression(17) {
        override fun toString(): String = "if ($condition) ${surroundIfHigher(ifTrue)} else ${surroundIfHigher(ifFalse)}"
    }

    data class Spread(
        val value: Expression
    ) : OperatorExpression(18) {
        override fun toString(): String = "*${surroundIfHigher(value)}"
    }

    data class Assignment(
        val left: Expression,
        val right: Expression,
        val operator: AssignmentOperator
    ) : OperatorExpression(19) {
        override fun toString(): String = if (operator.nonNative)
            "($left $operator $right).also { `av-` -> $left = `av-` }"
        else
            "$right.also { `av-` -> $left $operator `av-` }"
    }
}

open class PrimaryExpression : Priority(-1)
// :-> Expression, ConstructorInvocation, Literal, Identifier

data class ListComprehension(
    val mapping: Expression,
    val forEach: ForStatement.ForEachCondition,
    val ifCondition: Expression?
) : Priority(20) {
    override fun toString(): String = buildString {
        append(forEach.inValue)
        val lambdaParameter = buildString {
            append(forEach.name)
            if (forEach.type is Either.A) {
                append(": ")
                append(forEach.type)
            }
        }
        ifCondition?.let {
            append(".filter { ")
            append(lambdaParameter)
            append(" -> ")
            append(it)
            append(" }")
        }
        append(".map { ")
        append(lambdaParameter)
        append(" -> ")
        append(mapping)
        append(" }")
    }
}

data class ConstructorInvocation(
    val type: UserType,
    val typeArguments: List<TypeArgument>,
    val arguments: Either<List<ValueArgument>, InitializerList>
) : PrimaryExpression() {
    override fun toString(): String = buildString {
        append(type)
        if (typeArguments.isNotEmpty())
            typeArguments.joinTo(this, prefix = "<", postfix = ">")
        val args = arguments.either({ it }, { (it.arguments as Either.A).value }) //TODO: Dictionary initializer
        args.joinTo(this, prefix = "(", postfix = ")")
    }
}

interface Switch : Statement
// :-> SwitchExpression, WhenExpression

data class SwitchExpression(
    val subject: Expression,
    val branches: List<Branch>,
    val elseBody: StatementBody?
) : PrimaryExpression(), Switch {
    data class Branch(
        val condition: List<Condition>,
        val body: StatementBody
    ) {
        data class Condition(
            val type: Type,
            val values: Expression
        ) {
            interface Type
            // :-> CASE, InOperator, IsOperator

            object CASE : Type

            override fun toString(): String = buildString {
                if (type !is CASE) {
                    append(type)
                    append(' ')
                }
                append(values)
            }
        }

        override fun toString(): String = buildString {
            condition.joinTo(this)
            append(" -> ")
            append(body)
        }
    }

    override fun toString(): String = buildString {
        append("when (")
        append(subject)
        append(") {\n")
        branches.forEach {
            append(it)
            nl()
        }
        elseBody?.let {
            append(it)
            nl()
        }
        append('}')
    }
}

data class WhenExpression(
    val branches: List<Branch>,
    val elseBody: StatementBody?
) : PrimaryExpression(), Switch {
    data class Branch(
        val conditions: List<Expression>,
        val body: StatementBody
    ) {
        override fun toString(): String = buildString {
            conditions.joinTo(this)
            append(" -> ")
            append(body)
        }
    }

    override fun toString(): String = buildString {
        append("when {\n")
        branches.forEach {
            append(it)
            nl()
        }
        elseBody?.let {
            append(it)
            nl()
        }
        append('}')
    }
}

///////////////////////////////////////////////////////////////////////////
// Literals
///////////////////////////////////////////////////////////////////////////

open class Literal : PrimaryExpression()
// :-> StringLiteral, MultilineStringLiteral, THIS, SuperLiteral, IntLiteral, LongLiteral, RealLiteral, CharLiteral,
//     TRUE, FALSE, NULL, VOID, Lambda, InitializerList

data class StringLiteral(
    val content: String
) : Literal() {
    override fun toString(): String = content
}

data class MultilineStringLiteral(
    val content: String
) : Literal() {
    override fun toString(): String = "\"\"$content\"\""
}

object THIS : Literal() {
    override fun toString(): String = "this"
}

data class SuperLiteral(
    val type: UserType?
) : Literal() {
    override fun toString(): String = "super${type?.let { "<$it>" } ?: ""}"
}

data class LiteralLiteral(
    val value: String
) : Literal() {
    override fun toString(): String = value
}

object NULL : Literal() {
    override fun toString(): String = "null"
}

data class Lambda(
    val parameters: List<Parameter>,
    val body: Either<Expression, Body>
) : Literal() {
    data class Parameter(
        val annotations: List<Annotation>,
        val type: Either<Type, VAR>?,
        val name: String? // null means '_'
    ) {
        override fun toString(): String = buildString {
            annotations.forEach {
                append(it)
                append(' ')
            }
            if (name != null)
                append(name)
            else
                append('_')
            if (type is Either.A) {
                append(": ")
                append(type)
            }
        }
    }

    data class Body(
        val statements: List<Statement>,
        val lastExpression: Expression?
    ) {
        override fun toString(): String = buildString {
            (lastExpression?.let { statements + it } ?: statements).joinTo(this, "\n")
        }
    }

    override fun toString(): String = buildString {
        append('{')
        if (parameters.isNotEmpty())
            parameters.joinTo(this, prefix = " ", postfix = " ->")
        body.either({
            append(' ')
            append(it)
            append(' ')
        }, {
            append('\n')
            append(it)
            append('\n')
        })
        append('}')
    }
}

data class InitializerList(
    val arguments: Either<List<ValueArgument>, List<Pair<Expression, Expression>>> // Normal <-> Dictionary
) : Literal() {
    override fun toString(): String = buildString {
        //TODO: Constructors etc.
        arguments.either({
            it.joinTo(this, prefix = "quova.internal.fittingArray(", postfix = ")")
        }, {
            it.joinTo(this, prefix = "hashMapOf(", postfix = ")") { pair ->
                "${pair.first} to ${pair.second}"
            }
        })
    }
}

///////////////////////////////////////////////////////////////////////////
// Operators
///////////////////////////////////////////////////////////////////////////

enum class AssignmentOperator(val string: String, val nonNative: Boolean = false) {
    ASSIGN("="), PLUS("+="), MINUS("-="), TIMES("*="), DIV("/="), MOD("%="),
    AND("and", true), XOR("xor", true), OR("or", true),
    SHL("shl", true), SHR("shr", true), USHR("ushr", true),
    COALESCING("?:", true);

    override fun toString(): String = string
}

enum class InOperator(val string: String) : SwitchExpression.Branch.Condition.Type {
    IN(" in "), NOT_IN(" !in ");

    override fun toString(): String = string
}

enum class InstanceOperator(val string: String) : SwitchExpression.Branch.Condition.Type {
    INSTANCEOF(" is "), NOT_INSTANCEOF(" !is ");

    override fun toString(): String = string
}

///////////////////////////////////////////////////////////////////////////
// Types
///////////////////////////////////////////////////////////////////////////

object VOID : Literal() {
    override fun toString(): String = "Unit"
}

object VAR {
    override fun toString(): String = "var"
}

data class Type(
    val type: Type,
    val arrayDim: Int,
    val nullable: Boolean
) {
    interface Type
    // :-> TypeReference, FunctionType

    override fun toString(): String = buildString {
        repeat(arrayDim) {
            append("Array<")
        }
        append(type)
        repeat(arrayDim) {
            append(">")
        }
        if (nullable)
            append('?')
    }
}

interface TypeReference : Type.Type
// :-> PrimitiveType, UserType

data class PrimitiveType(
    val type: Type,
    val array: Boolean
) : TypeReference {
    enum class Type(val string: String) {
        BYTE("UByte"), SBYTE("Byte"), SHORT("Short"), USHORT("UShort"), INT("Int"),
        UINT("UInt"), LONG("Long"), ULONG("ULong"), FLOAT("Float"), DOUBLE("Double"),
        CHAR("Char"), BOOLEAN("Boolean");

        override fun toString(): String = string
    }

    override fun toString(): String = "$type${if (array) "Array" else ""}"
}

data class UserType(
    val name: String /*ID*/,
    val typeArguments: List<TypeArgument>
) : TypeReference {
    override fun toString(): String = buildString {
        append(name)
        if (typeArguments.isNotEmpty())
            typeArguments.joinTo(this, prefix = "<", postfix = ">")
    }
}

data class FunctionType(
    val suspend: Boolean,
    val returnType: Type,
    val parameterTypes: List<Type>
) : Type.Type {
    override fun toString(): String = buildString {
        if (suspend)
            append("suspend ")
        parameterTypes.joinTo(this, prefix = "(", postfix = ")")
        append(" -> ")
        append(returnType)
    }
}

data class Annotation(
    val name: String /*ID*/,
    val typeArguments: List<TypeArgument>,
    val valueArguments: List<ValueArgument>
) {
    override fun toString(): String = buildString {
        append('@')
        append(name)
        if (typeArguments.isNotEmpty())
            typeArguments.joinTo(this, prefix = "<", postfix = ">")
        if (valueArguments.isNotEmpty())
            valueArguments.joinTo(this, prefix = "(", postfix = ")")
    }
}

///////////////////////////////////////////////////////////////////////////
// Modifiers
///////////////////////////////////////////////////////////////////////////

enum class FunctionModifier(val string: String = "null") {
    INLINE("inline"), TAILREC("tailrec"), STATIC("static"), DEFAULT("default"),
    SUSPEND("suspend"), STRICTFP, SYNCHRONIZED, NATIVE("external");

    override fun toString(): String = string
}

enum class VisibilityModifier(val string: String) {
    PUBLIC("public"), PRIVATE("private"), INTERNAL("internal"), PROTECTED("protected");

    override fun toString(): String = string
}

enum class InheritanceModifier(val string: String) {
    FINAL("final"), ABSTRACT("abstract");

    override fun toString(): String = string
}

enum class VarianceModifier(val string: String) {
    IN("in"), OUT("out");

    override fun toString(): String = string
}

enum class PropertyModifier(val string: String = "null") {
    CONST("const"), READONLY, STATIC("static"), VOLATILE, TRANSIENT;

    override fun toString(): String = string
}

///////////////////////////////////////////////////////////////////////////
// Identifiers
///////////////////////////////////////////////////////////////////////////

/*data class Identifier(
    val simpleIdentifiers: List<String>
)*/