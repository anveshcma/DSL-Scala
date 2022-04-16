  /*
  Anvesh Koganti
  670875073
  CS474 - Homework 3
  */
  import org.scalatest.flatspec.AnyFlatSpec
  import org.scalatest.matchers.should.Matchers

  import DslMethods.*
  import DslMethods.Operator.*

  class testClassObject extends AnyFlatSpec with Matchers {
    behavior of "DslMethods"
    it should "check the creation of object and working of method" in {
      // Define a class "firstClass" with
      compute(ClassDef("firstClass",
        Field("Public","field1",Value(12)),
        Constructor(List(),Assign(Variable("field1"), Insert(Value("2"), Value(3)))),
        Method("Public","Method1",List("localVar1"),
          Assign(Variable("field1"),Variable("localVar1")))))

      compute(NewObject("firstClass",Variable("firstObject"),List()))
      compute(InvokeMethod("firstObject","Method1",List(Value(14))))

      val actual = compute(GetObjectField("firstObject","field1"))
      val expected = 14
      expected shouldBe actual
    }

    it should "check the working of shadowing local variable in method" in {
      compute(ClassDef("secondClass",
        Field("Public","field1",Value(12)),
        Field("Public","field2"),
        Constructor(List(),Assign(Variable("field2"), Insert(Value("abc"), Value(3)))),
        Method("Public","Method1",List("localVar1"),
          Assign(Variable("field1",true),Variable("localVar1")))))

      compute(NewObject("secondClass",Variable("secondObject"),List()))
      compute(InvokeMethod("secondObject","Method1",List(Value(14))))

      val actual = compute(GetObjectField("secondObject","field1"))
      val expected = 12
      expected shouldBe actual
    }

    it should "check the functioning of the constructor" in {
      compute(ClassDef("thirdClass",
        Field("Public","field1",Value(12)),
        Field("Public","field2"),
        Constructor(List(),Assign(Variable("field2"), Insert(Value("abc"), Value(3)))),
        Method("Public","Method1",List("localVar1"),
          Assign(Variable("field1",true),Variable("localVar1")))))

      compute(NewObject("thirdClass",Variable("thirdObject"),List()))

      val actual = compute(GetObjectField("thirdObject","field2"))
      val expected = scala.collection.mutable.Set(3, "abc")
      expected shouldBe actual
    }

    it should "check the functioning of extends(Inheritance)" in {
      compute(ClassDef("fourthClass",
        Field("Public","field1",Value(12)),
        Field("Public","field2"),
        Constructor(List(),Assign(Variable("field2"), Insert(Value("abc"), Value(3)))),
        Method("Public","Method1",List("localVar1"),
          Assign(Variable("field1"),Variable("localVar1")))))

      compute(ClassDef("fifthClass",
        Field("Public","field3",Value(19)),
        Field("Public","field4"),
        Constructor(List(),Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
        Method("Public","Method2",List("localVar2"),
          Assign(Variable("field3"),Variable("localVar2"))),
        Extends("fourthClass")))

      compute(NewObject("fifthClass",Variable("fifthObject"),List()))
      compute(InvokeMethod("fifthObject","Method1",List(Value(14))))

      val actual = compute(GetObjectField("fifthObject","field1"))
      val expected = 14
      expected shouldBe actual
    }

    it should "check the functioning inner class" in {
      compute(ClassDef("sixthClass",
        Field("Public","field1",Value(12)),
        Field("Public","field2"),
        Constructor(List(),Assign(Variable("field2"), Insert(Value("abc"), Value(3)))),
        Method("Public","Method1",List("localVar1"),
          Assign(Variable("field1"),Variable("localVar1"))),
        InnerClassDef("innerSixthClass",
          Field("Public","field3",Value(19)),
          Field("Public","field4"),
          Constructor(List(),Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
          Method("Public","Method2",List("localVar2"),
            Assign(Variable("field2"),Variable("localVar2"))))))

      compute(NewObject("sixthClass",Variable("sixthObject"),List()))
      compute(NewObject("innerSixthClass",Variable("innerSixthObject"),List(),"sixthObject"))

      compute(InvokeMethod("innerSixthObject","Method2",List(Value(14))))

      val actual = compute(GetObjectField("innerSixthObject","field2"))
      val expected = 14
      expected shouldBe actual
    }

    it should "check the access modifiers" in {
      compute(ClassDef("seventhClass",
        Field("Public","field1",Value(12)),
        Field("Private","field2"),
        Method("Private","Method1",List("localVar1"),
          Assign(Variable("field1"),Variable("localVar1")))))

      compute(ClassDef("eighthClass",
        Field("Public","field3",Value(19)),
        Field("Public","field4"),
        Constructor(List(),Assign(Variable("field4"), Insert(Value("efg"), Value(31)))),
        Method("Public","Method2",List("localVar2"),
          Assign(Variable("field3"),Variable("localVar2"))),
        Extends("seventhClass")))

      compute(NewObject("eighthClass",Variable("eighthObject"),List()))
      try{
        compute(InvokeMethod("eighthObject","Method1",List(Value(14))))
      }
     catch{
       case c: java.util.NoSuchElementException =>
         c.getMessage shouldBe "key not found: Method1"
     }
    }

      it should "check the working of multiple set operators in multiple methods" in {
        // Define a class "firstClass" with
        compute(ClassDef("firstClass",
          Field("Public","field1",Value(12)),
          Field("Public","field2"),
          Constructor(List("constVar1"),Assign(Variable("field1"), Insert(Value("2"), Value("21")))),
          Method("Public","Method1",List("localVar1"),
            Assign(Variable("field2"),Variable("localVar1")),
            Macro("MyMacro",Insert(Value("2"), Value(3))),
            Assign(Variable("field2"), Macro("MyMacro"))),
          Method("Public","Method2",List("localVar2"),
            Assign(Variable("field1"),Variable("localVar2")),
            Assign(Variable("localVar3"),Variable("localVar2")),
            Macro("MyMacro",Insert(Value("2"), Value("22"))),
            Assign(Variable("field1"), Macro("MyMacro")))))

        compute(NewObject("firstClass",Variable("firstObject"),List(Value(100))))
        compute(InvokeMethod("firstObject","Method1",List(Value(14))))
        compute(InvokeMethod("firstObject","Method2",List(Value(99))))

        val actual = compute(GetObjectField("firstObject","field1"))
        val expected = scala.collection.mutable.Set("2","22")
        expected shouldBe actual
      }
  }

