grammar learnbdaLang;

init: prog;

prog: expression;


expression:
      left=expression operator='+' right=expression #Binary
    | left=expression operator='-' right=expression #Binary
    | left=expression operator='*' right=expression #Binary
    | left=expression operator='/' right=expression #Binary
    | left=expression operator='==' right=expression #Binary
    | left=expression operator='&&' right=expression #Binary
    | atom #Unary;


atom:
    'lambda' param=NAME body=expression #Lambda
    | '(' inner=expression ')' #Parenthesized
    | function=atom '(' argument=expression ')' # App
    | NAME #Variable
    | INT # INT
    | BOOL # BOOL
    | STRING # STRING;


STRING: '"' ~('"')* '"';
INT: [0-9]+;
BOOL: 'false' | 'true';
NAME: [a-zA-Z_]+;
WS: [ \t\r\n] -> skip;

