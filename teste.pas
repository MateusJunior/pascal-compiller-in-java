program FibonacciPrint;

var
  limite, a, b, temp: integer;

begin
  write('Entre com o limite para os números de Fibonacci: ');
  read(limite);
  writeln(' ');

  a := 0;
  b := 1;

  writeln('Números de Fibonacci até ', limite, ': ');

  while (a <= limite) do
  begin
    writeln(a);
    temp := a + b;
    a := b;
    b := temp;
  end;
end.