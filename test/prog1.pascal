program HelloWorld;

const 
    aaa = ^bbb;
    aaa = bbb^;
    bbb = not false;
    ccc = 1 + 1 + 1;
    ddd = lol.neki div lol.nekineki;

type
    ccc = integer;
    ddd = char;
    
var
    eee:integer;
    fff:boolean;
    kkk:char;
    aa:array[1..4] of integer;
    
procedure Functions();
begin 
  a := 5 + 3 * 23 div -45 + (12 * 8 * (1 + 8));
  b := 3 * 5 + 3;
  c := 3 + 3 * 5;
  d := ^a + ^c + - ^b^ * 30;
end;

procedure a(); 
    var
        i:integer;
        j:boolean;
        
    function argh(lol:char):boolean;
    begin
        write('g');
    end;
begin 
    write('a');
    aa[4] := (4 div 3) * 5 + (4+2);
end;
function b():boolean; begin end;
function c(i:integer; b:boolean):integer;
begin 
    write('b');
    1+1;
    result := 5;
end;

begin
    2+2;
    4*3;
    while(i > 10+1) do
    begin 
        write('a');
        for i := 0 to 10 do 1+1;
    end;
    if true then 54 else 5;
    neki(3,4);
    1+1;
    ccc^;
    ^ccc;
    {^ccc^;}
    a+b[3];
    ccc := 5 + -3;
    aa := 4;
end.
