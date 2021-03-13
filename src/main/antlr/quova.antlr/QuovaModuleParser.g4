parser grammar QuovaModuleParser;

options { tokenVocab = QuovaModuleLexer; }

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
    : plugins
    | repositories
    | dependencies
    | otherOption
    | variableDeclaration
    ;

plugins
    : PLUGINS COLON (LSQUARE (plugin (COMMA plugin)*)? RSQUARE | (DASH plugin)+)
    ;

plugin
    : name versionName?
    | raw
    ;

repositories
    : REPOSITORIES COLON (LSQUARE (repository (COMMA repository)*)? RSQUARE | (DASH repository)+)
    ;

repository
    : name
    | raw
    ;

dependencies
    : DEPENDENCIES COLON (LSQUARE (dependency (COMMA dependency)*)? RSQUARE | (DASH dependency)+)
    ;

dependency
    : (IMPL | ONLY_COMPILE | ONLY_RUNTIME) name COLON name COLON versionName
    | raw
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
    | PLUGINS
    | REPOSITORIES
    | DEPENDENCIES
    | IMPL
    | ONLY_COMPILE
    | ONLY_RUNTIME
    | variableCall)
    (DOT name)?
    ;

versionName
    : (NUMBER | variableCall) (DOT NUMBER)* DASH_SNAPSHOT?
    ;

text
    : ((name | versionName) DOT?)+
    ;

raw
    : RAW_BEGIN rawContent RAW_END
    ;

rawContent
    : RAW*
    ;