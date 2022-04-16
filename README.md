# CS 474 - HW 4
Anvesh Koganti <br />
670875073<br /><br />
HW-4 specific documentation can be found in this section:
[Exception Definition and Usage](#exception-definition-and-usage) and [If construct](#if-construct)
<br />
Test class specific to HW-4: testHW4.scala
# Usage
Use 'sbt clean compile test' to run the test script. Use 'sbt clean compile run' to run the main method. <br />
To use the methods from the DslMethods.scala file use these 2 lines at the top of the file: <br />
`import DslMethods.*` <br />
`import DslMethods.Operator.*` <br/>

## Notes
- Names of the classes, abstract classes and interfaces must be unique within a scope.
- Virtual dispatch is enabled and overriding methods are automatically invoked.
- Reserved keywords: MainMap, ClassMap, parentReference
# Language Syntax
All operators must be enclosed in "compute(...)"

## Exception Definition and Usage
```
//Exception Class definition
compute(
    ExceptionClassDef("ExceptionClass1",Field("Private", "Reason")))
    
// Try Catch Syntax    
compute(CatchException("ExceptionClass1",
    Block(IF(GetObjectField("firstObject", "field1"),
        Block(Assign(Variable("set1"), Insert(Value("2"), Value(3)))),
        Block(Assign(Variable("set2"), Insert(Value("yt"), Value(45))),
            ThrowException("ExceptionClass1", Assign(Variable("Reason"), Value("Check Failed"))))),
        Assign(Variable("set3"), Insert(Value("2"), Value(3)))),
    Catch("ExceptionObject1",Assign(Variable("Var1"), GetObjectField("ExceptionObject1", "Reason")))))        

```
### ExceptionClassDef
`ExceptionClassDef`(_ClassName_(String), _Expressions_*):
This signature is used to define an Exception Class. <br />
ClassName must be a string. <br />
Valid expressions include Field and Method.

### CatchException
`CatchException`(_ClassName_(String), _TryBlock_(Block), _CatchClause_(Catch) ):
This signature is used to define a try-catch block. <br />
ClassName must be the name of the exception class which has to be caught in form of a string. <br />
TryBlock is a Block of code. The code in this block gets executed line by line. <br />
CatchClause is used to catch an exception object.

### ThrowException
`ThrowException`(_ClassName_(String), _Expressions_*):
This signature is used to create and throw and exception object of _ClassName_ class . <br />
ClassName must be the name of the exception class of which an object is to be created and thrown. <br />
Valid expressions include Assign. The expressions must be used to set the fields of the object being created.<br />
*Please note when using Assign to set values, the right part of assign operation must be a Value and not Variable. 

### Catch
`Catch`(_ClassName_(String), _Expressions_*):
This signature is used to create and throw and exception object of _ClassName_ class . <br />
ClassName must be the name of the exception class of which an object is to be created and thrown. <br />
Valid expressions include Assign. The expressions must be used to set the fields of the object being created.<br />
*Please note when using Assign to set values, the right part of assign operation must be a Value and not Variable.

## If construct
```
compute(
    IF(Check("set15", Value("7")),
        Block(Assign(Variable("set2"), Insert(Value("2"), Value(3))),// THEN CLAUSE
        Assign(Variable("set3"), Insert(Value("4"), Value(5)))),     // THEN CLAUSE
        Block(Assign(Variable("set499"), Insert(Value("6"), Value(7)))))) // ELSE CLAUSE

```
### If Construct
`IF`(_Condition_(Expression), _ThenClause_(Block), _ElseClause_(Block) ):
This signature is used to define an if-else construct. <br />
Condition must a single expression which must evaluate to either true or false.  <br />
ThenClause is a Block of code which gets evaluated if the condition evaluates to true.  <br />
ElseClause is a Block of code which gets evaluated if the condition evaluates to false.

### Block
`Block`(_Expressions_(All valid expressions)):
This signature is used to write a block of code. <br />
Expressions are comma separated lines of codes.The code in this block gets executed line by line.
## Class Definition
```
compute(
    ClassDef("ClassName",
    Field("Public","publicField",Value(12)),                                
    Field("Private","privateField"),
    Constructor(List("parameter1"),
        Assign(Variable("privateField"), Insert(Value("abc"), Variable("parameter1")))),
    Method("Public","publicMethod",List("parameter2"),
        Assign(Variable("publicField"),Variable("parameter2")))))
```
Different modules used in the above example are explained below:
### ClassDef
`ClassDef`(_ClassName_(String), _Expressions_(All valid modules)):
This signature is used to start defining a Class. <br />
ClassName must be a string. <br />
Valid expressions include Field, Constructor, Method, Extends, InnerClassDef.

### Field
`Field`(_Access_(Public/Private/Protected), _FieldName_(String), _Value_(Optional)):
This signature is used to declare a field.<br />
Access must be one of "Public"/"Private"/"Protected". <br />
FieldName must be string.<br />
Value is optional. It has to be of the form Value(data).

### Constructor
`Constructor`(_ParameterList_, _Expressions_(All valid expressions)):
This signature is used declare a constructor. <br />
ParameterList must be a list of parameters in form of list of strings. <br />
Valid expressions include all the Set operations(HW-1). <br />
***Please note that if any parameter shares name with an existing field, the field gets hidden and cannot be accessed. Please use different names for parameters and field names.**

### Method
`Method`(_Access_(Public/Private/Protected), _MethodName_(String), _ParameterList_, _Expressions_(All valid expressions)):
This signature is used to declare a method.<br />
Access must be one of "Public"/"Private"/"Protected". <br />
MethodName must be string.<br />
ParameterList must be a list of parameters in form of list of strings. <br />
Valid expressions include all the Set operations(HW-1), Object declaration and method calls. <br />
***Please note that if a local variable/ parameter is named same as a Class field(Instance Variable), the instance variable is hidden and can no longer be accessed. Please use different names for local variables/parameters and field names.**<br />
***Please note that if a local variable having same name as that of an instance variable has to be declared and used inside method, put a True flag while using Variable(name,True). This allows for creation of a new variable of given name in the current scope instead of returning the instance variable.** 
## Inheritance (Class, Abstract Class and Interface)
#### Parent Class
```
compute(
    ClassDef("ParentClass",
    Field("Public","publicField1",Value(100)),
    Method("Public","publicMethod",List("parameter1"),
        Assign(Variable("publicField1"),Variable("parameter1")))))
```
#### Interface
```
compute(
    InterfaceDef("InterfaceOne",
    Field("Public", "publicField1", Value(12)),
    Field("Public", "publicField2"),
    AbstractMethod("AbstractMethod1"),
    AbstractMethod("AbstractMethod2")))
```
#### Abstract Class
```
compute(
    AbstractClassDef("AbstractClassOne",
    Field("Public", "publicField1", Value(12)),
    Field("Public", "publicField2"),
    Constructor(List(), Assign(Variable("publicField1"), Insert(Value("2"), Value(3)))),
    AbstractMethod("AbstractMethod1"),
    AbstractMethod("AbstractMethod2"),
    Method("Public", "publicMethod", List("localVar1"),
        Assign(Variable("publicField1"), Variable("localVar1")))))
```
#### Using _Extends_

```
// A class can extend another class/ abstract class 
// An abstract class can extend another class/ abstract class
// An interface can extend another interface

compute(InterfaceDef("InterfaceTwo",
    AbstractMethod("AbstractMethod3"),
    Extends("InterfaceOne")))
    
compute(
    AbstractClassDef("AbstractClassTwo",
    Field("Public", "publicField3"),
    AbstractMethod("AbstractMethod3"),
    Method("Public", "publicMethod2", List("localVar1"),
        Assign(Variable("publicField3"), Variable("localVar1"))),
    Extends("AbstractClassOne")))    
```
#### Using _Implements_
```
// A class can implement an interface
// An abstract class can implement an interface

compute(
    ClassDef("ConcreteClass",
    Field("Public", "publicField3", Value(19)),
    Field("Public", "publicField4"),
    Constructor(List(), Assign(Variable("publicField4"), Insert(Value("efg"), Value(31)))),
    Method("Public", "AbstractMethod1", List("localVar2"),
        Assign(Variable("publicField2"), Variable("localVar2"))),
    Method("Public", "AbstractMethod2", List("localVar3"),
        Assign(Variable("publicField4"), Variable("localVar3"))),
    Implements("interfaceOne")))
```
#### Nested Class, Interface and Abstract Class
```
compute(
    ClassDef("OuterClass",
    Field("Public","outerClassField",Value(12)),
    Constructor(List("tempData"),
        Assign(Variable("outerClassField"), Insert(Value("abc"), Variable("tempData")))),
    Method("Public","publicOuterMethod",List("parameter2"),
        Assign(Variable("outerClassField"),Insert(Value("2"), Variable("parameter2")))),
    InnerClassDef("innerClass",
        Field("Public","innerClassField",Value(0)),
        Method("Public","AbstractMethod1",List("parameter3"),
            Assign(Variable("innerClassField"),Insert(Value("2"), Variable("parameter3")))),
        Method("Public","AbstractMethod2",List("parameter3"),
            Assign(Variable("innerClassField"),Insert(Value("2"), Variable("parameter3")))),
        Extends("AbstractClassOne")),
    InterfaceDef("nestedInterface",
        Field("Public", "field5", Value(12)),
        Field("Public", "field6"),
        AbstractMethod("AbstractMethod3"),
        Extends("interfaceTwo"))))

```
###  InterfaceDef
`InterfaceDef`(_InterfaceName_(String), _Expressions_(Valid modules)):
This signature is used to start defining an Interface. <br />
InterfaceName must be a string. <br />
Valid expressions include Field (public access only), AbstractMethod, Extends( another interface).<br /> 
***Same signature can be used to define nested interfaces.**
### AbstractClassDef
`AbstractClassDef`(_AbstractClassName_(String), _Expressions_(Valid modules)):
This signature is used to start defining an Abstract Class. <br />
AbstractClassName must be a string. <br />
Valid expressions include Field , Constructor, AbstractMethod, Method, Extends( class/ another abstract class), Implements( interface).<br />
***Same signature can be used to define nested abstract classes.**
### AbstractMethod
`AbstractMethod`(_AbstractMethodName_(String)):
This signature is used to start defining an Abstract Method. <br />
AbstractMethodName must be a string. <br />
***To implement this abstract method, declare a concrete method with the same name.**
### Extends 
`Extends`(_ParentName_(String)):
This signature is used implement the inheritance functionality. <br />
ParentName must be a string which is the name of the parent class/ abstract class / interface from which the child class/ abstract class/ interface inherit. <br />
***Please note that when inheriting from a parent class, make sure that the parent class/ abstract class does not have a constructor with Parameters.**<br />
**A class can extend another class/ abstract class**<br />
**An abstract class can extend another class/ abstract class**<br />
**An interface can extend another interface**

### Implements
`Implements`(_InterfaceName_(String)):
This signature is used implement the inheritance functionality using interfaces. <br />
InterfaceName must be a string which is the name of the interface from which the child class/ abstract class inherit. <br />
***A class can implement an interface**<br />
**An abstract class can implement an interface**<br />


### InnerClassDef
`InnerClassDef`(_InnerClassName_, _Expressions_(All valid modules)):
This signature is used to define an inner class within an outer class. <br />
InnerClassName must be a string.<br />
Valid expressions include Field, Constructor, Method, Extends, Implements.<br />
***Please note that currently only one level of inner class definition is supported**

## Object creation and method call

```
compute(NewObject("ParentClass",Variable("ParentObject"),List(Value(100))))

compute(InvokeMethod("ParentObject","publicMethod",List(Value(14))))

compute(GetObjectField("ParentObject","publicField1"))
```
### NewObject
`NewObject`(_ClassName_, _ObjectName_, _AttributeList_, _ParentObject_(Optional)):
This signature allows for creation of an instance of class (object). <br />
Classname is the name of the class which is to be instantiated. This is a string.<br />
ObjectName is the variable to which the object has to be bound to. This is a Variable(String).<br />
AttributeList is a list of attributes(Values/Variables)  which is passed to the constructor (As defined during class declaration).<br />
ParentObject must be passed if an inner class is being instantiated. This will be a name of the outer class object as a string. <br />

### InvokeMethod
`InvokeMethod`(_ObjectName_, _MethodName_, _AttributeList_):
This signature is used to invoke methods of objects. It returns the result from the last expression call.<br />
ObjectName is the name of the object as a string.<br />
MethodName si the name of the method of the object as a string. <br />
AttributeList is a list of attributes(Values/Variables) which is passed to the method (As defined during class declaration).<br />

### GetObjectField
`GetObjectField`(_ObjectName_, _FieldName_):
This signature is used to retrieve an instance variable of an object.<br />
ObjectName is the name of the object as a string.<br />
FieldName is the name of the field as a string. <br />

## Set Operations
### Value
Value(Parameter): Returns the Parameter.<br />
Parameter can be any value like integer, string etc.<br />
Returns the data bound to Parameter1<br /><br />

Usage: <br />
* Operation: Represent integer 5 for use in other operations
* Previous state: N/A
* Command: `compute(Value(5)`<br />
* Result: 5

### Variable
Variable(Name, overrideFlag(Optional)): Used to create a binding or retrieve data bound to Name. <br />
Name must be a string.<br />
If a variable with a certain name has to be declared in the current scope even if there exists one in parent scope there by shadowing it, set the overrideFlag to True. It is False by default.<br />

Usage: <br />
* Operation: Retrieve data bound to var1 = 10
* Previous state: var1 = 10
* Command: `compute(Variable("var1"))`<br />
* Result: 10

### Assign
Assign(Parameter1, Parameter2): Used to bind the resultant from the evaluation of Parameter 2 to the variable in Parameter1.<br />
Parameter1 : Variable(String)<br />
Parameter2 : Can be a method call like `Insert()` or simple `Value()`<br />
Returns the data bound to Parameter1<br /><br />

Usage: <br />
* Operation: Assign var1 = 10
* Previous state: N/A
* Command: `compute(Assign(Variable("var1"),Value(10)))`<br />
* Result: var1 = 10

### Insert
Insert(Parameter*): Used to insert the parameters in the arguments into a set. <br />
Each parameter can a `Variable()` or a `Value()`.<br />
**Has to be used with Assign** to perform the insert operation<br /><br />

Usage:<br />
* Operation: Insert string "g" and data in "var1" into a set named "set3".
* Previous state: var1 = 10, set3 is empty set<br />
* Command: `compute(Assign(Variable("set3"), Insert(Value("g"), Variable("var1"))))`<br />
* Result: set3 = Set(“g”,10).<br />

### Delete
Delete(Parameter*): Used to delete the parameters in the arguments from a set. <br />
Each parameter can a `Variable()` or a `Value()`.<br />
**Has to be used with Assign** to perform the insert operation<br /><br />
Usage: <br />
* Operation: Delete string "g" from a set named "set4" <br />
* Previous state: set4 = Set(“g”,3)<br />
* Command: `compute(Assign(Variable("set4"), Delete(Value(3))))`<br />
* Output: set4 = Set(“g”)<br />

### Union
Union(Parameter1, Parameter2): Performs the union of the set elements in the two sets and returns a new set containing the result.<br />
Parameter1 & Parameter2 have to be sets. So they need to have the signature `Variable(“setname”)`.<br />
Can be used with the Assign method to save the result.<br /><br />
Usage: <br />
* Operation: Union set5 and set6 elements <br />
* Previous state: set5 = Set(“g”,3,89), set6 = Set(“a”,89)<br />
* Command: `compute(Union(Variable("set5"),Variable("set6")))`<br />
* Output: Set(“g”,3,”a”,89)<br />

### Intersection
Intersection(Parameter1, Parameter2): Performs the intersection of the set elements in the two sets and returns a new set containing the result.<br />
Parameter1 & Parameter2 have to be sets. So they need to have the signature `Variable(“setname”)`.<br />
Can be used with the Assign method to save the result.<br /><br />
Usage: <br />
* Operation: Intersection of set5 and set6 <br />
* Previous state: set5 = Set(“g”,3,89), set6 = Set(“a”,89)<br />
* Command: `compute(Intersection(Variable("set5"),Variable("set6")))`<br />
* Output: Set(89)<br />

### Difference
Difference(Parameter1, Parameter2): Returns a set containing elements of the first set that are not present in the second set.<br />
Parameter1 & Parameter2 have to be sets. So they need to have the signature `Variable(“setname”)`.<br />
Can be used with the Assign method to save the result.<br /><br />
Usage: <br />
* Operation: Difference of set5 and set6 <br />
* Previous state: set5 = Set(“g”,3,89), set6 = Set(“a”,89)<br />
* Command: `compute(Difference(Variable("set5"),Variable("set6")))`<br />
* Output: Set(“g”,3)<br />

### Symmetric Difference
SymDiff(Parameter1, Parameter2): Returns a set containing elements that are present in exactly one of the sets but not the both.<br />
Parameter1 & Parameter2 have to be sets. So they need to have the signature `Variable(“setname”)`.<br />
Can be used with the Assign method to save the result.<br /><br />
Usage: <br />
* Operation: Symmetric difference of set5 and set6 <br />
* Previous state: set5 = Set(“g”,3,89), set6 = Set(“a”,89)<br />
* Command: `compute(SymDiff(Variable("set5"),Variable("set6")))`<br />
* Output: Set(“g”,3,”a”)<br />

### Product
Product(Parameter1, Parameter2): Returns a set containing all possible ordered pairs.<br />
Parameter1 & Parameter2 have to be sets. So they need to have the signature `Variable(“setname”)`.<br />
Can be used with the Assign method to save the result.<br /><br />
Usage: <br />
* Operation: Product of set5 and set6 <br />
* Previous state: set5 = Set(“g”,3), set6 = Set(“a”)<br />
* Command: `compute(Product(Variable("set5"),Variable("set6")))`<br />
* Output: Set((3,"a"), ("g","a"))<br />

### Check
Check(Name, Parameter): Checks if the value Parameter is present in a set bound to the variable Name.<br />
Parameter has to of signature `Value(string/number)`.<br />
Returns True if present else returns False. <br /><br />
Usage:<br />
* Operation: Check if 5 is present in set6 <br />
* Previous state: set6 = Set(“a”)<br />
* Command: `compute(Check("set6",Value(5)))`<br />
* Output: False <br />

### Scope
case Scope(Name, Parameter): Creates a scope inside the current scope with the labelled Name and evaluates Parameter within this scope.<br /> ***If a named scope is needed Name has to be a non-empty string and if anonymous scope is needed, Name has to be ""***.
<br />
Usage: Named Scope<br />
* Operation: Create a scope named "level1" and insert elements 5 and 6 into a new set named "set9"  <br />
* Previous state: N/A <br />
* Command: `compute(Scope("level1",Assign(Variable("set9"),Insert(Value(5),Value(6)))))`<br />
* Output: "set9"= Set(5,6) in scope "level1" <br />
<br />

Usage: Anonymous Scope<br />
* Operation: Create an anonymous scope and Union sets set5 and set6  <br />
* Previous state: set5 = Set(“g”,3), set6 = Set(“a”) <br />
* Command: `compute(Scope("",Union(Variable("set5"),Variable("set6"))))` <br />
* Output: Set(“g”,3,“a”) in scope "". This data is available only within this scope. Once this scope is exited, the data cannot be accessed again. <br />

### Macro
Macro(Name, Parameter(optional)): Creates a macro labelled Name and stores the operation in Parameter. If both Name and Parameter are supplied, a new macro will be created with label Name. If `Macro()` is called only with Name attribute, it returns the stored operation. <br />
Usage:<br />
* Operation: Store macro labelled "insert 5" <br />
* Previous state: N/A <br />
* Command: `compute(Macro("insert 5",Insert(Value(5))))`<br />
* Output: when ever macro "insert 5" is called it will be replaced with `Insert(Value(5))`  <br />

