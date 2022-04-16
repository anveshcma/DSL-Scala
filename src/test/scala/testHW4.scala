/*
Anvesh Koganti
670875073
CS474 - Homework 4
*/
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import DslMethods.*
import DslMethods.Operator.*

class testHW4 extends AnyFlatSpec with Matchers {
  behavior of "DslMethods"

  it should "check the working of branching construct IF" in {

    compute(Assign(Variable("set15"), Insert(Value("2"), Value(3), Value(89))))

    // Condition: Check if String 7 is present in set15 -> Evaluates to False.
    // Execute Else branch
    compute(IF(Check("set15", Value("7")),
      Block(Assign(Variable("set2"), Insert(Value("2"), Value(3))),
        Assign(Variable("set3"), Insert(Value("4"), Value(5)))),
      Block(Assign(Variable("set499"), Insert(Value("6"), Value(7))))))

    // set4 is defined in Else branch
    val actual = compute(Variable("set499"))
    val expected = scala.collection.mutable.Set("6", 7)

    expected shouldBe actual

  }

  it should "Check if lazy evaluation is working in IF construct" in {

    compute(Assign(Variable("set10"), Insert(Value(11), Value(22))))

    // Condition: Check if Integer 11 is present in set10 -> Evaluates to True.
    // Execute If branch only
    // The Block of code defined in Else branch is never called and
    // hence is never evaluated even though it has been defined.
    // The blocks of code in If and Else blocks are lazily evaluated.
    compute(IF(Check("set10", Value(11)),
      Block(Assign(Variable("set33"), Insert(Value("2"), Value(3))),
        Assign(Variable("set34"), Insert(Value("4"), Value(5)))),
      Block(Assign(Variable("set49"), Insert(Value("6"), Value(7))))))

    // set49 is not defined as the block of code is not called even though it was defined.
    // Hence it evaluates to "N/A".
    val actual = compute(Variable("set49"))
    val expected = "N/A"

    expected shouldBe actual
  }

  it should "check the functioning of single try catch block" in {
    // Class definition
    compute(ClassDef("firstClass",
      Field("Public", "field1", Value(12)),
      Field("Public", "field2", Value(18)),
      Constructor(List(), Assign(Variable("field1"), Insert(Value("2"), Value(3)))),
      Method("Public", "Method1", List("localVar1"),
        Assign(Variable("field1"), Variable("localVar1")))))

    // Create object and invoke method
    compute(NewObject("firstClass", Variable("firstObject"), List()))
    compute(InvokeMethod("firstObject", "Method1", List(Value(false))))

    // Exception Class definition
    compute(ExceptionClassDef("ExceptionClass1", Field("Private", "Reason")))

    // Try Catch block.
    // Try code block has If else condition block. Condition evaluates to False. So else block triggers.
    // Else block has ThrowException call.
    // After exception is thrown no line below it gets executed till exception is caught.
    compute(CatchException("ExceptionClass1",
      Block(IF(GetObjectField("firstObject", "field1"),
        Block(Assign(Variable("set288"), Insert(Value("2"), Value(3)))),
        Block(Assign(Variable("set676"), Insert(Value("yt"), Value(45))),
          ThrowException("ExceptionClass1", Assign(Variable("Reason"), Value("Check Failed"))))),
        Assign(Variable("set377"), Insert(Value("2"), Value(3)))),
      Catch("ExceptionObject1", Assign(Variable("TestVar4"), GetObjectField("ExceptionObject1", "Reason")))))

    // set 377 is not defined as this line of code does not execute as it is defined after throw exception call.
    val actual = compute(Variable("set377"))
    val expected = "N/A"
    expected shouldBe actual
    // Verify the Reason field of the thrown exception object.
    val actual2 = compute(GetObjectField("ExceptionObject1", "Reason"))
    val expected2 = "Check Failed"
    expected2 shouldBe actual2
  }

  it should "check the functioning of nested try catch blocks" in {

    compute(Assign(Variable("set155"), Insert(Value("2"), Value(3), Value(89))))

    // Exception Class definitions
    compute(ExceptionClassDef("ExceptionClass11", Field("Private", "Reason")))
    compute(ExceptionClassDef("ExceptionClass22", Field("Private", "Reason")))

    // Outer try-catch block ExceptionClass11 within which another try-catch ExceptionClass22 is declared.
    // Exception of class ExceptionClass11 is thrown inside try-catch declared to catch ExceptionClass22.
    // As expected it is not handled there and it is passed on to outer try-catch block which can handle ExceptionClass1 exception.
    compute(CatchException("ExceptionClass11",
      Block(IF(Check("set155", Value("7")),
        Block(Assign(Variable("set28453"), Insert(Value("2"), Value(3)))),
        Block(CatchException("ExceptionClass22",
          Block(IF(Check("set155", Value("2")),
            Block(ThrowException("ExceptionClass11", Assign(Variable("Reason"), Value("Check Failed")))),
            Block(Assign(Variable("set254"), Insert(Value("2"), Value(3)))))),
          Catch("ExceptionObject22", Assign(Variable("TestVar4"), GetObjectField("ExceptionObject22", "Reason"))))))),
      Catch("ExceptionObject11", Assign(Variable("TestVar4"), GetObjectField("ExceptionObject11", "Reason")))))

    // Verify the Reason field of the thrown ExceptionObject1 caught by outer try-catch block.
    val actual = compute(GetObjectField("ExceptionObject11", "Reason"))
    val expected = "Check Failed"
    expected shouldBe actual

    // Inner try-catch block does not catch exception of type ExceptionObject1 and is passed on.
    // Therefore ExceptionObject2 does not get created.
    try {
      compute(GetObjectField("ExceptionObject22", "Reason"))
    } catch
      case c: NoSuchElementException =>
        c.getMessage shouldBe "key not found: ExceptionObject22"
  }

  it should "check if try catch block works properly if no exception occurs" in {

    compute(Assign(Variable("set99"), Insert(Value("2"), Value(3), Value(89))))

    // Exception Class definition
    compute(ExceptionClassDef("ExceptionClass3", Field("Private", "Reason")))

    // If block gets executed and line after the IF clause also gets called as no exception gets thrown
    compute(CatchException("ExceptionClass3",
      Block(IF(Check("set99", Value("2")),
        Block(Assign(Variable("set2881"), Insert(Value("2"), Value(3)))),
        Block(Assign(Variable("set6761"), Insert(Value("yt"), Value(45))),
          ThrowException("ExceptionClass3", Assign(Variable("Reason"), Value("Check Failed"))))),
        Assign(Variable("set3771"), Insert(Value("2"), Value(3)))),
      Catch("ExceptionObject3", Assign(Variable("TestVar4"), GetObjectField("ExceptionObject3", "Reason")))))

    // set 3771 is defined as this line of code gets executed after IF clause.
    val actual = compute(Variable("set3771"))
    val expected = scala.collection.mutable.Set(3, "2")
    expected shouldBe actual

    // Therefore ExceptionObject3 does not get created as catch clause does not get called
    // no exception was thrown.
    try {
      compute(GetObjectField("ExceptionObject3", "Reason"))
    } catch
      case c: NoSuchElementException =>
        c.getMessage shouldBe "key not found: ExceptionObject3"
  }
}
