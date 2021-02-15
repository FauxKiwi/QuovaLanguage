parser grammar QuovaParser;

options { tokenVocab = QuovaLexer; }

quovaFile
    : ShebangLine? (PACKAGE identifier SEMI)? importHeader* declaration* EOF
    ;

importHeader
    : IMPORT identifier (DOT STAR | AS simpleIdentifier)? SEMI
    ;

//////////////////
// DECLARATIONS //
//////////////////

declaration
    : typeDeclaration
    | functionDeclaration
    | propertyDeclaration
    | SEMI
    ;

typeDeclaration
    : annotation* visibilityModifier?
    ( classDeclaration
    | singletonDeclaration
    | interfaceDeclaration
    | enumClassDeclaration
    | primitiveEnumDeclaration
    | recordDeclaration
    | inlineClassDeclaration
    | annotationDeclaration )
    ;

classDeclaration
    : inheritanceModifier? SEALED? CLASS simpleIdentifier variantTypeParameters? valueParameters? supertypes? (classBody | SEMI)
    ;

singletonDeclaration
    : STATIC CLASS simpleIdentifier supertypes? (singletonBody | SEMI)
    ;

interfaceDeclaration
    : ABSTRACT? INTERFACE simpleIdentifier variantTypeParameters? (COLON type (COMMA type)*)? (interfaceBody | SEMI)?
    ;

enumClassDeclaration
    : ENUM CLASS? simpleIdentifier variantTypeParameters? valueParameters? supertypes? (enumClassBody | SEMI)
    ;

primitiveEnumDeclaration
    : ENUM BITFIELD? primitiveNumberType simpleIdentifier (primitiveEnumBody | SEMI)
    ;

recordDeclaration
    : RECORD simpleIdentifier variantTypeParameters? LPAREN valueParameter (COMMA valueParameter)* RPAREN (classBody | SEMI)
    ;

inlineClassDeclaration
    : INLINE CLASS simpleIdentifier LPAREN valueParameter RPAREN (classBody | SEMI)
    ;

annotationDeclaration
    : AT_INTERFACE simpleIdentifier annotationMembers
    ;

functionDeclaration
    : annotation* functionModifiers typeParameters? (typeOrVoid | DEF) simpleIdentifier valueParameters throwExceptions? (functionBody | SEMI)
    ;

propertyDeclaration
    : annotation* visibilityModifier? inheritanceModifier? (READONLY | CONST)? typeOrVar property (COMMA property)* SEMI
    ;

property
    :  simpleIdentifier ((LCURL (getter setter? | setter getter) RCURL)? (ASSIGN expression)? | LANGLE DASH expression)
    ;

///////////////////////
// DECLARATION PARTS //
///////////////////////

classBody
    : LCURL classMember* RCURL
    ;

singletonBody
    : LCURL singletonMember* RCURL
    ;

interfaceBody
    : LCURL declaration* RCURL
    ;

primitiveEnumBody
    : LCURL (primitiveEnumEntry (COMMA primitiveEnumEntry)*)? SEMI? RCURL
    ;

enumClassBody
    : LCURL (enumClassEntries* | enumClassEntries+ SEMI classMember* | classMember+) RCURL
    ;

annotationMembers
    : LCURL (annotationParameter | classMember)* RCURL
    ;

classMember
    : declaration
    | constructor
    | initBlock
    ;

singletonMember
    : initBlock
    | declaration
    ;

constructor
    : annotation* visibilityModifier* typeParameters? (userType | CONSTRUCTOR) valueParameters constructorDelegations? functionBody?
    ;

constructorDelegations
    : COLON
    ;

initBlock
    : STATIC? block
    ;

primitiveEnumEntry
    : simpleIdentifier ((ASSIGN | SHL) expression)?
    ;

enumClassEntries
    : enumClassEntry (COMMA enumClassEntry)*
    ;

enumClassEntry
    : simpleIdentifier valueArguments? classBody?
    ;

annotationParameter
    : type simpleIdentifier LPAREN RPAREN (DEFAULT expression)?
    ;

functionBody
    : DOUBLE_ARROW expression SEMI
    | block
    ;

valueParameters
    : LPAREN (valueParameter (COMMA valueParameter)*)? RPAREN
    ;

valueParameter
    : annotation* VAR? type ELLIPSIS? identifier
    ;

typeParameters
    : LANGLE typeParameter (COMMA typeParameter)* RANGLE
    ;

typeParameter
    : annotation* REIFIED? simpleIdentifier (COLON type (AMP type)*)?
    ;

