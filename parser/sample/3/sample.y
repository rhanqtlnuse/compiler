%start E

%token PLUS
%token MULT
%token LPAREN RPAREN
%token id

%%

E
    : E PLUS T
    | T
    ;

T
    : T MULT F
    | F
    ;

F
    : LPAREN E RPAREN
    | id
    ;
