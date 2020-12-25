
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

//// TODO
//// class
//class Person {
//    // constructor
//    init(name) {
//        this.name = name;
//    }
//    // method
//    show() {
//        print this.name;
//    }
//}
//
//// inheritance
//class Programmer < Person {
//    init(name, lang) {
//        super.init(name);
//        this.lang = lang;
//    }
//    show() {
//        print name + " " + id;
//    }
//}
//
//// class is also a callable
//s = Programmer("xzmeng", "C")
//
//s.show(); // xzmeng C




