package quova.compiler

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import quova.Either
import quova.antlr.gen.QuovaLexer
import quova.antlr.gen.QuovaParser
import quova.bool
import quova.eitherCatchingA

class QuovaCompiler(val src: String) {
    fun compile(): QuovaFile {
        val lexer = QuovaLexer(CharStreams.fromString(src))
        val parser = QuovaParser(CommonTokenStream(lexer))
        return visit(parser.quovaFile())
    }

    private fun visit(ctx: QuovaParser.QuovaFileContext): QuovaFile =
        QuovaFile(
            ctx.ShebangLine()?.text,
            ctx.identifier()?.text,
            ctx.importHeader().map { visit(it) }
                    + Import("quova", true, listOf(), null)
                    + Import("kotlin.Int", false, listOf(), "Integer"),
            run {
                val list = mutableListOf<Declaration>()
                ctx.declaration().forEach { declaration ->
                    visit(declaration)?.let { list.add(it) }
                }
                list
            }
        )

    private fun visit(ctx: QuovaParser.ImportHeaderContext): Import =
        Import(
            ctx.identifier().text,
            ctx.STAR().bool(),
            ctx.AS()?.let { listOf() } ?: ctx.simpleIdentifier().map { it.text },
            ctx.AS()?.let { ctx.simpleIdentifier().takeUnless { it.isEmpty() }?.get(0) }?.text
        )

    private fun visit(ctx: QuovaParser.DeclarationContext, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL): Declaration? =
        ctx.typeDeclaration()?.let { visit(it, local, standardVisibility) } ?:
        ctx.functionDeclaration()?.let { visit(it, local, standardVisibility) } ?:
        ctx.propertyDeclaration()?.let { visit(it, local, standardVisibility) }

    private fun visit(ctx: QuovaParser.TypeDeclarationContext, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL): TypeDeclaration =
        TypeDeclaration(
            ctx.annotation().map { visit(it) },
            visit(ctx.visibilityModifier(), local, standardVisibility),
            ctx.classDeclaration()?.let { visit(it) } ?:
            ctx.singletonDeclaration()?.let { visit(it) } ?:
            ctx.interfaceDeclaration()?.let { visit(it) } ?:
            ctx.enumClassDeclaration()?.let { visit(it) } ?:
            ctx.primitiveEnumDeclaration()?.let { visit(it) } ?:
            ctx.recordDeclaration()?.let { visit(it) } ?:
            ctx.inlineClassDeclaration()?.let { visit(it) } ?:
            visit(ctx.annotationDeclaration())
        )

    private fun visit(ctx: QuovaParser.ClassDeclarationContext): ClassDeclaration = run {
        val allMembers = ctx.classBody()?.classMember()?.mapNotNull { visit(it) } ?: listOf()
        val members = mutableListOf<ClassMember>()
        val staticMembers = mutableListOf<SingletonMember>()
        allMembers.forEach {
            if (it.isStatic())
                staticMembers.add(it as SingletonMember)
            else
                members.add(it)
        }
        ClassDeclaration(
            ctx.inheritanceModifier()?.let { visit(it) },
            ctx.SEALED().isNotEmpty(),
            ctx.INNER().isNotEmpty(),
            ctx.simpleIdentifier().text,
            ctx.variantTypeParameters()?.variantTypeParameter()?.map { visit(it) } ?: listOf(),
            ctx.valueParameters()?.valueParameter()?.map { visit(it) } ?: listOf(),
            ctx.supertypes()?.supertype()?.map { visit(it) } ?: listOf(),
            members,
            staticMembers
        )
    }

    private fun visit(ctx: QuovaParser.SingletonDeclarationContext): SingletonDeclaration =
        SingletonDeclaration(
            ctx.simpleIdentifier().text,
            ctx.supertypes()?.supertype()?.map { visit(it) } ?: listOf(),
            ctx.singletonBody().singletonMember().mapNotNull { visit(it) }
        )

    private fun visit(ctx: QuovaParser.InterfaceDeclarationContext): InterfaceDeclaration = run {
        val allMembers = ctx.interfaceBody()?.declaration()?.mapNotNull { visit(it, standardVisibility = VisibilityModifier.PUBLIC) } ?: listOf()
        val members = mutableListOf<Declaration>()
        val staticMembers = mutableListOf<Declaration>()
        allMembers.forEach {
            if (it.isStatic())
                staticMembers.add(it)
            else
                members.add(it)
        }
        InterfaceDeclaration(
            ctx.simpleIdentifier().text,
            ctx.variantTypeParameters()?.variantTypeParameter()?.map { visit(it) } ?: listOf(),
            ctx.userType().map { visit(it) },
            members,
            staticMembers
        )
    }

