%{
#include <stdio.h>
%}

digit	[0-9]
letter  [a-zA-Z]
int	    [+-]?{digit}+
float	[+-]?{digit}*\.{digit}+
name	[_a-zA-Z][_a-zA-Z0-9]*
ws	    [ \t\n\f\r\v]

%%

"="      { fprintf(yyout, "<EQ>\n"); }
"!="     { fprintf(yyout, "<NE>\n"); }
"<"      { fprintf(yyout, "<LT>\n"); }
"<="     { fprintf(yyout, "<LE>\n"); }
">"      { fprintf(yyout, "<GT>\n"); }
">="     { fprintf(yyout, "<GE>\n"); }
"+"      { fprintf(yyout, "<PLUS>\n"); }
"-"      { fprintf(yyout, "<MINUS>\n"); }
"*"      { fprintf(yyout, "<MULT>\n"); }
"/"      { fprintf(yyout, "<DIVIDE>\n"); }
")"      { fprintf(yyout, "<RPAREN>\n"); }
"("      { fprintf(yyout, "<LPAREN>\n"); }
":="     { fprintf(yyout, "<ASSIGN>\n"); }
";"      { fprintf(yyout, "<SEMICOLON>\n"); }
"IF"     { fprintf(yyout, "<IF>\n"); }
"THEN"   { fprintf(yyout, "<THEN>\n"); }
"ELSE"   { fprintf(yyout, "<ELSE>\n"); }
"FI"     { fprintf(yyout, "<FI>\n"); }
"WHILE"  { fprintf(yyout, "<WHILE>\n"); }
"DO"     { fprintf(yyout, "<DO>\n"); }
"OD"     { fprintf(yyout, "<OD>\n"); }
"PRINT"  { fprintf(yyout, "<PRINT>\n"); }
{name}   { fprintf(yyout, "<NAME, %s>\n", yytext); }   
{float}  { fprintf(yyout, "<FLOAT, %s>\n", yytext); }
{int}    { fprintf(yyout, "<INTEGER, %s>\n", yytext); }
"//".*\n { printf("Comment ignored\n"); }
{ws}	 { ; }
.        { ; }

%%

#ifndef yywrap

int yywrap() { return 1; }

#endif

int main(void) {

    FILE *fin = fopen("program.in", "r");
    FILE *fout = fopen("tokens.out", "w+");
    yyin = fin;
    yyout = fout;
    yylex();

    return 0;
}