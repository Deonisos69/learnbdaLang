grammar test;

init: prog;

prog: def* expression;

def: 'def' name=NAME '(' parameter=NAME ':' parameterType=type ')' ':' resultType=type '=>' body=expression ;

expression
    : atom #Unary
    | left=expression '*' right=expression #Binary
    | left=expression ('+' | '-') right=expression #Binary
    | left=expression '==' right=expression #Binary
    | left=expression '&&' right=expression #Binary
    | left=expression '||' right=expression #Binary
    ;

atom
    : NAME #Var
    | INT #IntLit
    | BOOL #BoolLit
    | TEXT #TextLit
    | 'fun' NAME ':' parameterType=type '=>' expression #Lambda
    | func=atom '(' arg=expression ')' #App
    | 'if' condition=expression 'then' then=expression 'else' else=expression #If
    | 'let' NAME '=' bound=expression 'in' body=expression #Let
    | '(' inner=expression ')' #Parenthesized
    ;

type
    : 'Int' #IntType
    | 'Bool' #BoolType
    | 'Text' #TextType
    | '(' inner=type ')' #ParenthiszedType
    | <assoc=right> arg=type '->' result=type #FunctionType
    ;

TEXT: '"' ~('"')* '"';
INT: [0-9]+;
BOOL: 'false' | 'true';
NAME: [a-zA-Z_]+;
WS: [ \t\r\n] -> skip;