    private fun visit(ctx: QuovaParser.EnumClassDeclarationContext): EnumClassDeclaration = run {
        val allMembers = ctx.enumClassBody()?.classMember()?.mapNotNull { visit(it) } ?: listOf()
        val members = mutableListOf<ClassMember>()
        val staticMembers = mutableListOf<SingletonMember>()
        allMembers.forEach {
            if (it.isStatic())
                staticMembers.add(it as SingletonMember)
            else
                members.add(it)
        }
        EnumClassDeclaration(
            ctx.simpleIdentifier().text,
            ctx.variantTypeParameters().variantTypeParameter().map { visit(it) },
            ctx.valueParameters().valueParameter().map { visit(it) },
            ctx.supertypes().supertype().map { visit(it) },
            ctx.enumClassBody().enumClassEntries().enumClassEntry().map { visit(it) },
            members,
            staticMembers
        )
    }

    private fun visit(ctx: QuovaParser.PrimitiveEnumDeclarationContext): PrimitiveEnumDeclaration =
        PrimitiveEnumDeclaration(
            ctx.BITFIELD().bool(),
            PrimitiveEnumDeclaration.Type.valueOf(visit(ctx.primitiveNumberType()).name),
            ctx.simpleIdentifier().text,
            ctx.primitiveEnumBody().primitiveEnumEntry().map { visit(it) }
        )

    private fun visit(ctx: QuovaParser.RecordDeclarationContext): RecordDeclaration = run {
        val allMembers = ctx.classBody()?.classMember()?.mapNotNull { visit(it) } ?: listOf()
        val members = mutableListOf<ClassMember>()
        val staticMembers = mutableListOf<SingletonMember>()
        allMembers.forEach {
            if (it.isStatic())
                staticMembers.add(it as SingletonMember)
            else
                members.add(it)
        }
        RecordDeclaration(
            ctx.simpleIdentifier().text,
            ctx.variantTypeParameters()?.variantTypeParameter()?.map { visit(it) } ?: listOf(),
            ctx.valueParameter().map { visit(it) },
            members,
            staticMembers
        )
    }

    private fun visit(ctx: QuovaParser.InlineClassDeclarationContext): InlineClassDeclaration = run {
        val allMembers = ctx.classBody()?.classMember()?.mapNotNull { visit(it) } ?: listOf()
        val members = mutableListOf<ClassMember>()
        val staticMembers = mutableListOf<SingletonMember>()
        allMembers.forEach {
            if (it.isStatic())
                staticMembers.add(it as SingletonMember)
            else
                members.add(it)
        }
        InlineClassDeclaration(
            ctx.simpleIdentifier().text,
            visit(ctx.valueParameter()),
            members,
            staticMembers
        )
    }

    private fun visit(ctx: QuovaParser.AnnotationDeclarationContext): AnnotationDeclaration = run {
        val allMembers = ctx.annotationMembers()?.classMember()?.mapNotNull { visit(it) } ?: listOf()
        val members = mutableListOf<ClassMember>()
        val staticMembers = mutableListOf<SingletonMember>()
        allMembers.forEach {
            if (it.isStatic())
                staticMembers.add(it as SingletonMember)
            else
                members.add(it)
        }
        AnnotationDeclaration(
            ctx.simpleIdentifier().text,
            ctx.annotationMembers().annotationParameter().map { visit(it) },
            members,
            staticMembers
        )
    }

    private fun visit(ctx: QuovaParser.FunctionDeclarationContext, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL): FunctionDeclaration = run {
        val functionModifiers = visit(ctx.functionModifiers(), local, standardVisibility)
        FunctionDeclaration(
            ctx.annotation().map { visit(it) },
            functionModifiers.visibility,
            functionModifiers.inheritance,
            functionModifiers.modifiers,
            ctx.typeParameters()?.typeParameter()?.map { visit(it) } ?: listOf(),
            ctx.typeOrVoid()?.let { visit(it) },
            ctx.simpleIdentifier().text,
            ctx.valueParameters().valueParameter().map { visit(it) },
            ctx.throwExceptions()?.userType()?.map { visit(it) } ?: listOf(),
            ctx.functionBody()?.let { visit(it) }
        )
    }

    private fun visit(ctx: QuovaParser.PropertyDeclarationContext, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL): PropertyDeclaration = run {
        val propertyModifiers = visit(ctx.propertyModifiers(), local, standardVisibility)
        PropertyDeclaration(
            ctx.annotation().map { visit(it) },
            propertyModifiers.visibility,
            propertyModifiers.inheritance,
            propertyModifiers.modifiers,
            visit(ctx.typeOrVar()),
            ctx.property().map { visit(it) }
        )
    }

    private fun visit(ctx: QuovaParser.PropertyContext): Property =
        Property(
            ctx.simpleIdentifier().text,
            ctx.getter()?.let { visit(it) },
            ctx.setter()?.let { visit(it) },
            ctx.value?.let { visit(it) },
            ctx.delegate?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.ClassMemberContext): ClassMember? =
        ctx.declaration()?.let { visit(it) } ?:
        ctx.constructor()?.let { visit(it) } ?:
        ctx.initBlock()?.let { visit(it) }

    private fun visit(ctx: QuovaParser.SingletonMemberContext): SingletonMember? =
        ctx.declaration()?.let { visit(it) } ?:
        ctx.initBlock()?.let { visit(it) }

