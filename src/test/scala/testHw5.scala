/*
Anvesh Koganti
670875073
CS474 - Homework 4
*/
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import DslMethods.*
import DslMethods.Operator.*

import scala.collection.mutable

class testHw5 extends AnyFlatSpec with Matchers {
  behavior of "DslMethods"

  it should "check the partial eval with 1 undefined var -  Union" in {
    // Define 1 set variable
    compute(Assign(Variable("set59"), Insert(Value("abc"), Value(3))))
    // 1 variable is defined and the other is not. Evaluates to partial exp.
    val partialExp = compute(Union(Variable("set59"),Variable("set69")))
    // set59 data is retrieved and substituted but set69 is as it is.
    val expected = Union(Value(Set("abc", 3)),Variable("set69"))
    expected shouldBe partialExp
    // define set6
    compute(Assign(Variable("set69"), Insert(Value("abc"), Value(89))))
    // now when the partial eval expression is called, it performs union and outputs a set.
    val completeEval = compute(PartialEval(partialExp))
    // expected output set data.
    val finalOutput = scala.collection.mutable.Set("abc",3,89)
    finalOutput shouldBe completeEval
  }

  it should "check the partial eval with 2 undefined var - Difference" in {
    // Both variables are not defined. Evaluates to partial exp.
    val partialExp = compute(Difference(Variable("set99"),Variable("set109")))
    // set99 and set109 data are as they are. No change.
    val expected = Difference(Variable("set99"),Variable("set109"))
    expected shouldBe partialExp
    // define set99 and set109
    compute(Assign(Variable("set99"), Insert(Value("abc"), Value(3))))
    compute(Assign(Variable("set109"), Insert(Value("abc"))))
    // now when the partial eval expression is called, it performs difference and outputs a set.
    val completeEval = compute(PartialEval(partialExp))
    // expected output set data.
    val finalOutput = scala.collection.mutable.Set(3)
    finalOutput shouldBe completeEval
  }

  it should "check the partial eval with 2 defined var - SymDiff" in {
    // define set11 and set12
    compute(Assign(Variable("set11"), Insert(Value("2"), Value(3), Value(89))))
    compute(Assign(Variable("set12"), Insert(Value("a"), Value(89))))
    // this expression will be completely evaluated and not partially as both variables are defined
    // therefore PartialEval(...) signature should not be used
    val actual = compute(SymDiff(Variable("set11"),Variable("set12")))
    val expected = scala.collection.mutable.Set("2",3,"a")
    expected shouldBe actual
  }

  it should "check the partial eval and optimization with Insert and Delete" in {
    // define set21
    compute(Assign(Variable("set21"), Insert(Value("abc"), Value(4), Value(89))))
    // As set19 is not defined, it is left as is.
    // set21 is defined. So 3 and data in set21 is optimized into a single Value and partial exp is returned.
    val partialExp = compute(Assign(Variable("set299"), Insert(Variable("set19"),Variable("set21"), Value(3))))
    // Set("abc",3, 4, 89) is the result from optimization operation.
    val expected = Assign(Variable("set299"),Insert(Value(Set("abc",3, 4, 89)), Variable("set19")))
    expected shouldBe partialExp
    // define set19
    compute(Assign(Variable("set19"), Insert(Value(90))))
    // now when the partial eval expression is called, it performs Insert and Assign and outputs a set.
    val completeEval = compute(PartialEval(partialExp))
    // expected output set data.
    val finalOutput = scala.collection.mutable.Set("abc",3, 4, 89,90)
    finalOutput shouldBe completeEval

  }

  it should "check the monad operation with integer doubler transformer function" in {

    compute(Assign(Variable("set55"), Insert(Value(3), Value(15))))
    compute(Assign(Variable("set66"), Insert(Value("as"))))
    val setOp = Union(Variable("set55"),Variable("set66"))
    val wrapped = setMonad(setOp)
    def comp(set:Operator) : Operator ={
      set match {
        case Value(x) =>
          Value(x)
        case Variable(name: String, false) =>
          if (mainBindingMap.contains(name)){
            val temp = mainBindingMap(name).asInstanceOf[mutable.Set[Any]]
            val newSet = mutable.Set[Any]()
            for (e <- temp){
              if (e.isInstanceOf[Int]){
                newSet += e.asInstanceOf[Int]*2
              }else if (e.isInstanceOf[String])
                {
                  newSet += e.asInstanceOf[String]*2
                }
                else{

              }
            }
            Value(newSet)
          } else{
            // Return None if variable is not found
            Value("N/A")
          }
        case Union(x,y) =>
          val s1 = setMonad(x).map(comp)
          val s2 = setMonad(y).map(comp)
          Union(s1,s2)
        case _ =>
          throw DSLException("The defined function cannot optimize this expression.")
      }
    }
    val actual = wrapped.map(comp)
    val expected = Union(Value(2),Value(3))
    println(compute(actual))
    expected shouldBe actual
  }

  it should "check the Insert operation on values99" in {

    compute(Assign(Variable("set5"), Insert(Value("2"), Value(3), Value(89))))
    compute(Assign(Variable("set6"), Insert(Value("a"), Value(89))))
    val setOp = Union(Variable("set5"),Variable("set6"))
    val wrapped = setMonad(setOp)
    def comp(set:Operator) : Operator ={
      set match {
        case Value(x) =>
          Value(x)
        case Variable(name: String, false) =>
          if (mainBindingMap.contains(name)){
             Value(mainBindingMap(name))
          } else{
              // Return None if variable is not found
              Value("N/A")
            }
        case Union(x,y) =>
          val s1 = setMonad(x).map(comp)
          val s2 = setMonad(y).map(comp)
          Union(s1,s2)
        case _ =>
          throw DSLException("The defined function cannot optimize this expression.")
      }
    }
    val actual = wrapped.map(comp)
    val expected = Union(Value(2),Value(3))
    println(compute(actual))
    expected shouldBe actual
  }

}
