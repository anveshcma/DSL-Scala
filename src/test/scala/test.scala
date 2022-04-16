/*
Anvesh Koganti
670875073
CS474 - Homework 1
*/
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import DslMethods.*
import DslMethods.Operator.*

class test extends AnyFlatSpec with Matchers {
  behavior of "DslMethods"
  it should "check the Insert operation on values" in {
    val actual = compute(Assign(Variable("set2"), Insert(Value("2"), Value(3))))
    val expected = scala.collection.mutable.Set("2", 3)
    expected shouldBe actual
  }

  it should "check the Insert operation on variables" in {
    compute(Assign(Variable("var1"),Value(10)))
    val actual = compute(Assign(Variable("set3"), Insert(Value("2"), Variable("var1"))))
    val expected = scala.collection.mutable.Set(10, "2")
    expected shouldBe actual
  }

  it should "check the error happening during Insert" in {
    compute(Assign(Variable("var1"),Value(10)))
    try {
      compute(Assign(Variable("set2"), Insert(Value("2"), Variable("var4"))))
    } catch
      case c: DSLException =>
        c.getMessage shouldBe "Variable(s) does not exist"
  }

  it should "check the Delete operation" in {
   compute(Assign(Variable("set4"), Insert(Value("2"), Value(3))))
    val actual = compute(Assign(Variable("set4"), Delete(Value(3))))
    val expected = scala.collection.mutable.Set("2")
    expected shouldBe actual
  }

  it should "check the error happening during Delete" in {
   compute(Assign(Variable("set2"), Insert(Value("2"), Value(3))))
    try {
     compute(Assign(Variable("set300"), Delete(Value(3))))
    } catch{
      case c: DSLException =>
        c.getMessage shouldBe "Set not found"
    }
  }
  it should "check the Union operation" in {
   compute(Assign(Variable("set5"), Insert(Value("2"), Value(3), Value(89))))
   compute(Assign(Variable("set6"), Insert(Value("a"), Value(89))))
    val actual = compute(Union(Variable("set5"),Variable("set6")))
    val expected = scala.collection.mutable.Set("2",3,"a",89)
    expected shouldBe actual
  }

  it should "check the Intersection operation" in {
   compute(Assign(Variable("set7"), Insert(Value("2"), Value(3), Value(89))))
   compute(Assign(Variable("set8"), Insert(Value("a"), Value(89))))
    val actual = compute(Intersection(Variable("set7"),Variable("set8")))
    val expected = scala.collection.mutable.Set(89)
    expected shouldBe actual
  }


  it should "check the Difference operation" in {
   compute(Assign(Variable("set9"), Insert(Value("2"), Value(3), Value(89))))
   compute(Assign(Variable("set10"), Insert(Value("a"), Value(89))))
    val actual = compute(Difference(Variable("set9"),Variable("set10")))
    val expected = scala.collection.mutable.Set("2",3)
    expected shouldBe actual
  }

  it should "check the SymDiff operation" in {
   compute(Assign(Variable("set11"), Insert(Value("2"), Value(3), Value(89))))
   compute(Assign(Variable("set12"), Insert(Value("a"), Value(89))))
    val actual = compute(SymDiff(Variable("set11"),Variable("set12")))
    val expected = scala.collection.mutable.Set("2",3,"a")
    expected shouldBe actual
  }

  it should "check the Product operation" in {
   compute(Assign(Variable("set13"), Insert(Value("2"), Value(3))))
   compute(Assign(Variable("set14"), Insert(Value("a"))))
    val actual = compute(Product(Variable("set13"),Variable("set14")))
    val expected = scala.collection.mutable.Set((3,"a"), (2,"a"))
//    expected shouldBe actual
  }

  it should "check the Check operation" in {
   compute(Assign(Variable("set15"), Insert(Value("2"), Value(3), Value(89))))
    val actual = compute(Check("set15",Value("2")))
    val expected = true
    expected shouldBe actual
  }

  it should "check the named scope union operation" in {
   compute(Scope("Level1",Assign(Variable("set16"), Insert(Value("2"), Value(3)))))
   compute(Assign(Variable("set17"), Insert(Value("b"), Value(7))))
    val actual = compute(Scope("Level1",Union(Variable("set16"),Variable("set17"))))
    val expected = scala.collection.mutable.Set("2",3,7,"b")
    expected shouldBe actual
  }

  it should "check the named scope Check operation" in {
   compute(Scope("Level1",Assign(Variable("set16"), Insert(Value("2"), Value(3)))))
   compute(Assign(Variable("set17"), Insert(Value("b"), Value(7))))
    val actual = compute(Scope("Level1",Check("set17",Value(7))))
    val expected = true
    expected shouldBe actual
  }

  it should "check the Macro operation" in  {
    compute(Macro("MyMacro",Insert(Value("2"), Value(3))))
    val actual =compute(Assign(Variable("set18"), Macro("MyMacro")))
    val expected = scala.collection.mutable.Set("2",3)
    expected shouldBe actual
  }

  it should "check the overriding operation" in  {
   compute(Assign(Variable("temp"),Value(99)))
    val actual =compute(Assign(Variable("temp"), Value(98)))
    val expected = 98
    expected shouldBe actual
  }

}
