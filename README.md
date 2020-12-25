A Programming Language implemented in Java
=======================
This repo is a reproduction Bob Nystrom's [Crafting Interpreters](http://craftinginterpreters.com/).

Features
--------
- Dynamic type
- Lexical scope
- Class and inheritance
- First-class function and closure
- Tree-walk interpreter

Roadmaps
--------
- [x] Tokenizer
- [x] Parser and interpreter skeleton
- [x] Basic arithmetics
- [x] Expressions and statements
- [x] Control flow
- [x] Functions and closures
- [x] Lexical scope
- [ ] Classes
- [ ] Inheritance

What it looks like
------------------
```javascript
// function and recursive
fun fib(n) {
    if (n < 2) return n;
    return fib(n - 2) + fib(n - 1) ;
}
// 3 times faster than Python!

// for loop
for (var i = 0; i < 10; i = i + 1) {
    print fib(i); // 0 1 1 2 3 5 ...
}

// closure
fun make_adder(n){
    fun adder(x){
        return n + x;
    }
    return adder;
}

var adder = make_adder(10);
print adder(5); // 15
```
    $ cd src
    $ javac loxy/Loxy.java && java loxy.Loxy a.l
----


References
---------
1. http://craftinginterpreters.com/
2. http://web.stanford.edu/class/cs143/