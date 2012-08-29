package compiler.lexanal;

import java.io.*;

import compiler.report.*;
import compiler.synanal.*;

%%

%class      PascalLex
%public

%line
%column

/* Vzpostavimo zdruzljivost z orodjem Java Cup.
 * To bi lahko naredili tudi z ukazom %cup,
 * a v tem primeru ne bi mogli uporabiti razreda compiler.lexanal.PascalSym
 * namesto razreda java_cup.runtime.Symbol za opis osnovnih simbolov. */
%cupsym     compiler.synanal.PascalTok
%implements java_cup.runtime.Scanner
%function   next_token
%type       PascalSym
%eofval{
    if(commentNesting > 0) Report.warning("Reached end of file with unclosed comment.");
    return new PascalSym(PascalTok.EOF);
%eofval}
%eofclose

%{
    int commentNesting = 0;

    private PascalSym sym(int type) { return new PascalSym(type, yyline + 1, yycolumn + 1, yytext()); }
    
    private String GetOutputString() { return "\""+yytext()+"\", row "+(yyline+1)+"  col "+(yycolumn+1); }
    private void reportWarning() { Report.warning("I have a problem with lexeme "+GetOutputString()); }
%}

%eof{
%eof}

%state COMMENT

%%
<COMMENT> {
"{"  { commentNesting++; }
"}"  { commentNesting--; if(commentNesting<=0) yybegin(YYINITIAL); }
.|\n { }
}

<YYINITIAL> {
[ \n\t\r]+ { }

[/][/].*?[\n] { }
"{" { commentNesting++; yybegin(COMMENT); }

''''|'[^']' { return sym(PascalTok.CHAR_CONST); }
"true"|"false" { return sym(PascalTok.BOOL_CONST); }
[0-9]+ { return sym(PascalTok.INT_CONST); }
"nil"  { return sym(PascalTok.NIL); }

"boolean" { return sym(PascalTok.BOOL); }
"char"    { return sym(PascalTok.CHAR); }
"integer" { return sym(PascalTok.INT); }
"record"  { return sym(PascalTok.RECORD); }
"array"   { return sym(PascalTok.ARRAY); }

"var"    { return sym(PascalTok.VAR); }
"const"  { return sym(PascalTok.CONST); }
".."     { return sym(PascalTok.DOTS); }
"of"     { return sym(PascalTok.OF); }
"type"   { return sym(PascalTok.TYPE); }

"begin" { return sym(PascalTok.BEGIN); }
"end"   { return sym(PascalTok.END); }

"if"    { return sym(PascalTok.IF); }
"then"  { return sym(PascalTok.THEN); }
"else"  { return sym(PascalTok.ELSE); }
"for"   { return sym(PascalTok.FOR); }
"to"    { return sym(PascalTok.TO); }
"do"    { return sym(PascalTok.DO); }
"while" { return sym(PascalTok.WHILE); }

"procedure" { return sym(PascalTok.PROCEDURE); }
"function"  { return sym(PascalTok.FUNCTION); }
"program"   { return sym(PascalTok.PROGRAM); }

"and" { return sym(PascalTok.AND); }
"or"  { return sym(PascalTok.OR); }
"not" { return sym(PascalTok.NOT); }
":"   { return sym(PascalTok.COLON); }
","   { return sym(PascalTok.COMMA); }
"."   { return sym(PascalTok.DOT); }
"["   { return sym(PascalTok.LBRACKET); }
"("   { return sym(PascalTok.LPARENTHESIS); }
")"   { return sym(PascalTok.RPARENTHESIS); }
"]"   { return sym(PascalTok.RBRACKET); }
"+"   { return sym(PascalTok.ADD); }
"-"   { return sym(PascalTok.SUB); }
"*"   { return sym(PascalTok.MUL); }
"div" { return sym(PascalTok.DIV); }
":="  { return sym(PascalTok.ASSIGN); }
">="  { return sym(PascalTok.GEQ); }  
"<="  { return sym(PascalTok.LEQ); }
"<>"  { return sym(PascalTok.NEQ); }
">"   { return sym(PascalTok.GTH); }
"<"   { return sym(PascalTok.LTH); }
"="   { return sym(PascalTok.EQU); }
"|"   { return sym(PascalTok.OR); }
"^"   { return sym(PascalTok.PTR); }
";"   { return sym(PascalTok.SEMIC); }

[a-zA-Z][a-zA-Z0-9_]* { return sym(PascalTok.IDENTIFIER); }

. { reportWarning(); }

}
