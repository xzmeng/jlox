package visitor;

public class Dog extends Animal{
    final String name;

    Dog(String name) {
        this.name = name;
    }

    void bark() {
        System.out.println("won won won");
    }

    @Override
    void accept(AnimalVisitor av) {
        av.visitDog(this);
    }
}