variantTypeParameters
    : LANGLE variantTypeParameter (COMMA variantTypeParameter)* RANGLE
    ;

variantTypeParameter
    : annotation* varianceModifier? typeParameter
    ;

valueArguments
    : LPAREN (valueArgument (COMMA valueArgument)*)? RPAREN
    ;

valueArgument
    : (simpleIdentifier ASSIGN)? expression
    ;

typeArguments
    : LANGLE typeArgument (COMMA typeArgument)* RANGLE
    ;

typeArgument
    : varianceModifier? userType
    | QUEST
    ;

supertypes
    : COLON (supertype (COMMA supertype)*)
    ;

supertype
    : type (valueArguments | LANGLE DASH expression)?
    ;

throwExceptions
    : THROWS userType (COMMA userType)*
    ;

getter
    : annotation* GET (SEMI | LPAREN RPAREN functionBody)
    ;

setter
    : SET (SEMI | LPAREN setterParameter RPAREN functionBody)
    ;

setterParameter
    : annotation* type? simpleIdentifier
    ;

////////////////
// STATEMENTS //
////////////////

statement
    : labeledStatement
    | assignment
    | ifStatement
    | switchExpression
    | loopStatement
    | jumpStatement SEMI
    | block
    | declaration
    | expression SEMI
    ;

simpleStatement
    : ifStatement
    | loopStatement
    | block
    | expression
    ;

labeledStatement
    : simpleIdentifier COLON statement
    ;

assignment
    : expression assignmentOperator expression
    ;

ifStatement
    : IF LPAREN expression RPAREN statementBody (ELSE statementBody)?
    ;

loopStatement
    : forStatement
    | whileStatement
    ;

forStatement
    : FOR LPAREN (classicForCondition | forEachCondition) RPAREN statementBody
    ;

classicForCondition
    : (classicForVarDecl (COMMA classicForVarDecl)*)? SEMI
      (expression (COMMA expression)*)? SEMI
      (simpleStatement (COMMA simpleStatement)*)?
    ;

classicForVarDecl
    : typeOrVar? simpleIdentifier ASSIGN expression
    ;

forEachCondition
    : typeOrVar? simpleIdentifier COLON expression
    ;

whileStatement
    : DO statementBody WHILE LPAREN expression RPAREN
    | WHILE LPAREN expression RPAREN statementBody
    ;

statementBody
    : (block | statement | SEMI)
    ;

jumpStatement
    : BREAK simpleIdentifier
    | CONTINUE
    | RETURN expression (AT simpleIdentifier)?
    ;

/////////////////
// EXPRESSIONS //
/////////////////

expression
    : primaryExpression
    | operatorExpression
    | expression forSuffix
    ;

block
    : LCURL statement* RCURL
    ;

operatorExpression
    : operatorExpression (INCR | DECR | BANG BANG | (typeArguments? valueArguments) | indexingSuffix | callSuffix) #postfix
    | (PLUS | DASH | INCR | DECR | BANG | TILDE | COLONCOLON) operatorExpression #prefix
    | LPAREN type RPAREN QUEST? operatorExpression #cast
    | operatorExpression (STAR | SLASH | PERCENT) operatorExpression #sum
    | operatorExpression (PLUS | DASH) operatorExpression # product
    | operatorExpression (SHL | SHR | USHR) operatorExpression #shift
    | operatorExpression RANGE operatorExpression #range
    | operatorExpression ELVIS operatorExpression #elvis
    | operatorExpression (inOperator | instanceOperator) operatorExpression #namedCheck
    | operatorExpression (LANGLE | RANGLE | LE | GE) operatorExpression #comparison
    | operatorExpression (EQEQ | NOT_EQ | EQEQEQ | NOT_EQEQ) operatorExpression #equality
    | operatorExpression AMP operatorExpression #bitAnd
    | operatorExpression PIPE operatorExpression #bitOr
    | operatorExpression CONJ operatorExpression #conjunction
    | operatorExpression DISJ operatorExpression #disjunction
    | <assoc=right> operatorExpression QUEST operatorExpression COLON operatorExpression #ternary
    | ELLIPSIS operatorExpression #spread
    | operatorExpression assignmentOperator operatorExpression #assign
    | primaryExpression #primary
    ;

indexingSuffix
    : LSQUARE expression (COMMA expression)* RSQUARE
    ;

callSuffix
    : DOT CLASS
    | (QUEST? DOT | COLONCOLON) expression
    ;

forSuffix
    : FOR (LPAREN classicForCondition RPAREN | forEachCondition | LPAREN forEachCondition RPAREN)
    ;

