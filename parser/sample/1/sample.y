%start ROOT

%token EQ NE LT LE GT GE
%token PLUS MINUS MULT DIVIDE
%token RPAREN LPAREN
%token ASSIGN
%token SEMICOLON
%token IF THEN ELSE FI
%token WHILE DO OD
%token PRINT
%token INTEGER FLOAT
%token NAME

%empty epsilon

%%

ROOT
    : stmtseq
    ;

statement
    : designator ASSIGN expression
    | PRINT expression
    | IF expression THEN stmtseq ELSE stmtseq FI
    | IF expression THEN stmtseq FI
    | WHILE expression DO stmtseq OD
    ;

stmtseq
    : stmtseq SEMICOLON statement
    | statement
    ;

expression
    : expr2
    | expr2 EQ expr2
    | expr2 NE expr2
    | expr2 LT expr2
    | expr2 LE expr2
    | expr2 GT expr2
    | expr2 GE expr2
    ;

expr2
    : expr3
    | expr2 PLUS expr3
    | expr2 MINUS expr3
    ;

expr3
    : expr4
    | expr3 MULT expr4
    | expr3 DIVIDE expr4
    ;

expr4
    : PLUS expr4
    | MINUS expr4
    | LPAREN expression RPAREN
    | INTEGER
    | FLOAT
    | designator
    ;

designator
    : NAME
    ;
