parser grammar QuovaPreprocessorParser;

options { tokenVocab=QuovaPreprocessorLexer; }

file
    : fileContent* EOF
    ;

fileContent
    : DIRECTIVE directiveContent RSQUARE?
    | SOMETHING
    ;

directiveContent
    : (DIRECTIVE_CONTENT | directiveOperation | DIRECTIVE nested=directiveContent? RSQUARE?) following=directiveContent?
    ;

directiveOperation
    : NOT directiveOperation #unary
    | directiveOperation (STAR | SLASH | PERCENT) directiveOperation #product
    | directiveOperation (PLUS | MINUS) directiveOperation #sum
    | directiveOperation (LT | GT | LE | GE) directiveOperation #comparison
    | directiveOperation (EQ | NEQ) directiveOperation #equality
    | directiveOperation (AND | OR) directiveOperation #logical
    | LPAREN directiveContent RPAREN #parenthesized
    | DIRECTIVE_CONTENT #content
    | DIRECTIVE nested=directiveContent? RSQUARE? #directive
    ;