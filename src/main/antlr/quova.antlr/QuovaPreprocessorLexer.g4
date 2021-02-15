lexer grammar QuovaPreprocessorLexer;

DIRECTIVE
    : '#' -> pushMode(SIMPLE_DIRECTIVE)
    ;

COMPLEX_DIRECTIVE
    : '#[' -> type(DIRECTIVE), pushMode(DELIMITED_DIRECTIVE)
    ;

STRING
    : '"' .*? '"' -> type(SOMETHING)
    ;

SOMETHING
    : ~[#"']+
    ;

mode SIMPLE_DIRECTIVE;

DIRECTIVE_CONTENT: ~[ \t\f\r\n\]#]+ -> popMode;

mode DELIMITED_DIRECTIVE;

NESTED_SIMPLE_DIRECTIVE: '#' -> type(DIRECTIVE), pushMode(SIMPLE_DIRECTIVE);

NESTED_COMPLEX_DIRECTIVE: '#[' ->  type(DIRECTIVE), pushMode(DELIMITED_DIRECTIVE);

RSQUARE: ']' -> popMode;

SPACING: [ \t\f\r\n]+ -> channel(HIDDEN);

OR: '||';
AND: '&&';
EQ: '==';
NEQ: '!=';
LT: '<';
GT: '>';
LE: '<=';
GE: '>=';
PLUS: '+';
MINUS: '-';
STAR: '*';
SLASH: '/';
PERCENT: '%';
NOT: '!';
LPAREN: '(';
RPAREN: ')';

DELIMITED_DIRECTIVE_CONTENT: ~([ \t\f\r\n#()+*/:!%<>=|&-] | ']')+ -> type(DIRECTIVE_CONTENT);