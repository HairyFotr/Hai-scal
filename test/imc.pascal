program HelloWorld;
var
    bg:boolean;
    i:integer;
    c:char;
    arr:array[4..65] of array[5..44] of boolean;
    a:array[4..6] of boolean;
    b:array[0..2] of boolean;
    
function fa():boolean;
begin
    arr[5][5] := false;
    arr[5][5] := false;
    fa := true
end;

procedure putbul(b:boolean);
begin
    if b then putch('t') else putch('f')
end;
procedure putnls();
begin
    putch(chr(10))
end;

procedure pa();
begin
    bg := true
    //fa()
end;
function id(i:integer):integer;
begin
    id := i
end;

begin
    bg := true;
    putbul(bg);
    bg := not true;
    putbul(bg);
    putnls();
    pa();
    
    bg := false;

    a[4] := true;
    a[6] := false;
    
    for i := 4 to 6 do putbul(a[i]);
    putnls();

    b[0] := true;
    b[2] := false;
    for i := 0 to 2 do putbul(b[i]);
    putnls();
    
    for i := 3 to 5 do
    begin
        if i = 4 then bg := not false;
        while bg do
        begin
            i := 10;
            i := 35+i;
            bg := false
        end
    end;
    for i := 0 to 3 do
    begin
    putch('H');
    putch('e');
    putch('l');
    putch('l');
    putch('o');
    putch(' ');
    putch('W');
    putch('o');
    putch('r');
    putch('l');
    putch('d');
    putch('!');
    putch(' ')
    end;

    putint(id(6+1)+1)
end.
