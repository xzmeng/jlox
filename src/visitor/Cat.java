package visitor;

public class Cat extends Animal{
    final String name;

    Cat(String name) {
        this.name = name;
    }

    void bark() {
        System.out.println("meow meow meow");
    }

    @Override
    void accept(AnimalVisitor av) {
        av.visitCat(this);
    }
}
