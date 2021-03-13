lexer grammar QuovaModuleLexer;

COMMENT: '#' .*? (NL | EOF) -> channel(HIDDEN);
NL: ([\r\n] | '\r\n') -> channel(HIDDEN);
SPACE: [ \t\f]+ -> channel(HIDDEN);

PROJECT: 'project';
GROUP: 'group';
VERSION: 'version';
PLUGINS: 'plugins';
REPOSITORIES: 'repositories';
DEPENDENCIES: 'dependencies';
IMPL: 'impl';
ONLY_COMPILE: 'compile';
ONLY_RUNTIME: 'runtime';

DASH_SNAPSHOT: '-SNAPSHOT';

LSQUARE: '[';
RSQUARE: ']';
LANGLE: '<';
RANGLE: '>';
DOT: '.';
COLON: ':';
COMMA: ',';
DASH: '-';
DOLLAR: '$';
EQUALS: '=';

RAW_BEGIN: '%%{' -> pushMode(RAW_MODE);

NUMBER: [0-9]+;

TEXT: [a-zA-Z_] [a-zA-Z0-9_]*;


mode RAW_MODE;

RAW: ~[{}]+;

CURLS: '{' -> type(RAW), pushMode(RAW_CURLS);

RAW_END: '}' -> popMode;

mode RAW_CURLS;

CURLS_RAW: ~[{}]+ -> type(RAW);

CURLS_CURLS: '{' -> type(RAW), pushMode(RAW_CURLS);

CURLS_END: '}' -> type(RAW), popMode;