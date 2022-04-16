/*
Anvesh Koganti
670875073
CS474 - Homework 3
*/
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import DslMethods.*
import DslMethods.Operator.*

class testInterfaceAbsClass extends AnyFlatSpec with Matchers {
  behavior of "DslMethods"
  it should "check if an abstract class has at least one abstract method" in {

    // Valid abstract class definition
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    // Invalid abstract class definition
    try {
      compute(AbstractClassDef("AbsClass2",
        Field("Public", "field3", Value(12)),
        Field("Public", "field4"),
        Method("Public", "Method2", List("localVar1"),
          Assign(Variable("field1"), Variable("localVar1")))))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "There must be at least 1 abstract method in abstract class."
    }
  }

  it should "check if interface has only abstract methods and has no constructor defined" in {

    //Valid Interface definition
    compute(InterfaceDef("interfaceOne",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2")))

    //Invalid Interface definition -- has concrete method
    try {
      compute(InterfaceDef("interfaceOne",
        Field("Public", "field1", Value(12)),
        Field("Public", "field2"),
        AbstractMethod("AbsMeth1"),
        AbstractMethod("AbsMeth2")))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "There must be no concrete method in an interface."
    }

    //Invalid Interface definition -- has constructor
    try {
      compute(InterfaceDef("interfaceOne",
        Field("Public", "field1", Value(12)),
        Field("Public", "field2"),
        Constructor(List(), Assign(Variable("field1"), Insert(Value("2"), Value(3)))),
        AbstractMethod("AbsMeth1"),
        AbstractMethod("AbsMeth2")))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "Constructor is not allowed in an Interface."
    }
  }

  it should "check if all abstract methods are implemented by a class when it extends from abstract class" in {

    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    // Class extends abstract class "AbsClass1" and implements all abstract methods
    compute(ClassDef("fifthClass",
      Field("Public", "field1", Value(19)),
      Field("Public", "field4"),
      Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
      Method("Public", "AbsMeth1", List("localVar2"),
        Assign(Variable("field4"), Variable("localVar2"))),
      Method("Public", "AbsMeth2", List("localVar2"),
        Assign(Variable("field4"), Variable("localVar2"))),
      Extends("AbsClass1")))

    // Class extends abstract class "AbsClass1" but does not implement all abstract methods ("AbsMeth2")
    try {
      compute(ClassDef("fifthClass",
        Field("Public", "field1", Value(19)),
        Field("Public", "field4"),
        Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
        Method("Public", "AbsMeth1", List("localVar2"),
          Assign(Variable("field4"), Variable("localVar2"))),
        Extends("AbsClass1")))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "There must be no un implemented abstract methods in concrete class."
    }
  }

  it should "check if instantiating abstract classes and interfaces is being allowed" in {
    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    //Valid Interface definition
    compute(InterfaceDef("interfaceOne",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2")))

    // Try creating object of Interface
    try {
      compute(NewObject("interfaceOne", Variable("interfaceObject"), List()))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "This Abstract Class/Interface cannot be instantiated."
    }

    // Try creating object of Abstract Class
    try {
      compute(NewObject("AbsClass1", Variable("absClassObject"), List()))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "This Abstract Class/Interface cannot be instantiated."
    }
  }

  it should "check if multiple inheritance is allowed" in {
    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    //Valid Interface definition
    compute(InterfaceDef("interfaceOne",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2")))

    // Class extends abstract class "AbsClass1" and implements "interfaceOne" interface which is not allowed
    try {
      compute(ClassDef("fifthClass",
        Field("Public", "field1", Value(19)),
        Field("Public", "field4"),
        Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
        Method("Public", "AbsMeth1", List("localVar2"),
          Assign(Variable("field4"), Variable("localVar2"))),
        Extends("AbsClass1"),
        Implements("interfaceOne")))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "Only single inheritance is allowed."
    }
  }

  it should "check if an interface can implement another interface" in {

    //Valid Interface definition
    compute(InterfaceDef("interfaceOne",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      AbstractMethod("AbsMeth2")))

    //Valid Interface inheritance via extends
    compute(InterfaceDef("interfaceTwo",
      Field("Public", "field3", Value(12)),
      Field("Public", "field4"),
      AbstractMethod("AbsMeth3"),
      AbstractMethod("AbsMeth4"),
      Extends("interfaceOne")))

    //Invalid implements operation
    try {
      compute(InterfaceDef("interfaceTwo",
        Field("Public", "field3", Value(12)),
        Field("Public", "field4"),
        AbstractMethod("AbsMeth3"),
        AbstractMethod("AbsMeth4"),
        Implements("interfaceOne")))

    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "An interface cannot implement another interface."
    }
  }

  it should "check if dependency resolution is happening after inheritance" in {

    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    // Class extends abstract class "AbsClass1"
    // Field "field1" will hide the field with same name in the abstract class "AbsClass1"
    compute(ClassDef("fifthClass",
      Field("Public", "field1", Value(19)),
      Field("Public", "field4"),
      Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
      Method("Public", "AbsMeth1", List("localVar2"),
        Assign(Variable("field4"), Variable("localVar2"))),
      Extends("AbsClass1")))

    // Create object of "fifthClass" concrete class
    compute(NewObject("fifthClass", Variable("fifthObject"), List()))

    val actual = compute(GetObjectField("fifthObject", "field1"))
    val expected = 19
    expected shouldBe actual
  }

  it should "check if circular composition is detected" in {

    // Define Abstract Class "AbsClass2"
    compute(AbstractClassDef("AbsClass2",
      AbstractMethod("AbsMeth1")))

    // Define Abstract Class "AbsClass1" which extends Abstract Class "AbsClass2"
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth2"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1"))),
      Extends("AbsClass2")))

    // Modify definition of Abstract Class "AbsClass2" to extend Abstract Class "AbsClass1"
    // This will cause circular inheritance and is not allowed
    try {
      compute(AbstractClassDef("AbsClass2",
        AbstractMethod("AbsMeth1"),
        Extends("AbsClass1")))

    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "Circular inheritance is found."
    }
  }

  it should "check if nested classes/ interfaces work" in {

    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      Method("Public", "Method11", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    //Valid Interface
    compute(InterfaceDef("interfaceTwo",
      Field("Public", "field3", Value(12)),
      Field("Public", "field4"),
      AbstractMethod("AbsMeth3"),
      AbstractMethod("AbsMeth4")))

    // Define Class "sixthClass", Nested class "innerSixthClass" which extends an Abstract Class "AbsClass1"
    // and an nested interface "nestedInterfaceThree" which extends an interface "interfaceTwo"
    compute(ClassDef("sixthClass",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      Constructor(List(), Assign(Variable("field2"), Insert(Value("abc"), Value(3)))),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1"))),
      InnerClassDef("innerSixthClass",
        Field("Public", "field3", Value(19)),
        Field("Public", "field4"),
        Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
        Method("Public", "Method2", List("localVar2"),
          Assign(Variable("field2"), Variable("localVar2"))),
        Method("Public", "AbsMeth1", List("localVar2"),
          Assign(Variable("field2"), Variable("localVar2"))),
        Extends("AbsClass1")),
      InterfaceDef("nestedInterfaceThree",
        Field("Public", "field5", Value(12)),
        Field("Public", "field6"),
        AbstractMethod("AbsMeth5"),
        Extends("interfaceTwo"))))

    // Create Object of outer class and then use it to create object of the nested class
    compute(NewObject("sixthClass", Variable("sixthObject"), List()))
    compute(NewObject("innerSixthClass", Variable("innerSixthObject"), List(), "sixthObject"))

    // Invoke an inherited method of nested class object
    compute(InvokeMethod("innerSixthObject", "Method11", List(Value(14))))

    val actual = compute(GetObjectField("innerSixthObject", "field1"))
    val expected = 14
    expected shouldBe actual
  }

  it should "check access modifiers in abstract classes and interfaces" in {

    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Private", "field2", Value(77)),
      AbstractMethod("AbsMeth1"),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    // Class extends abstract class "AbsClass1"
    // Field "field2" is private and will not be inherited
    compute(ClassDef("fifthClass",
      Field("Public", "field1", Value(19)),
      Field("Public", "field4"),
      Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
      Method("Public", "AbsMeth1", List("localVar2"),
        Assign(Variable("field4"), Variable("localVar2"))),
      Extends("AbsClass1")))

    // Create object of "fifthClass" concrete class
    compute(NewObject("fifthClass", Variable("fifthObject"), List()))
    // Instance variable "field2" will not be found
    try {
      compute(GetObjectField("fifthObject", "field2"))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "Instance variable not found"
    }

    // Interface can contain only public fields
    try {
      compute(InterfaceDef("interfaceOne",
        Field("Public", "field1", Value(12)),
        Field("Private", "field2"),
        AbstractMethod("AbsMeth1"),
        AbstractMethod("AbsMeth2")))
    }
    catch {
      case ex: DSLException => ex.getMessage shouldBe "Only public fields are allowed in an interface."
    }

  }

  it should "check the creation of object and working of method with Inheritance" in {

    // Valid abstract class
    compute(AbstractClassDef("AbsClass1",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2"),
      AbstractMethod("AbsMeth1"),
      Method("Public", "Method11", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    //Valid Interface
    compute(InterfaceDef("interfaceTwo",
      Field("Public", "field3", Value(12)),
      Field("Public", "field4"),
      AbstractMethod("AbsMeth3"),
      AbstractMethod("AbsMeth4")))

    // Class extends abstract class "AbsClass1"
    compute(ClassDef("fifthClass",
      Field("Public", "field1", Value(19)),
      Field("Public", "field4"),
      Constructor(List(), Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
      Method("Public", "AbsMeth1", List("localVar2"),
        Assign(Variable("field4"), Insert(Value("2"), Value("21")))),
      Extends("AbsClass1")))

    // Create object of "fifthClass" concrete class
    compute(NewObject("fifthClass", Variable("fifthObject"), List()))
    // Invoke inherited method
    compute(InvokeMethod("fifthObject", "Method11", List(Value(14))))

    val actual1 = compute(GetObjectField("fifthObject", "field1"))
    val expected1 = 14
    expected1 shouldBe actual1

    // Invoke overridden method
    compute(InvokeMethod("fifthObject", "AbsMeth1", List(Value(100))))

    val actual2 = compute(GetObjectField("fifthObject", "field4"))
    val expected2 = scala.collection.mutable.Set("2", 31, "21", "efg")
    expected2 shouldBe actual2
  }
}