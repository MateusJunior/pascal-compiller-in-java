#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
int main(){
int limite, a, b, temp;
printf("'Entre com o limite para os números de Fibonacci: '");
scanf("%d ", &limite);
printf("' '");
printf("\n");
a = 0;
b = 1;
printf("'Números de Fibonacci até '%d ': '", limite);
printf("\n");
while(a <= limite){
printf("%d ", a);
printf("\n");
temp = a + b;
a = b;
b = temp;
};
return 0;
}