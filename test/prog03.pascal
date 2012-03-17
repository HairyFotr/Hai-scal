program HelloWorld;

{$EXTENDED}

procedure say_Hello-World();
begin
    write('H');
    write('e');
    write('l');
    write('l');
    write('o');
    write(' ');
    write('W');
    write('o');
    write('r');
    write('l');
    write('d');
    write('!');
    writeln();
end;

var i,j: integer,
    c: character, { š { } š }
    isThisA_-Weird_Name_That-is_valid: boolean;
    
begin
    i := 10;
    isThisA_-Weird_Name_That-is_valid := true;

    for j := 1 to i do
    begin
        say_hello-to_World(i);
    end;
end.
