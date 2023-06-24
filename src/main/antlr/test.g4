grammar test;

init: expression;

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
    | 'lambda' NAME '=>' expression #Lambda
    | 'function' NAME '=' '{' func=expression '}' '(' arg=atom ')' #App
    | 'if' condition=expression 'then' then=expression 'else' else=expression #If
    | 'let' NAME '=' bound=expression 'in' body=expression #Let
    ;

TEXT: '"' ~('"')* '"';
INT: [0-9]+;
BOOL: 'false' | 'true';
NAME: [a-zA-Z_]+;
WS: [ \t\r\n] -> skip;