primaryExpression
    : LPAREN expression RPAREN
    | constructorInvocation
    | literal
    | identifier
    ;

constructorInvocation
    : NEW type typeArguments? (valueArguments | initializerList)
    ;

switchExpression
    : SWITCH LPAREN expression RPAREN LCURL (switchCondition ARROW statementBody)* (DEFAULT ARROW statementBody)? RCURL
    | whenExpression
    ;

switchCondition
    : (CASE | inOperator | instanceOperator) expression (COMMA expression)*
    ;

whenExpression
    : IF LCURL (whenCondition ARROW statementBody)* (ELSE ARROW statementBody)? RCURL
    ;

whenCondition
    : expression (COMMA expression)*
    ;

//////////////
// LITERALS //
//////////////

literal
    : stringLiteral
    | multilineStringLiteral
    | THIS
    | superLiteral
    | INTEGER_LITERAL
    | LONG_LITERAL
    | REAL_LITERAL
    | CHAR_LITERAL
    | TRUE
    | FALSE
    | NULL
    | VOID
    | lambda
    | initializerList
    ;

stringLiteral
    : STRING_LITERAL stringLiteralContent* END
    ;

stringLiteralContent
    : CHARACTERS
    | SINGLE_EXPR
    | stringLiteralExpression
    ;

stringLiteralExpression
    : COMPLEX_EXPR expression RCURL
    ;

multilineStringLiteral
    : MULTILINE_STRING_LITERAL multilineStringLiteralContent* END
    ;

multilineStringLiteralContent
    : CHARACTERS
    | stringLiteralExpression
    ;

superLiteral
    : SUPER (LANGLE type RANGLE)?
    ;

lambda
    : (lambdaParameter | LPAREN lambdaParameter (COMMA lambdaParameter)* RPAREN) ARROW (expression | lambdaBody)
    ;

lambdaParameter
    : (annotation* typeOrVar) simpleIdentifier
    | UNDERSCORE
    ;

lambdaBody
    : LCURL statement* expression? RCURL
    ;

initializerList
    : LCURL (valueArgument (COMMA valueArgument)*)? RCURL
    | dictionaryInitializer
    ;

dictionaryInitializer
    : LCURL (expression COLON expression (COMMA expression COLON expression)*)? RCURL
    ;

///////////////
// OPERATORS //
///////////////

assignmentOperator
    : ASSIGN
    | PLUS_ASSIGN
    | MINUS_ASSIGN
    | TIMES_ASSIGN
    | DIV_ASSIGN
    | MOD_ASSIGN
    | AND_ASSIGN
    | XOR_ASSIGN
    | OR_ASSIGN
    | SHL_ASSIGN
    | SHR_ASSIGN
    | USHR_ASSIGN
    | COALESCING_ASSIGN
    ;

inOperator
    : IN
    | NOT_IN
    ;

instanceOperator
    : INSTANCEOF
    | NOT_INSTANCEOF
    ;

///////////
// TYPES //
///////////

typeOrVoid
    : type
    | VOID
    ;

typeOrVar
    : type
    | VAR
    ;

type
    : (typeReference | functionType) (LSQUARE RSQUARE)* QUEST?
    ;

typeReference
    : primitiveType
    | userType
    ;

primitiveType
    : (primitiveNumberType
    | FLOAT
    | DOUBLE
    | CHAR
    | BOOLEAN)
    (LSQUARE RSQUARE)?
    ;

primitiveNumberType
    : BYTE
    | SBYTE
    | SHORT
    | USHORT
    | INT
    | UINT
    | LONG
    | ULONG
    ;

userType
    : identifier typeArguments?
    ;

functionType
    : SUSPEND FUNCTION LANGLE type LPAREN (type (COMMA type)*)? RPAREN RANGLE
    ;

annotation
    : AT simpleIdentifier valueParameters?
    ;

///////////////
// MODIFIERS //
///////////////

functionModifiers
    : visibilityModifier? inheritanceModifier?
    ( INLINE
    | TAILREC
    | STATIC
    | DEFAULT
    | SUSPEND )*
    ;

visibilityModifier
    : PUBLIC
    | PRIVATE
    | INTERNAL
    | PROTECTED
    ;

inheritanceModifier
    : FINAL
    | ABSTRACT
    ;

varianceModifier
    : IN
    | OUT
    ;


/////////////////
// IDENTIFIERS //
/////////////////

identifier
    : simpleIdentifier (DOT simpleIdentifier)*
    ;

simpleIdentifier
    : IDENTIFIER
    ;