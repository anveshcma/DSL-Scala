/*
Anvesh Koganti
670875073
CS474 - Homework 5
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
    val partialExp = compute(Difference(Variable("set991"),Variable("set1091")))
    // set991 and set1091 data are as they are. No change.
    val expected = Difference(Variable("set991"),Variable("set1091"))
    expected shouldBe partialExp
    // define set991 and set1091
    compute(Assign(Variable("set991"), Insert(Value("abc"), Value(3))))
    compute(Assign(Variable("set1091"), Insert(Value("abc"))))
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

  it should "check the monad operation with set elements doubler transformer function" in {
    // Transformer function to double individual set elements and return optimized partial eval expression
    def doubleSetElements(set:Operator) : Operator =
      set match
        // Double set elements of the variable passed
        case Variable(name: String, false) =>
          if (mainBindingMap.contains(name))
            val temp = mainBindingMap(name).asInstanceOf[mutable.Set[Any]]
            val newSet = mutable.Set[Any]()
            for (e <- temp){
              e match
                case x:String => newSet += x*2
                case y:Int => newSet += y*2
                case _ =>
            }
            // return the new doubled elements set
            Value(newSet)
          else
            // Return Variable(name) if variable is not found
            Variable(name)

        // return partially evaluated
        case Union(x,y) =>
          val s1 = setMonad(x).map(doubleSetElements)
          val s2 = setMonad(y).map(doubleSetElements)
          Union(s1,s2)
        case _ =>
          throw DSLException("The defined function cannot optimize this expression.")

    // define set55 and set66
    compute(Assign(Variable("set55"), Insert(Value(10), Value(15))))
    compute(Assign(Variable("set66"), Insert(Value("CS474"))))

    // define the union expression
    val expression = Union(Variable("set55"),Variable("set66"))
    // wrap the set expression with the monad case-class
    val wrappedSet = setMonad(expression)

    // call the map monad function on the expression with doubleSetElements function
    // the map applies the passed function on the elements of the expression
    // we get back partial eval expression as defined in transformer function
    val partialExp = wrappedSet.map(doubleSetElements)
    // Integers are doubled and strings are replicated twice. Partial eval expression.
    val expected = Union(Value(mutable.Set(20, 30)),Value(mutable.Set("CS474CS474")))
    expected shouldBe partialExp

    // now when the partial eval expression is called, it performs Union of the 2 sets.
    val completeEval = compute(PartialEval(partialExp))
    // expected output set data. Integers are doubled and strings are replicated twice.
    val finalOutput = scala.collection.mutable.Set("CS474CS474",20,30)
    finalOutput shouldBe completeEval
  }

  it should "check the monad operation with add default element transformer function" in {
    // Transformer function to add default data to  set elements and return optimized partial eval expression
    def addDefaultElement(set:Operator) : Operator =
      set match
        // add default data to set elements of the variable passed
        case Variable(name: String, false) =>
          if (mainBindingMap.contains(name))
            val temp = mainBindingMap(name).asInstanceOf[mutable.Set[Any]]
            // adding default string to all variables
            temp += "defaultData"
            // return the new doubled elements set
            Value(temp)
          else
          // Return Variable(name) if variable is not found
            Variable(name)

        // return partially evaluated intersection expression
        case Intersection(x,y) =>
          val s1 = setMonad(x).map(addDefaultElement)
          val s2 = setMonad(y).map(addDefaultElement)
          Intersection(s1,s2)
        case _ =>
          throw DSLException("The defined function cannot optimize this expression.")

    // define set432 and set983
    compute(Assign(Variable("set432"), Insert(Value("data1"), Value("data2"))))
    compute(Assign(Variable("set983"), Insert(Value("data1"))))

    // define the Intersection expression
    val expression = Intersection(Variable("set432"),Variable("set983"))
    // wrap the set expression with the monad case-class
    val wrappedSet = setMonad(expression)

    // call the map monad function on the expression with addDefaultElement function
    // the map applies the passed function on the elements of the expression
    // we get back partial eval expression as defined in transformer function
    val partialExp = wrappedSet.map(addDefaultElement)
    //  Partial eval expression.
    val expected = Intersection(Value(mutable.Set("data1","data2","defaultData")),Value(mutable.Set("data1","defaultData")))
    expected shouldBe partialExp

    // now when the partial eval expression is called, it performs Intersection of the 2 sets.
    val completeEval = compute(PartialEval(partialExp))
    // expected output set data. defaultData and data1 are common between the 2 sets.
    val finalOutput = scala.collection.mutable.Set("data1","defaultData")

    finalOutput shouldBe completeEval
  }

  it should "check the monad operation with multiply constant transformer function" in {
    // Transformer function to multiple constant to the first set and return optimized partial eval expression
    def multiplyConstant(set:Operator) : Operator =
      set match
        // add default data to set elements of the variable passed
        case Variable(name: String, false) =>
          if (mainBindingMap.contains(name))
            val temp = mainBindingMap(name).asInstanceOf[mutable.Set[Any]]
            Value(temp)
          else
          // Return Variable(name) if variable is not found
            Variable(name)

        // return partially evaluated intersection expression
        // y will be replaced with Value(mutable.Set("common multiple")) irrespective of what the user enters.
        case Product(x,y) =>
          val s1 = setMonad(x).map(multiplyConstant)
          val s2 = Value(mutable.Set("common multiple"))
          Product(s1,s2)
        case _ =>
          throw DSLException("The defined function cannot optimize this expression.")

    // define set4321
    compute(Assign(Variable("set4321"), Insert(Value("data1"), Value("data2"))))

    // define the Intersection expression. Variable("Replace") will be replaced with value defined in function.
    val expression = Product(Variable("set4321"),Variable("Replace"))
    // wrap the set expression with the monad case-class
    val wrappedSet = setMonad(expression)

    // call the map monad function on the expression with multiplyConstant function
    // the map applies the passed function on the elements of the expression
    // we get back partial eval expression as defined in transformer function
    val partialExp = wrappedSet.map(multiplyConstant)
    //  Partial eval expression.
    val expected = Product(Value(mutable.Set("data1","data2")),Value(mutable.Set("common multiple")))
    expected shouldBe partialExp

    // now when the partial eval expression is called, it performs product of the 2 sets.
    val completeEval = compute(PartialEval(partialExp))
    // expected output set data.
    val finalOutput = scala.collection.mutable.Set(("data1","common multiple"),("data2","common multiple"))

    finalOutput shouldBe completeEval
  }
}