    private fun visit(ctx: QuovaParser.ConstructorContext): Constructor =
        Constructor(
            ctx.annotation().map { visit(it) },
            visit(ctx.visibilityModifier()),
            ctx.typeParameters()?.typeParameter()?.map { visit(it) } ?: listOf(),
            ctx.valueParameters().valueParameter().map { visit(it) },
            ctx.constructorDelegations().constructorDelegation().map { visit(it) },
            ctx.functionBody()?.let { visit(it) }
        )/*.also {
            ctx.simpleIdentifier()?.text?.takeIf { it != className }?.let { error("Error") }

        }*/

    private fun visit(ctx: QuovaParser.ConstructorDelegationContext): Constructor.Delegation =
        Constructor.Delegation(
            Either(ctx.THIS()?.let { THIS }, ctx.superLiteral()?.let { visit(it) }),
            ctx.valueArguments().valueArgument().map { visit(it) }
        )

    private fun visit(ctx: QuovaParser.InitBlockContext): InitBlock =
        InitBlock(
            ctx.STATIC().bool(),
            visit(ctx.block())
        )

    private fun visit(ctx: QuovaParser.PrimitiveEnumEntryContext): PrimitiveEnumDeclaration.Entry =
        PrimitiveEnumDeclaration.Entry(
            ctx.simpleIdentifier().text,
            ctx.expression()?.let {
                PrimitiveEnumDeclaration.Entry.Value(
                    ctx.ASSIGN()?.let { PrimitiveEnumDeclaration.Entry.Value.Type.ASSIGN } ?:
                    PrimitiveEnumDeclaration.Entry.Value.Type.SHIFT,
                    visit(it)
                )
            }
        )

    private fun visit(ctx: QuovaParser.EnumClassEntryContext): EnumEntry =
        EnumEntry(
            ctx.simpleIdentifier().text,
            ctx.valueArguments().valueArgument().map { visit(it) },
            ctx.classBody().classMember().mapNotNull { visit(it) }
        )

    private fun visit(ctx: QuovaParser.AnnotationParameterContext): ValueParameter =
        ValueParameter(
            ctx.annotation().map { visit(it) },
            false,
            visit(ctx.type()),
            false,
            ctx.simpleIdentifier().text,
            ctx.expression()?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.FunctionBodyContext): Either<Block, Expression> =
        Either(ctx.block()?.let { visit(it) }, ctx.expression()?.let { visit(it) })

    private fun visit(ctx: QuovaParser.ValueParameterContext): ValueParameter =
        ValueParameter(
            ctx.annotation().map { visit(it) },
            ctx.VAR().bool(),
            visit(ctx.type()),
            ctx.ELLIPSIS().bool(),
            ctx.simpleIdentifier().text,
            ctx.expression()?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.TypeParameterContext): TypeParameter =
        TypeParameter(
            ctx.annotation().map { visit(it) },
            ctx.REIFIED().bool(),
            ctx.simpleIdentifier().text,
            ctx.type().map { visit(it) }
        )

    private fun visit(ctx: QuovaParser.VariantTypeParameterContext): VariantTypeParameter =
        VariantTypeParameter(
            ctx.annotation().map { visit(it) },
            ctx.varianceModifier()?.let { visit(it) },
            ctx.simpleIdentifier().text,
            ctx.type().map { visit(it) }
        )

    private fun visit(ctx: QuovaParser.ValueArgumentContext): ValueArgument =
        ValueArgument(
            ctx.simpleIdentifier()?.text,
            visit(ctx.expression())
        )

    private fun visit(ctx: QuovaParser.TypeArgumentContext): TypeArgument =
        TypeArgument(
            !ctx.QUEST().bool(),
            ctx.varianceModifier()?.let { visit(it) },
            ctx.type()?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.SupertypeContext): Supertype =
        Supertype(
            visit(ctx.userType()),
            eitherCatchingA(ctx.valueArguments()?.valueArgument()?.map { visit(it) }, ctx.expression()?.let { visit(it) }) { listOf() }
        )

    private fun visit(ctx: QuovaParser.GetterContext): Getter =
        Getter(
            ctx.annotation().map { visit(it) },
            ctx.functionBody()?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.SetterContext): Setter =
        Setter(
            ctx.annotation().map { visit(it) },
            ctx.setterParameter()?.annotation()?.map { visit(it) } ?: listOf(),
            ctx.setterParameter()?.simpleIdentifier()?.text,
            ctx.functionBody()?.let { visit(it) }
        )

    /*
    private Setter visit(QuovaParser.SetterContext ctx) =>
        new Setter(
            visit(a) for a : ctx.annotation(),
            ctx.setterParameter()?{ a for a : annotation() } : listOf(),
            ctx.setterParameter()?.simpleIdentifier()?.text,
            ctx.functionBody()?{ visit(this) }
        );
     */

    private fun visit(ctx: QuovaParser.StatementContext): Statement? =
        ctx.labeledStatement()?.let { visit(it) } ?:
        ctx.assignment()?.let { visit(it) } ?:
        ctx.ifStatement()?.let { visit(it) } ?:
        ctx.switchExpression()?.let { visit(it) } ?:
        ctx.loopStatement()?.let { loop ->
            loop.forStatement()?.let { visit(it) } ?:
            visit(loop.whileStatement())
        } ?:
        ctx.jumpStatement()?.let { visit(it) } ?:
        ctx.block()?.let { visit(it) } ?:
        ctx.declaration()?.let { visit(it, true) } ?:
        ctx.expression()?.let { visit(ctx.expression()) }

