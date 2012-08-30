
program recordArrays;
const
   N = 8;
type
   Rec = record
            arr1  : array[1..N] of integer;
            arr2 : array[1..N] of boolean
         end;
var
   rec : Rec;
   i   : integer;
   arr1 : array[1..N] of integer;
   arr2 : array[1..N] of boolean;

procedure printA(arr : array[1..N] of integer);
var i : integer;
begin
   putch('A');
   putch(':');
   putch(' ');
   for i := 1 to N do
   begin
      putint(arr[i]);
       putch('|')
   end;
   putch(chr(10))
end; { printA }

procedure printB(arr : array[1..N] of boolean);
var i : integer;
begin
   putch('B');
   putch(':');
   putch(' ');
   for i := 1 to N do
   begin
      if arr[i] then
         putint(1)
      else
         putint(0);
       putch('|')
   end;
   putch(chr(10))
end; { printA }


begin

   for i := 1 to N do
   begin
      arr1[i] := i+5;
      putint(arr1[i])
   end;
   putch(chr(10));
  
   printA(arr1);

   for i := 1 to N do
   begin
      if i <= N div 2 then
          arr2[i] := true
      else
          arr2[i] := false;

      if arr2[i] then
         putint(1)
      else
         putint(0);
       putch('|')
   end;
   putch(chr(10));
  
   printB(arr2);

   for i := 1 to N do
   begin
      rec.arr1[i] := i;
      putint(rec.arr1[i])
   end;
   putch(chr(10));

   printA(rec.arr1);
   printB(rec.arr2);

   for i := 1 to N do
      if i > 4 then
         rec.arr2[i] := true
      else
         rec.arr2[i] := false;

   printA(rec.arr1);
   printB(rec.arr2)
end.
