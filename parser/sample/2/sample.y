%token c
%token d

%start S

%%

S
    : C C
    ;

C
    : c C
    | d
    ;