    private fun visit(ctx: QuovaParser.SimpleStatementContext): SimpleStatement =
        ctx.ifStatement()?.let { visit(it) } ?:
        ctx.loopStatement()?.let { loop ->
            loop.forStatement()?.let { visit(it) } ?:
            loop.whileStatement()?.let { visit(it) }
        } ?:
        ctx.block()?.let { visit(it) } ?:
        visit(ctx.expression())

    private fun visit(ctx: QuovaParser.LabeledStatementContext): LabeledStatement =
        LabeledStatement(
            ctx.simpleIdentifier().text,
            visit(ctx.statement())!!
        )

    private fun visit(ctx: QuovaParser.AssignmentContext): Assignment =
        Assignment(
            visit(ctx.expression(0)),
            visit(ctx.expression(1)),
            visit(ctx.assignmentOperator())
        )

    private fun visit(ctx: QuovaParser.IfStatementContext): IfStatement =
        IfStatement(
            visit(ctx.expression()),
            visit(ctx.statementBody(0)),
            ctx.statementBody(1)?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.ForStatementContext): ForStatement =
        ForStatement(
            Either(ctx.classicForCondition()?.let { visit(it) }, ctx.forEachCondition()?.let { visit(it) }),
            visit(ctx.statementBody())
        )

    private fun visit(ctx: QuovaParser.ClassicForConditionContext): ForStatement.ClassicCondition =
        ForStatement.ClassicCondition(
            ctx.classicForVarDecl().map { visit(it) },
            ctx.expression().map { visit(it) },
            ctx.simpleStatement().map { visit(it) }
        )

    private fun visit(ctx: QuovaParser.ClassicForVarDeclContext): ForStatement.ClassicCondition.VarDeclaration =
        ForStatement.ClassicCondition.VarDeclaration(
            ctx.typeOrVar()?.let { visit(it) },
            ctx.simpleIdentifier().text,
            visit(ctx.expression())
        )

    private fun visit(ctx: QuovaParser.ForEachConditionContext): ForStatement.ForEachCondition =
        ForStatement.ForEachCondition(
            ctx.typeOrVar()?.let { visit(it) },
            ctx.simpleIdentifier().text,
            visit(ctx.expression())
        )

    private fun visit(ctx: QuovaParser.WhileStatementContext): WhileStatement =
        WhileStatement(
            ctx.DO().bool(),
            visit(ctx.expression()),
            visit(ctx.statementBody())
        )

    private fun visit(ctx: QuovaParser.JumpStatementContext): JumpStatement =
        JumpStatement(
            ctx.BREAK()?.let { JumpStatement.Order.BREAK } ?:
            ctx.CONTINUE()?.let { JumpStatement.Order.CONTINUE } ?:
            ctx.THROW()?.let { JumpStatement.Order.THROW } ?:
            JumpStatement.Order.RETURN,
            ctx.simpleIdentifier()?.text,
            ctx.expression()?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.StatementBodyContext): StatementBody =
        ctx.block()?.let { visit(it) } ?:
        ctx.statement()?.let { visit(it) } ?:
        PASS

    private fun visit(ctx: QuovaParser.ExpressionContext): Expression =
        ctx.primaryExpression()?.let { visit(it) } ?:
        ctx.operatorExpression()?.let { visit(it) } ?:
        run {
            val forSuffix = visit(ctx.forSuffix())
            ListComprehension(
                visit(ctx.expression()),
                forSuffix.forEach,
                forSuffix.ifCondition
            )
        }

    private fun visit(ctx: QuovaParser.BlockContext): Block =
        Block(
            ctx.statement().mapNotNull { visit(it) }
        )

