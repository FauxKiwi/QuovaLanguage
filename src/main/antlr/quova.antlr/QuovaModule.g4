grammar QuovaModule;

quovaModule
    : project group version element* EOF
    ;

project
    : PROJECT COLON name
    ;

group
    : GROUP COLON name
    ;

version
    : VERSION COLON versionName
    ;

element
    : repositories
    | dependencies
    | otherOption
    | variableDeclaration
    ;

repositories
    : REPOSITORIES COLON (LSQUARE (name (COMMA name)*)? RSQUARE | (DASH name)+)
    ;

dependencies
    : DEPENDENCIES COLON (LSQUARE (dependency (COMMA dependency)*)? RSQUARE | (DASH dependency)+)
    ;

dependency
    : (IMPL | ONLY_COMPILE | ONLY_RUNTIME) name COLON name COLON versionName
    ;

otherOption
    : name COLON text
    ;

variableDeclaration
    : DOLLAR name EQUALS text
    ;

variableCall
    : DOLLAR name
    ;

name
    : (TEXT
    | PROJECT
    | GROUP
    | VERSION
    | REPOSITORIES
    | DEPENDENCIES
    | IMPL
    | ONLY_COMPILE
    | ONLY_RUNTIME)
    (DOT name)?
    | variableCall
    ;

versionName
    : NUMBER (DOT NUMBER)* DASH_SNAPSHOT?
    | variableCall
    ;

text
    : ((name | versionName) DOT?)+
    ;

COMMENT: '#' .*? (NL | EOF) -> channel(HIDDEN);
NL: ([\r\n] | '\r\n') -> channel(HIDDEN);
SPACE: [ \t\f]+ -> channel(HIDDEN);

PROJECT: 'project';
GROUP: 'group';
VERSION: 'version';
REPOSITORIES: 'repositories';
DEPENDENCIES: 'dependencies';
IMPL: 'impl';
ONLY_COMPILE: 'compile';
ONLY_RUNTIME: 'runtime';

DASH_SNAPSHOT: '-SNAPSHOT';

LSQUARE: '[';
RSQUARE: ']';
DOT: '.';
COLON: ':';
COMMA: ',';
DASH: '-';
DOLLAR: '$';
EQUALS: '=';

NUMBER: [0-9]+;

TEXT: [a-zA-Z_] [a-zA-Z0-9_]*;