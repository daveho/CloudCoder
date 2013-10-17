#include <stdio.h>

int main(void) {
    // Declare variables for input (start and end)
    int start=45, end=67, i;
    
    // Read input values
    scanf("%i", &start);
    scanf("%i", &end);
    
    printf("start=%i, end=%i\n", start, end);
    
    // Use a loop to print the output values
    for (i = start ; i <= end ; i += 3){
        printf("%i ", i);
        
    }
    
    return 0;
}