    private fun visit(ctx: QuovaParser.OperatorExpressionContext): Expression = when (ctx) {
        is QuovaParser.PostfixContext -> OperatorExpression.Postfix(
            visit(ctx.operatorExpression()),
            ctx.INCR()?.let { OperatorExpression.Postfix.Operator.INCR } ?:
            ctx.DECR()?.let { OperatorExpression.Postfix.Operator.DECR } ?:
            ctx.BANG(0)?.let { OperatorExpression.Postfix.Operator.NOT_NULL } ?:
            ctx.invocationSuffix()?.let { visit(it) } ?:
            ctx.indexingSuffix()?.let { OperatorExpression.Postfix.Indexing(
                it.expression().map { ex -> visit(ex) }
            ) } ?:
            visit(ctx.callSuffix())
        )
        is QuovaParser.PrefixContext -> OperatorExpression.Prefix(
            ctx.PLUS()?.let { OperatorExpression.Prefix.Operator.PLUS } ?:
            ctx.DASH()?.let { OperatorExpression.Prefix.Operator.MINUS } ?:
            ctx.INCR()?.let { OperatorExpression.Prefix.Operator.INCR } ?:
            ctx.DECR()?.let { OperatorExpression.Prefix.Operator.DECR } ?:
            ctx.BANG()?.let { OperatorExpression.Prefix.Operator.NOT } ?:
            ctx.TILDE()?.let { OperatorExpression.Prefix.Operator.COMP } ?:
            OperatorExpression.Prefix.Operator.REF,
            visit(ctx.operatorExpression())
        )
        is QuovaParser.CastContext -> OperatorExpression.Cast(
            visit(ctx.type()),
            ctx.QUEST().bool(),
            visit(ctx.operatorExpression())
        )
        is QuovaParser.ProductContext -> OperatorExpression.Product(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            ctx.STAR()?.let { OperatorExpression.Product.Operator.TIMES } ?:
            ctx.SLASH()?.let { OperatorExpression.Product.Operator.DIV } ?:
            OperatorExpression.Product.Operator.REM
        )
        is QuovaParser.SumContext -> OperatorExpression.Sum(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            ctx.PLUS()?.let { OperatorExpression.Sum.Operator.PLUS } ?:
            OperatorExpression.Sum.Operator.MINUS
        )
        is QuovaParser.ShiftContext -> OperatorExpression.Shift(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            ctx.SHL()?.let { OperatorExpression.Shift.Operator.LEFT } ?:
            ctx.SHR()?.let { OperatorExpression.Shift.Operator.RIGHT } ?:
            OperatorExpression.Shift.Operator.U_RIGHT
        )
        is QuovaParser.RangeContext -> OperatorExpression.Range(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.ElvisContext -> OperatorExpression.Elvis(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.NamedCheckContext -> OperatorExpression.NamedCheck(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            Either(ctx.inOperator()?.let { visit(it) }, ctx.instanceOperator()?.let { visit(it) })
        )
        is QuovaParser.SpaceshipContext -> OperatorExpression.Spaceship(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.ComparisonContext -> OperatorExpression.Comparison(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            ctx.LANGLE()?.let { OperatorExpression.Comparison.Operator.LT } ?:
            ctx.RANGLE()?.let { OperatorExpression.Comparison.Operator.GT } ?:
            ctx.LE()?.let { OperatorExpression.Comparison.Operator.LE } ?:
            OperatorExpression.Comparison.Operator.GE
        )
        is QuovaParser.EqualityContext -> OperatorExpression.Equality(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            ctx.EQEQ()?.let { OperatorExpression.Equality.Operator.EQ } ?:
            ctx.NOT_EQ()?.let { OperatorExpression.Equality.Operator.NOT_EQ } ?:
            ctx.EQEQEQ()?.let { OperatorExpression.Equality.Operator.IDENTITY } ?:
            OperatorExpression.Equality.Operator.NOT_IDENTITY
        )
        is QuovaParser.BitAndContext -> OperatorExpression.BitAnd(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.BitXorContext -> OperatorExpression.BitXor(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.BitOrContext -> OperatorExpression.BitOr(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.ConjunctionContext -> OperatorExpression.Conjunction(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.DisjunctionContext -> OperatorExpression.Disjunction(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1))
        )
        is QuovaParser.TernaryContext -> OperatorExpression.Ternary(
            visit(ctx.operatorExpression()),
            visit(ctx.expression(0)),
            visit(ctx.expression(1))
        )
        is QuovaParser.SpreadContext -> OperatorExpression.Spread(
            visit(ctx.operatorExpression())
        )
        is QuovaParser.AssignContext -> OperatorExpression.Assignment(
            visit(ctx.operatorExpression(0)),
            visit(ctx.operatorExpression(1)),
            visit(ctx.assignmentOperator())
        )
        else -> visit((ctx as QuovaParser.PrimaryContext).primaryExpression())
    }

    private fun visit(ctx: QuovaParser.InvocationSuffixContext): OperatorExpression.Postfix.Invocation =
        OperatorExpression.Postfix.Invocation(
            ctx.typeArguments()?.typeArgument()?.map { visit(it) } ?: listOf(),
            ctx.valueArguments()?.let { va -> va.valueArgument().map { visit(it)} }?.let {
                    va -> ctx.lambdaBody()?.let { va + ValueArgument(null, Lambda(
                        listOf(), Either.B(visit(ctx.lambdaBody())))
                    ) } ?: va } ?:
            ctx.lambda()?.let { listOf(ValueArgument(null, visit(it))) } ?:
            listOf(ValueArgument(null, Lambda(listOf(), Either.B(visit(ctx.lambdaBody())))))
        )

    private fun visit(ctx: QuovaParser.CallSuffixContext): OperatorExpression.Postfix.Call =
        ctx.CLASS()?.let { OperatorExpression.Postfix.Call(
            OperatorExpression.Postfix.Call.Operator.REF,
            Either.B(OperatorExpression.Postfix.Call.CLASS)
        ) } ?:
        OperatorExpression.Postfix.Call(
            ctx.QUEST()?.let { OperatorExpression.Postfix.Call.Operator.SAFE } ?:
            ctx.COLONCOLON()?.let { OperatorExpression.Postfix.Call.Operator.REF } ?:
            OperatorExpression.Postfix.Call.Operator.DOT,
            Either.A(visit(ctx.expression()))
        )

    private fun visit(ctx: QuovaParser.ForSuffixContext) = run {
        data class ForSuffix(
            val forEach: ForStatement.ForEachCondition,
            val ifCondition: Expression?
        )
        ForSuffix(
            visit(ctx.forEachCondition()),
            ctx.expression()?.let { visit(it) }
        )
    }

    private fun visit(ctx: QuovaParser.PrimaryExpressionContext): Expression =
        ctx.expression()?.let { visit(it) } ?:
        ctx.constructorInvocation()?.let { visit(it) } ?:
        ctx.literal()?.let { visit(it) } ?:
        LiteralLiteral(ctx.identifier().text)

    private fun visit(ctx: QuovaParser.ConstructorInvocationContext): ConstructorInvocation {
        if (!ctx.LSQUARE().isNullOrEmpty()) {
            return ctx.primitiveTypeNoArray()?.let {
                ConstructorInvocation(null, Either.B(visit(ctx.initializerList())))
            } ?:
            ConstructorInvocation(null, Either.B(visit(ctx.initializerList(), List(ctx.LSQUARE().size + 1) { i ->
                if (i < ctx.LSQUARE().size) null else visit(ctx.userType())
            })))
        }
        if (!ctx.arraySize().isNullOrEmpty()) {
            return ctx.primitiveTypeNoArray()?.let {
                if (ctx.arraySize().size == 1)
                    ConstructorInvocation(
                        UserType("${it.text.capitalize()}Array", listOf()),
                        Either.A(listOf(ValueArgument(null, visit(ctx.arraySize()[0].expression()))))
                    )
                else
                    ConstructorInvocation(UserType("Array<${visit(ctx.primitiveTypeNoArray())}>", listOf()),
                        Either.A(listOf(ValueArgument(null, visit(ctx.arraySize()[0].expression())))))
            } ?:
            ConstructorInvocation(UserType("Array<${visit(ctx.userType())}>", listOf()),
                Either.A(listOf(ValueArgument(null, visit(ctx.arraySize()[0].expression())))))
        }
        val userType = visit(ctx.userType())
        return ConstructorInvocation(
            userType,
            Either(
                ctx.valueArguments()?.valueArgument()?.map { visit(it) },
                ctx.initializerList()?.let { visit(it, listOf(userType)) }
            )
        )
    }

    private fun visit(ctx: QuovaParser.SwitchExpressionContext): Switch =
        ctx.whenExpression()?.let { whenExpression ->
            WhenExpression(
                List(whenExpression.whenCondition().size) { i ->
                    WhenExpression.Branch(
                        whenExpression.whenCondition(i).expression().map { visit(it) },
                        visit(whenExpression.statementBody(i))
                    )
                },
                whenExpression.statementBody(ctx.statementBody().size - 1).takeIf { ctx.DEFAULT().bool() }?.let { visit(it) }
            )
        } ?:
        SwitchExpression(
            visit(ctx.expression()),
            List(ctx.switchCondition().size) { i ->
                SwitchExpression.Branch(
                    visit(ctx.switchCondition(i)),
                    visit(ctx.statementBody(i))
                )
            },
            ctx.statementBody(ctx.statementBody().size - 1).takeIf { ctx.DEFAULT().bool() }?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.SwitchConditionContext): List<SwitchExpression.Branch.Condition> {
        val list = mutableListOf<SwitchExpression.Branch.Condition>()

        val type: SwitchExpression.Branch.Condition.Type =
            ctx.CASE()?.let { SwitchExpression.Branch.Condition.CASE } ?:
            ctx.inOperator()?.let { visit(it) } ?:
            visit(ctx.instanceOperator())

        list.add(SwitchExpression.Branch.Condition(
            type,
            visit(ctx.expression(0))
        ))

        ctx.switchCondition()?.let {
            list.addAll(visit(it))
            return list
        }

        repeat(ctx.expression().size - 1) { i ->
            list.add(SwitchExpression.Branch.Condition(
                type,
                visit(ctx.expression(i + 1))
            ))
        }

        return list
    }

    private fun visit(ctx: QuovaParser.LiteralContext): Literal =
        ctx.stringLiteral()?.let { StringLiteral(it.text) } ?:
        ctx.multilineStringLiteral()?.let { MultilineStringLiteral(it.text) } ?:
        ctx.THIS()?.let { THIS } ?:
        ctx.superLiteral()?.let { visit(it) } ?:
        ctx.INTEGER_LITERAL()?.let { LiteralLiteral(it.text) } ?:
        ctx.LONG_LITERAL()?.let { LiteralLiteral(it.text) } ?:
        ctx.REAL_LITERAL()?.let { LiteralLiteral(it.text) } ?:
        ctx.CHAR_LITERAL()?.let { LiteralLiteral(it.text) } ?:
        ctx.TRUE()?.let { LiteralLiteral(it.text) } ?:
        ctx.FALSE()?.let { LiteralLiteral(it.text) } ?:
        ctx.NULL()?.let { NULL } ?:
        ctx.VOID()?.let { VOID } ?:
        ctx.lambda()?.let { visit(it) } ?:
        visit(ctx.initializerList())

    private fun visit(ctx: QuovaParser.SuperLiteralContext): SuperLiteral =
        SuperLiteral(ctx.userType()?.let { visit(it) })

    private fun visit(ctx: QuovaParser.LambdaContext): Lambda =
        Lambda(
            ctx.lambdaParameter().map { visit(it) },
            Either(ctx.expression()?.let { visit(it) }, ctx.lambdaBody()?.let { visit(it) })
        )

    private fun visit(ctx: QuovaParser.LambdaParameterContext): Lambda.Parameter =
        Lambda.Parameter(
            ctx.annotation().map { visit(it) },
            ctx.typeOrVar()?.let { visit(it) },
            ctx.simpleIdentifier()?.text
        )

    private fun visit(ctx: QuovaParser.LambdaBodyContext): Lambda.Body =
        Lambda.Body(
            ctx.statement().mapNotNull { visit(it) },
            ctx.expression()?.let { visit(it) }
        )

    private fun visit(ctx: QuovaParser.InitializerListContext, types: List<UserType?> = listOf()): InitializerList =
        InitializerList(
            ctx.dictionaryInitializer()?.let { di ->
                Either(
                    null, List(di.expression().size / 2) { i ->
                        visit(di.expression(i * 2)) to visit(di.expression(i * 2 + 1))
                })
            } ?:
            Either(
                ctx.valueArgument().map { visit(it) }, null
            ),
            types
        )

    private fun visit(ctx: QuovaParser.AssignmentOperatorContext): AssignmentOperator =
        ctx.ASSIGN()?.let { AssignmentOperator.ASSIGN } ?:
        ctx.PLUS_ASSIGN()?.let { AssignmentOperator.PLUS } ?:
        ctx.MINUS_ASSIGN()?.let { AssignmentOperator.MINUS } ?:
        ctx.TIMES_ASSIGN()?.let { AssignmentOperator.TIMES } ?:
        ctx.DIV_ASSIGN()?.let { AssignmentOperator.DIV } ?:
        ctx.MOD_ASSIGN()?.let { AssignmentOperator.MOD } ?:
        ctx.AND_ASSIGN()?.let { AssignmentOperator.AND } ?:
        ctx.XOR_ASSIGN()?.let { AssignmentOperator.XOR } ?:
        ctx.OR_ASSIGN()?.let { AssignmentOperator.OR } ?:
        ctx.SHL_ASSIGN()?.let { AssignmentOperator.SHL } ?:
        ctx.SHR_ASSIGN()?.let { AssignmentOperator.SHR } ?:
        ctx.USHR_ASSIGN()?.let { AssignmentOperator.USHR } ?:
        AssignmentOperator.COALESCING

    private fun visit(ctx: QuovaParser.InOperatorContext): InOperator =
        ctx.IN()?.let { InOperator.IN } ?:
        InOperator.NOT_IN

    private fun visit(ctx: QuovaParser.InstanceOperatorContext): InstanceOperator =
        ctx.INSTANCEOF()?.let { InstanceOperator.INSTANCEOF } ?:
        InstanceOperator.NOT_INSTANCEOF

    private fun visit(ctx: QuovaParser.TypeOrVoidContext): Either<Type, VOID> =
        ctx.type()?.let { Either(visit(it), null) } ?:
        Either(null, VOID)

    private fun visit(ctx: QuovaParser.TypeOrVarContext): Either<Type, VAR> =
        ctx.type()?.let { Either(visit(it), null) } ?:
        Either(null, VAR)

    private fun visit(ctx: QuovaParser.TypeContext): Type =
        Type(
            ctx.typeReference()?.let { visit(it) } ?:
            visit(ctx.functionType()),
            ctx.LSQUARE().size,
            ctx.QUEST().bool()
        )

    private fun visit(ctx: QuovaParser.TypeReferenceContext): TypeReference =
        ctx.primitiveType()?.let { visit(it) } ?:
        visit(ctx.userType())

    private fun visit(ctx: QuovaParser.PrimitiveTypeNoArrayContext): PrimitiveType.Type =
        ctx.primitiveNumberType()?.let { visit(it) } ?:
        ctx.FLOAT()?.let { PrimitiveType.Type.FLOAT } ?:
        ctx.DOUBLE()?.let { PrimitiveType.Type.DOUBLE } ?:
        ctx.CHAR()?.let { PrimitiveType.Type.CHAR } ?:
        PrimitiveType.Type.BOOLEAN

    private fun visit(ctx: QuovaParser.PrimitiveTypeContext): PrimitiveType =
        PrimitiveType(
            visit(ctx.primitiveTypeNoArray()),
            ctx.LSQUARE().bool()
        )

    private fun visit(ctx: QuovaParser.PrimitiveNumberTypeContext): PrimitiveType.Type =
        ctx.BYTE()?.let { PrimitiveType.Type.BYTE } ?:
        ctx.SBYTE()?.let { PrimitiveType.Type.SBYTE } ?:
        ctx.SHORT()?.let { PrimitiveType.Type.SHORT } ?:
        ctx.USHORT()?.let { PrimitiveType.Type.USHORT } ?:
        ctx.INT()?.let { PrimitiveType.Type.INT } ?:
        ctx.UINT()?.let { PrimitiveType.Type.UINT } ?:
        ctx.LONG()?.let { PrimitiveType.Type.LONG } ?:
        PrimitiveType.Type.ULONG

    private fun visit(ctx: QuovaParser.UserTypeContext): UserType =
        UserType(
            ctx.identifier().text,
            ctx.typeArguments()?.typeArgument()?.map { visit(it) } ?: listOf()
        )

    private fun visit(ctx: QuovaParser.FunctionTypeContext): FunctionType =
        FunctionType(
            ctx.SUSPEND().bool(),
            visit(ctx.type(0)),
            List(ctx.type().size - 1) { i ->
                visit(ctx.type(i + 1))
            }
        )

    private fun visit(ctx: QuovaParser.AnnotationContext): Annotation =
        Annotation(
            ctx.identifier().text,
            ctx.typeArguments()?.typeArgument()?.map { visit(it) } ?: listOf(),
            ctx.annotationArgument()?.map { it.valueArgument()?.let { va ->
                Annotation.Argument(va.simpleIdentifier()?.text, Either.A(visit(va.expression())))
            } ?:
                Annotation.Argument(it.simpleIdentifier()?.text, Either.B(it.expression().map { visit(it) }))
            } ?: listOf()
        )

    private fun visit(ctx: QuovaParser.FunctionModifiersContext, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL) = run {
        data class FunctionModifiers(
            val visibility: VisibilityModifier,
            val inheritance: InheritanceModifier?,
            val modifiers: List<FunctionModifier>
        )

        val modifiers = mutableListOf<FunctionModifier>()
        ctx.INLINE().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.INLINE) }
        ctx.TAILREC().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.TAILREC) }
        ctx.STATIC().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.STATIC) }
        ctx.DEFAULT().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.DEFAULT) }
        ctx.SUSPEND().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.SUSPEND) }
        ctx.STRICTFP().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.STRICTFP) }
        ctx.SYNCHRONIZED().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.SYNCHRONIZED) }
        ctx.NATIVE().takeIf { it.size == 1 }?.let { modifiers.add(FunctionModifier.NATIVE) }

        FunctionModifiers(
            visit(ctx.visibilityModifier(), local, standardVisibility),
            ctx.inheritanceModifier()?.let { visit(it) },
            modifiers
        )
    }

    private fun visit(ctx: QuovaParser.VisibilityModifierContext?, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL): VisibilityModifier =
        ctx?.PUBLIC()?.let { VisibilityModifier.PUBLIC } ?:
        ctx?.PRIVATE()?.let { VisibilityModifier.PRIVATE } ?:
        ctx?.PROTECTED()?.let { VisibilityModifier.PROTECTED } ?:
        if (local && ctx == null) VisibilityModifier.LOCAL else standardVisibility

    private fun visit(ctx: QuovaParser.InheritanceModifierContext): InheritanceModifier =
        ctx.FINAL()?.let { InheritanceModifier.FINAL } ?:
        InheritanceModifier.ABSTRACT

    private fun visit(ctx: QuovaParser.VarianceModifierContext): VarianceModifier =
        ctx.IN()?.let { VarianceModifier.IN } ?:
        VarianceModifier.OUT


    private fun visit(ctx: QuovaParser.PropertyModifiersContext, local: Boolean = false, standardVisibility: VisibilityModifier = VisibilityModifier.INTERNAL) = run {
        data class PropertyModifiers(
            val visibility: VisibilityModifier,
            val inheritance: InheritanceModifier?,
            val modifiers: List<PropertyModifier>
        )

        val modifiers = mutableListOf<PropertyModifier>()
        ctx.READONLY().takeIf { it.size == 1 }?.let { modifiers.add(PropertyModifier.READONLY) }
        ctx.STATIC().takeIf { it.size == 1 }?.let { modifiers.add(PropertyModifier.STATIC) }
        ctx.CONST().takeIf { it.size == 1 }?.let { modifiers.add(PropertyModifier.CONST) }
        ctx.VOLATILE().takeIf { it.size == 1 }?.let { modifiers.add(PropertyModifier.VOLATILE) }
        ctx.TRANSIENT().takeIf { it.size == 1 }?.let { modifiers.add(PropertyModifier.TRANSIENT) }

        PropertyModifiers(
            visit(ctx.visibilityModifier(), local, standardVisibility),
            ctx.inheritanceModifier()?.let { visit(it) },
            modifiers
        )
    }

        /*SwitchExpression.Branch.Condition(
            ctx.CASE()?.let { SwitchExpression.Branch.Condition.CASE } ?:
            ctx.inOperator()?.let { visit(it) } ?:
            visit(ctx.instanceOperator()),
            visit(ctx.expression(0))
        )*/
}