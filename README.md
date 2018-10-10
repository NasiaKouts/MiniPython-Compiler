# MiniPython-Compiler
This a MiniPython Compiler. In the repo you can also find the grammar of MiniPython used.

We implemented 2 visitors, mainly because of the fact that functions can be called before their actual definition. Thus, the first visitor looks for all the definitions among the rest checkings, and the second visitor validates each fucntion call, using the results of the first visitor.

## Checkings
1. Attempt to use undefined variable
    Also:
    - making sure that within inAForStatement(for id1 in id2: stm) the id2 is actually an array or list
    - making sure that in inAMinusEqualStatement and in inADivEqualsStatement both operants are integers

2. Attempt to call and undefined funtion

3. Wrong parameters used in a function call attempt

4. Attempt to add string and integer together, since our grammar allows only addition between 2 strings or 2 integers.

5. Invalid function call

6. Duplicate definition of function with the same number of arguments

## Contributors
Chasakis Dionysis [@ChasakisD](https://github.com/ChasakisD)  
Katopodis Antonis [@A.Katopodis](https://github.com/A-Katopodis)  
Koutsopoulou Nasia [@NasiaKouts](https://github.com/NasiaKouts)  
Vasilopoulou Melina [@MelinaVasilopoulou](https://github.com/MelinaVasilopoulou)
