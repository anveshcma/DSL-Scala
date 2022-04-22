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
  it should "check the Insert operation on values99" in {

    compute(Assign(Variable("set5"), Insert(Value("2"), Value(3), Value(89))))
    compute(Assign(Variable("set6"), Insert(Value("a"), Value(89))))
    val setOp = Union(Variable("set5"),Variable("set6"))
    val wrapped = setMonad(setOp)
    def comp(set:Operator) : Operator ={
      set match {
        case Value(x) =>
          return Value(x)
        case Variable(name: String, false) => {
          if (mainBindingMap.contains(name)){
            return Value(mainBindingMap(name))
          } else{
              // Return None if variable is not found
              return Value("N/A")
            }
        }
        case Union(x,y) => {
          val s1 = setMonad(x).map(comp)
          val s2 = setMonad(y).map(comp)
          return Union(s1,s2)
        }
      }
    }
    val actual = wrapped.map(comp)
    val expected = Union(Value(2),Value(3))
    println(compute(actual))
    expected shouldBe actual
  }

}
