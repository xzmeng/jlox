package visitor;

public abstract class Animal {
    interface AnimalVisitor {
        void visitDog(Dog dog);
        void visitCat(Cat cat);
    }

    abstract void bark();

    abstract void accept(AnimalVisitor av);

    public static void main(String[] args) {
        Animal dog = new Dog("White");
        Animal cat = new Cat("Kitty");

        // polymorphism
        dog.bark();
        cat.bark();

        // Visitor pattern
        AnimalVisitor eatVisitor = new EatVisitor();
        dog.accept(eatVisitor);
        cat.accept(eatVisitor);
    }
}
