lexer grammar QuovaLexer;

ShebangLine: '#!' .*? NL;

LineComment: '//' .*? (NL|EOF) -> channel(HIDDEN);
DelimitedComment: '/*' (DelimitedComment | .)*? '*/' -> channel(HIDDEN);

NL: [\n\r] -> channel(HIDDEN);
Space: [ \t\f] -> channel(HIDDEN);

//Keywords
AT_FILE: '@file:';

PACKAGE: 'package';
IMPORT: 'import';

CLASS: 'class';
INTERFACE: 'interface';
ENUM: 'enum';
RECORD: 'record';
AT_INTERFACE: '@interface';
TYPEDEF: 'typedef';

BYTE: 'byte';
SBYTE: 'sbyte';
SHORT: 'short';
USHORT: 'ushort';
INT: 'int';
UINT: 'uint';
LONG: 'long';
ULONG: 'ulong';
FLOAT: 'float';
DOUBLE: 'double';
DECIMAL: 'decimal';
CHAR: 'char';
BOOLEAN: 'boolean';
VOID: 'void';
FUNCTION: 'function';
VAR: 'var';
DEF: 'def';

IF: 'if';
ELSE: 'else';
SWITCH: 'switch';
DO: 'do';
WHILE: 'while';
FOR: 'for';
CASE: 'case';
BREAK: 'break';
CONTINUE: 'continue';
RETURN: 'return';

TRUE: 'true';
FALSE: 'false';
NULL: 'null';
THIS: 'this';
SUPER: 'super';

NEW: 'new';
AS: 'as';
INSTANCEOF: 'instanceof';
NOT_INSTANCEOF: '!instanceof';
IN: 'in';
NOT_IN: '!in';

BITFIELD: 'bitfield';
CONSTRUCTOR: 'constructor';
THROWS: 'throws';
SEALED: 'sealed';
DEFAULT: 'default';
GET: 'get';
SET: 'set';

PUBLIC: 'public';
PRIVATE: 'private';
INTERNAL: 'internal';
PROTECTED: 'protected';

READONLY: 'readonly';

FINAL: 'final';
ABSTRACT: 'abstract';

INLINE: 'inline';
TAILREC: 'tailrec';
SUSPEND: 'suspend';

REIFIED: 'reified';

OUT: 'out';

CONST: 'const';

STATIC: 'static';

// Symbols

DOT: '.';
COMMA: ',';
COLON: ':';
COLONCOLON: '::';
SEMI: ';';
ELLIPSIS: '...';

LPAREN: '(';
RPAREN: ')';
LSQUARE: '[';
RSQUARE: ']';
LCURL: '{' -> pushMode(DEFAULT_MODE);
RCURL: '}' -> popMode;
LANGLE: '<';
RANGLE: '>';

INCR: '++';
DECR: '--';
PLUS: '+';
DASH: '-';
BANG: '!';
QUEST: '?';
TILDE: '~';
STAR: '*';
SLASH: '/';
PERCENT: '%';
RANGE: '..';
SHL: '<<';
SHR: '>>';
USHR: '>>>';
LE: '<=';
GE: '>=';
EQEQ: '==';
EQEQEQ: '===';
NOT_EQ: '!=';
NOT_EQEQ: '!==';
AMP: '&';
CARET: '^';
PIPE: '|';
ASSIGN: '=';
PLUS_ASSIGN: '+=';
MINUS_ASSIGN: '-=';
TIMES_ASSIGN: '*=';
DIV_ASSIGN: '/=';
MOD_ASSIGN: '%=';
SHL_ASSIGN: '<<=';
SHR_ASSIGN: '>>=';
USHR_ASSIGN: '>>>=';
AND_ASSIGN: '&=';
XOR_ASSIGN: '^=';
OR_ASSIGN: '|=';
COALESCING_ASSIGN: '?=';
ELVIS: '?:';
CONJ: '&&';
DISJ: '||';

ARROW: '->';
DOUBLE_ARROW: '=>';
AT: '@';
UNDERSCORE: '_';

// Literals

fragment Digit: [0-9];
fragment DigitNonZero: [1-9];
fragment DigitOr_: Digit | '_';
fragment HexDigit: [0-9a-fA-F];
fragment HexDigitOr_: HexDigit | '_';

LONG_LITERAL: INTEGER_LITERAL [lL];
INTEGER_LITERAL: HEX_LITERAL | BIN_LITERAL | OCT_LITERAL | IntegerLiteral;
fragment IntegerLiteral: DigitNonZero (DigitOr_* Digit)? | '0'+;
HEX_LITERAL: '0' [xX] HexDigitOr_* HexDigit;
BIN_LITERAL: '0' [bB] [01_]* [01];
OCT_LITERAL: '0' [0-7_]* [0-7];

REAL_LITERAL: HEX_REAL_LITERAL | IntegerLiteral ([fFdDmM] | Exponent [fFdDmM]?) | Digit (DigitOr_* Digit)? '.' Digit (DigitOr_* Digit)? Exponent? [fFdDmM]?;
HEX_REAL_LITERAL: '0' [xX] HexDigitOr_* HexDigit ('.' HexDigit (HexDigitOr_* HexDigit)?)? 'p' IntegerLiteral;
fragment Exponent: [eE] [+-]? IntegerLiteral;

CHAR_LITERAL: '\'' (~[\\'] | EscapeSeq | '\\\'') '\'';

MULTILINE_STRING_LITERAL: '"""' -> pushMode(MULTILINE_STRING);
STRING_LITERAL: '"'  -> pushMode(STRING);

fragment EscapeSeq: '\\' ([rntf\\] | 'u' HexDigit HexDigit HexDigit HexDigit | Digit);

// Rest

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;

ERROR_CHAR: .;


mode STRING;

SINGLE_EXPR: '$' IDENTIFIER;

COMPLEX_EXPR: '${' -> pushMode(DEFAULT_MODE);

CHARACTERS: (~[\\"$] | EscapeSeq | '\\"')+;

END: '"' -> popMode;

mode MULTILINE_STRING;

MULTILINE_STRING_EXPR: '${' -> type(COMPLEX_EXPR), pushMode(DEFAULT_MODE);

MULTILINE_CHARACTERS: (~'$' | '$' ~'{')+ -> type(CHARACTERS);

MULTILINE_END: '"""' -> type(END), popMode;