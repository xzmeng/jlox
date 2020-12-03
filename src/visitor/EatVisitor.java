package visitor;

public class EatVisitor implements Animal.AnimalVisitor {
    @Override
    public void visitDog(Dog dog) {
        System.out.printf("dog %s eat%n", dog.name);
    }

    @Override
    public void visitCat(Cat cat) {
        System.out.printf("cat %s eat%n", cat.name);
    }
}
