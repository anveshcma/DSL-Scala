/*
Anvesh Koganti
670875073
CS474 - Homework 5
*/

import com.sun.jdi.InvalidTypeException

import scala.annotation.tailrec
import scala.collection.mutable

// Custom exception class
final case class DSLException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

object DslMethods {
  // Map to store all Exception related data
  val tempExceptionMap:  mutable.Map[String, Any] = mutable.Map("name" ->"")
  private val exceptionDataMap: mutable.Map[String,Any] = mutable.Map("exceptionOccurred" -> false, "exceptionObjectData" ->tempExceptionMap)
  // Map to store all the variable bindings and objects
  val mainBindingMap: mutable.Map[String,Any] = mutable.Map("MainMap" -> true)
  // Map to store the macros
  private val macrosMap: mutable.Map[String,Operator] = mutable.Map()
  // Map to store all the Class definitions
  private val classMap: mutable.Map[String,Any] = mutable.Map("ClassMap" -> true)
  // Wrapper class to aid Assign operator handle Insert and Delete operations
  case class operationWrapper(operationType:String,set:Operator)
  // HW-5
  // Signature of monadic function with map
  trait SetExpression:
    def map(f: Operator => Operator): Operator
  // Wrapper object to handle the application of map operation
  case class setMonad(setExp: Operator) extends SetExpression:
    // setMonad map accepts function f and the function is applied on the expression
    override def map(f: Operator => Operator): Operator =
      f(setExp)

  enum Operator:
    // Acceptable Set operations
    case Value(input: Any)
    case Variable(name: String, shouldOverride : Boolean = false)
    case Assign(name:Operator,value:Operator)
    case Insert(args: Operator*)
    case Delete(args: Operator*)
    case Union(input1: Operator, input2: Operator)
    case Difference(input1: Operator, input2: Operator)
    case Intersection(input1: Operator, input2: Operator)
    case SymDiff(input1: Operator, input2: Operator)
    case Product(input1: Operator, input2: Operator)
    case Check(name: String, value: Operator)
    case Scope(name:String,value:Operator)
    case Macro(name: String, value: Operator = Value("ND"))
    // Class and object related
    case ClassDef(name:String,args: Operator*)
    case Constructor(parameterList: List[String] ,exps: Operator*)
    case Field(access: String = "Public", name: String,value: Any = None)
    case Method(access: String = "Public", name: String, parameterList: List[String], exps:Operator*)
    case NewObject(name: String, exp: Operator,parameterValues: List[Operator], parentObject: String ="None")
    case InvokeMethod(objName: String, methodName: String, parameterValues: List[Operator])
    case Extends(className : String)
    case GetObjectField(objName: String, instanceVar: String)
    case InnerClassDef(name:String,args: Operator*)
    // Inheritance related - HW 3
    case InterfaceDef(name:String,args: Operator*)
    case AbstractClassDef(name:String,args: Operator*)
    case Implements(interfaceName: String)
    case AbstractMethod(name: String)
    // HW 4
    case Block(exps: Operator*)
    case IF(condition: Operator, thenClause: Block, elseClause: Block)
    case CatchException(name: String, tryBlock: Block, catchClause: Catch)
    case ExceptionClassDef(name: String, args: Operator*)
    case Catch(name: String, exps: Operator*)
    case ThrowException(name: String, args: Operator* )
    // HW 5
    case PartialEval(exps: Any)


  import DslMethods.Operator.*
  // Method designed to implement various operations defined in the enum Operator
  def compute(op: Operator, scopeMap:mutable.Map[String,Any]=mainBindingMap ): Any = {
    // Check "exceptionOccurred" flag. Execute only if it is false else check fo occurrence of catch block.
    if (!exceptionDataMap("exceptionOccurred").asInstanceOf[Boolean])
    {
      op match {

      // Partial eval expression can be called by invoking it by wrapping it with this wrapper
      case PartialEval(exps) =>
        compute(exps.asInstanceOf[Operator])

      // Exception Class definition
      case ExceptionClassDef(name, args*) =>
        // use classCreationHelper to create Exception class in classMap
        val scopeData = classMap
        classCreationHelper(name,"Exception",scopeData,args*)

      // Try catch block definition
      case CatchException(name, tryBlock, catchClause ) =>
        // Execute the try block
        compute(tryBlock,scopeMap)
        // Check if there was an exception thrown and verify the exception class of which object is created
        val exceptionMetaData = exceptionDataMap("exceptionObjectData").asInstanceOf[mutable.Map[String,Any]]
        if (exceptionDataMap("exceptionOccurred").asInstanceOf[Boolean] &
          exceptionMetaData("name").asInstanceOf[String] == name )
        {
          compute(catchClause,scopeMap) // Called when flag is true it is correct
                                        // catch statement designed to handle the exception thrown
        }
        else{}

      // Throw Exception
      case ThrowException(name, args*) =>

        // Retrieve exception class definition data
        val classData = classMap(name).asInstanceOf[mutable.Map[String,Any]]

        // Check if object of Exception Class is being created, else stop.
        if (classData("DefType") != "Exception") throw DSLException("ThrowException has to be used with exception class.")

        val newMap:  mutable.Map[String, Any] = mutable.Map()
        // Enter fields
        newMap.put("name",name )
        newMap.put("Field",classData("PrivateField").asInstanceOf[mutable.Map[String,Any]].clone())
        // Store methods
        newMap.put("Method",classData("PrivateMethod").asInstanceOf[mutable.Map[String,Any]])
        // Store the new exception object
        exceptionDataMap.put("exceptionObjectData",newMap)

        // Execute the command on the object fields
        val localScope = newMap("Field").asInstanceOf[mutable.Map[String,Any]]
        args.map( i =>compute(i,localScope))
        // Raise exception flag
        exceptionDataMap.put("exceptionOccurred",true)

      // Catch empty declaration
      case Catch(name, exps*) =>

      // IF ELSE construct
      case IF(condition, thenClause, elseClause) =>
        // Execute the condition expression
        val conditionVal = compute(condition)
        // Lazy evaluate block based on the condition
        if (!conditionVal.isInstanceOf[Boolean])
        {
          throw DSLException("If Condition check does not yield a boolean value!")
        }
        else{
          if (conditionVal == true) {compute(thenClause,scopeMap)}
            else {compute(elseClause,scopeMap)}
        }

      // Execute block of code
      case Block(exps*) =>
        exps.map( i =>compute(i,scopeMap))

      // Class definition
      case ClassDef(name, args*) =>
        val scopeData =  if (scopeMap.contains("MainMap")) classMap else scopeMap("InnerMembers").asInstanceOf[mutable.Map[String,Any]]
        // Call helper method with defType = "Concrete" and pass the Scope Map
        classCreationHelper(name,"Concrete",scopeData,args*)

      // Interface definition
      case InterfaceDef(name, args*) =>
        val scopeData =  if (scopeMap.contains("MainMap")) classMap else scopeMap("InnerMembers").asInstanceOf[mutable.Map[String,Any]]
        // Call helper method with defType = "Interface" and pass the Scope Map
        classCreationHelper(name,"Interface",scopeData,args*)

      // Abstract Class definition
      case AbstractClassDef(name, args*) =>
        val scopeData =  if (scopeMap.contains("MainMap")) classMap else scopeMap("InnerMembers").asInstanceOf[mutable.Map[String,Any]]
        // Call helper method with defType = "AbstractClass" and pass the Scope Map
        classCreationHelper(name,"AbstractClass",scopeData,args*)


      // Inner class definition
      case InnerClassDef(name, args*) =>
        val InnerClassData = scopeMap("InnerMembers").asInstanceOf[mutable.Map[String,Any]]
        val constructorArraySeq: (List[String],mutable.ArraySeq[Operator])= Tuple2(List("Default"), mutable.ArraySeq(Value("Default")))
        val newMap:  mutable.Map[String, Any] = mutable.Map("PublicField" -> scopeMap("PublicField").asInstanceOf[mutable.Map[String,Any]].clone(),
          "PrivateField" -> scopeMap("PrivateField").asInstanceOf[mutable.Map[String,Any]].clone(),
          "PublicMethod" ->scopeMap("PublicMethod").asInstanceOf[mutable.Map[String,Any]].clone(),
          "PrivateMethod" ->scopeMap("PrivateMethod").asInstanceOf[mutable.Map[String,Any]].clone(),
          "Constructor" -> constructorArraySeq, "InnerMembers" ->mutable.Map(), "AbstractMethod" -> mutable.Set[String](), "DefType" -> "Concrete",
          "Chain" -> mutable.ListBuffer[String](), "Name" -> name )
        InnerClassData.put(name,newMap)
        args.map( i =>compute(i,newMap))

      // Constructor definition
      case Constructor(parameterList,exps*) =>
        if (scopeMap.contains("Constructor")) {
          scopeMap.put("Constructor", Tuple2(parameterList, mutable.ArraySeq(exps: _*)))
        }
        else{
          throw DSLException("Constructor is not allowed in an Interface.")
        }

      // Field declaration definition
      case Field(access,name,value) =>
        val data = value.match{
          case None => None
          case _ => compute(value.asInstanceOf[Operator])
        }
        if(scopeMap("DefType") == "Interface"){
          if(access != "Public"){
            throw DSLException("Only public fields are allowed in an interface.")
          }
        }
        access match{
          case "Public" | "Protected" =>
            scopeMap("PublicField").asInstanceOf[mutable.Map[String, Any]].put(name, data)
          case "Private" =>
          case _ => throw DSLException("Invalid Access Modifier")
        }
        scopeMap("PrivateField").asInstanceOf[mutable.Map[String, Any]].put(name, data)

      // Method declaration
      case Method(access,name,parameterList, exps*) =>
        access match{
          case "Public"| "Protected" =>
            scopeMap("PublicMethod").asInstanceOf[mutable.Map[String, Any]].put(name, Tuple2(parameterList, mutable.ArraySeq(exps: _*)))
          case "Private" =>
          case _ => throw DSLException("Invalid Access Modifier")
        }
        scopeMap("PrivateMethod").asInstanceOf[mutable.Map[String, Any]].put(name, Tuple2(parameterList, mutable.ArraySeq(exps: _*)))

       // Abstract Method
      case AbstractMethod(name) =>
        val temp = scopeMap("AbstractMethod").asInstanceOf[mutable.Set[String]]
        temp += name // Add the new abstract class name to existing Set
        scopeMap("AbstractMethod") = temp

      // Object creation/ Class Instantiation
      case NewObject(name, exp,parameterValues,parentObject) =>
        val currMap = parentObject match{
          case "None" => classMap // Outer class object
          case _ => // Inner class object with outer class object reference
            val objectData = scopeMap(parentObject).asInstanceOf[mutable.Map[String,Any]]
            val innerClassData = objectData("InnerMembers").asInstanceOf[mutable.Map[String,Any]]
            innerClassData
        }
        // Retrieve class definition data
        val classData = currMap(name).asInstanceOf[mutable.Map[String,Any]]

        // Check if concrete class else do not allow
        if (classData("DefType") != "Concrete") throw DSLException("This Abstract Class/Interface cannot be instantiated.")

        val variableName= exp match {
          case Variable(input,shouldOverride) =>input
          case _ => throw DSLException("Invalid first attribute.")
        }
        val newMap:  mutable.Map[String, Any] = mutable.Map()
        // Enter fields
        newMap.put("Field",classData("PrivateField").asInstanceOf[mutable.Map[String,Any]].clone())
        // Inner Class
        newMap.put("InnerMembers",classData("InnerMembers").asInstanceOf[mutable.Map[String,Any]].clone())
        // Run constructor
        val constructorParameters = classData("Constructor").asInstanceOf[(List[String],mutable.ArraySeq[Operator])]._1
        val constructorExps = classData("Constructor").asInstanceOf[(List[String],mutable.ArraySeq[Operator])]._2
        val localMap=setParameters(constructorParameters,parameterValues,scopeMap)
        localMap.put("parentReference",newMap("Field").asInstanceOf[mutable.Map[String,Any]])
        runLines(localMap,constructorExps)
        // Store methods
        newMap.put("Method",classData("PrivateMethod").asInstanceOf[mutable.Map[String,Any]])
        scopeMap.put(variableName,newMap)

      // Invoke method of an object
      case InvokeMethod(objectName, methodName, parameterValues) =>
        // Retrieve object data
        val objectData = scopeMap(objectName).asInstanceOf[mutable.Map[String,Any]]
        val instanceVariables = objectData("Field").asInstanceOf[mutable.Map[String,Any]]
        val methodsData = objectData("Method").asInstanceOf[mutable.Map[String,Any]]
        val methodParameters = methodsData(methodName).asInstanceOf[(List[String],mutable.ArraySeq[Operator])]._1
        val methodExps = methodsData(methodName).asInstanceOf[(List[String],mutable.ArraySeq[Operator])]._2
        // Create new local variable map and point a reference to class instance variable map
        val newMap=setParameters(methodParameters,parameterValues,scopeMap)
        newMap.put("parentReference",instanceVariables)
        runLines(newMap,methodExps)

      // To implement inheritance
      case Extends(className) =>
        // Retrieve class definition data
        val classData = classMap(className).asInstanceOf[mutable.Map[String,Any]]

        // Extends should work only on Classes and Abstract classes
        if (classData("DefType") == "Interface"){
          if (scopeMap("DefType") != "Interface"){
            throw DSLException("The Class/ Abstract Class cannot Extend an Interface.")
          }
        } else{
          if (scopeMap("DefType") == "Interface") {
            throw DSLException("An interface cannot extend anything other than another interface.")
          }
        }

        // Interface does not need these operations
        if (classData("DefType") != "Interface") {
          val methodsData = classData("PublicMethod").asInstanceOf[mutable.Map[String, Any]]
          val constructorData = classData("Constructor").asInstanceOf[(List[String], mutable.ArraySeq[Operator])]
          val constructorLines = constructorData._2
          val newConstructorData = scopeMap("Constructor").asInstanceOf[(List[String], mutable.ArraySeq[Operator])]
          val newConstructorLines = newConstructorData._2

          scopeMap("PublicMethod") = methodsData.++(scopeMap("PublicMethod").asInstanceOf[mutable.Map[String, Any]])
          scopeMap("PrivateMethod") = methodsData.++(scopeMap("PrivateMethod").asInstanceOf[mutable.Map[String, Any]])
          scopeMap.put("Constructor", Tuple2(newConstructorData._1, constructorLines.++(newConstructorLines)))
        }
        // Common field inheritance
        val fieldData = classData("PublicField").asInstanceOf[mutable.Map[String,Any]]

        scopeMap("PublicField") = fieldData.++(scopeMap("PublicField").asInstanceOf[mutable.Map[String,Any]])
        scopeMap("PrivateField") = fieldData.++(scopeMap("PrivateField").asInstanceOf[mutable.Map[String,Any]])

        // Keep track of extends/implements chain
        if (scopeMap("Chain").asInstanceOf[mutable.ListBuffer[String]].lastOption.getOrElse(None) == scopeMap("Name")){
          throw DSLException("Only single inheritance is allowed.")
        }
        if(classData("Chain").asInstanceOf[mutable.ListBuffer[String]].contains(scopeMap("Name").asInstanceOf[String])){
          throw DSLException("Circular inheritance is found.")
        }

        if (classData("Chain").asInstanceOf[mutable.ListBuffer[String]].isEmpty ){
          val newChain = new mutable.ListBuffer[String]()
          newChain += className
          newChain += scopeMap("Name").asInstanceOf[String]
          scopeMap("Chain") = newChain
        }
        else {
            val oldChain = classData("Chain").asInstanceOf[mutable.ListBuffer[String]].clone()
            oldChain += scopeMap("Name").asInstanceOf[String]
            scopeMap("Chain") = oldChain
          }

        // Abstract classes logic to check if they have been implemented
        val abstractMethodsData = classData("AbstractMethod").asInstanceOf[mutable.Set[String]].clone()
        abstractMethodsData ++= scopeMap("AbstractMethod").asInstanceOf[mutable.Set[String]]
        abstractMethodsData --= scopeMap("PrivateMethod").asInstanceOf[mutable.Map[String,Any]].keys
        scopeMap("AbstractMethod") = abstractMethodsData

      // To implement inheritance
      case Implements(interfaceName) =>
        // Retrieve class definition data
        val InterfaceData = classMap(interfaceName).asInstanceOf[mutable.Map[String,Any]]

        // Extends should work only on Classes and Abstract classes
        if (InterfaceData("DefType") != "Interface"){
          throw DSLException("Cannot Implement Class/ Abstract Class")
        }
        if (scopeMap("DefType") == "Interface"){
          throw DSLException("An interface cannot implement another interface.")
        }

        val fieldData = InterfaceData("PublicField").asInstanceOf[mutable.Map[String,Any]]
        scopeMap("PublicField") = fieldData.++(scopeMap("PublicField").asInstanceOf[mutable.Map[String,Any]])
        scopeMap("PrivateField") = fieldData.++(scopeMap("PrivateField").asInstanceOf[mutable.Map[String,Any]])

        // Keep track of extends/implements chain

        if (scopeMap("Chain").asInstanceOf[mutable.ListBuffer[String]].lastOption.getOrElse(None) == scopeMap("Name")){
          throw DSLException("Only single inheritance is allowed.")
        }
        if(InterfaceData("Chain").asInstanceOf[mutable.ListBuffer[String]].contains(scopeMap("Name").asInstanceOf[String])){
          throw DSLException("Circular inheritance is found.")
        }

        if (InterfaceData("Chain").asInstanceOf[mutable.ListBuffer[String]].isEmpty ){
          val newChain = new mutable.ListBuffer[String]()
          newChain += interfaceName
          newChain += scopeMap("Name").asInstanceOf[String]
          scopeMap("Chain") = newChain
        }
        else {
          val oldChain = InterfaceData("Chain").asInstanceOf[mutable.ListBuffer[String]].clone()
          oldChain += scopeMap("Name").asInstanceOf[String]
          scopeMap("Chain") = oldChain
        }

        // Abstract classes logic to check if they have been implemented
        val abstractMethodsData = InterfaceData("AbstractMethod").asInstanceOf[mutable.Set[String]].clone()
        abstractMethodsData ++= scopeMap("AbstractMethod").asInstanceOf[mutable.Set[String]]
        abstractMethodsData --= scopeMap("PrivateMethod").asInstanceOf[mutable.Map[String,Any]].keys
        scopeMap("AbstractMethod") = abstractMethodsData

      // Retrieve instance variable after object creation
      case GetObjectField(objName, instanceVar) =>
        val objectData = scopeMap(objName).asInstanceOf[mutable.Map[String,Any]]
        val instanceVariables = objectData("Field").asInstanceOf[mutable.Map[String,Any]]
        if (instanceVariables.contains(instanceVar)) {
          val answer = instanceVariables.get(instanceVar)
          answer match{
            case Some(s: _) => s
            case _ =>
          }
        } else throw DSLException("Instance variable not found")

      //------------------------------------------------------------------------------
      // HW-1 Set Operations

      // Return the raw value
      case Value(input) => input

      // Return the value corresponding to a variable name if declared else return null
      case Variable(name,shouldOverride) =>
        // Look for variable in the current scope and return the value
        // else look in the outer scope( if there exists one)
        if (scopeMap.contains(name)){
          scopeMap(name)
        }  else {
          if(scopeMap.contains("parentReference")){
            compute(Variable(name),scopeMap("parentReference").asInstanceOf[mutable.Map[String,Any]])
          }else{
            // Return None if variable is not found
            "N/A"
          }
        }

      // Return a wrapper object containing elements to be inserted
      case Insert(args*) =>
        val tempSet = validateInputs(scopeMap, args *)
        val mutList = new mutable.ListBuffer[Operator]()
        args.foreach{i =>
          if (tempSet.contains(i)){
            tempSet -= i
            mutList += i
          }
        }

        if(mutList.isEmpty){
          operationWrapper("Insert", Value(tempSet.toSet))
        }
        else{
          if(mutList.length == 1){
            operationWrapper("Insert", Insert(Value(tempSet.toSet),mutList.head))
          }
          else{
            operationWrapper("Insert", Insert(Value(tempSet.toSet),mutList.head,mutList(1)))
          }
        }


      // Return a wrapper object containing elements to be deleted
      case Delete(args*) =>
        val tempSet = validateInputs(scopeMap, args *)
        val mutList = new mutable.ListBuffer[Operator]()
        args.foreach{i =>
          if (tempSet.contains(i)){
            tempSet -= i
            mutList += i
          }
        }
        if(mutList.isEmpty){
          operationWrapper("Delete", Value(tempSet.toSet))
        }
        else{
          if(mutList.length == 1){
            operationWrapper("Delete", Delete(Value(tempSet.toSet),mutList.head))
          }
          else{
            operationWrapper("Delete", Delete(Value(tempSet.toSet),mutList.head,mutList(1)))
          }
        }

      // Perform Set Union of 2 inputs sets
      case Union(input1,input2) =>
        val i1 = validateSetInput(input1,scopeMap)
        val i2 = validateSetInput(input2,scopeMap)

        if(i1.equals(Set("N/A")) & !i2.equals(Set("N/A"))){
          Union(input1,Value(i2))
        }else if(!i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Union(Value(i1),input2)
        }else if(i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Union(input1,input2)
        }
        else{
          i1.union(i2).to(mutable.Set)
        }


      // Perform Set Difference of 2 inputs sets
      case Difference(input1,input2) =>
        val i1 = validateSetInput(input1,scopeMap)
        val i2 = validateSetInput(input2,scopeMap)

        if(i1.equals(Set("N/A")) & !i2.equals(Set("N/A"))){
          Difference(input1,Value(i2))
        }else if(!i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Difference(Value(i1),input2)
        }else if(i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Difference(input1,input2)
        }
        else{
          i1.diff(i2).to(mutable.Set)
        }


      // Perform Set Intersection of 2 inputs sets
      case Intersection(input1,input2) =>
        val i1 = validateSetInput(input1,scopeMap)
        val i2 = validateSetInput(input2,scopeMap)

        if(i1.equals(Set("N/A")) & !i2.equals(Set("N/A"))){
          Intersection(input1,Value(i2))
        }else if(!i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Intersection(Value(i1),input2)
        }else if(i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Intersection(input1,input2)
        }
        else{
          i1.intersect(i2).to(mutable.Set)
        }


      // Perform Set symmetric difference of 2 inputs sets
      case SymDiff(input1,input2) =>
        val i1 = validateSetInput(input1,scopeMap)
        val i2 = validateSetInput(input2,scopeMap)

        if(i1.equals(Set("N/A")) & !i2.equals(Set("N/A"))){
          SymDiff(input1,Value(i2))
        }else if(!i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          SymDiff(Value(i1),input2)
        }else if(i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          SymDiff(input1,input2)
        }
        else{
          i1.union(i2).diff(i1.intersect(i2)).to(mutable.Set)
        }


      // Perform Set product of 2 inputs sets
      case Product(input1,input2) =>
        val i1 = validateSetInput(input1,scopeMap)
        val i2 = validateSetInput(input2,scopeMap)

        if(i1.equals(Set("N/A")) & !i2.equals(Set("N/A"))){
          Product(input1,Value(i2))
        }else if(!i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Product(Value(i1),input2)
        }else if(i1.equals(Set("N/A")) & i2.equals(Set("N/A"))){
          Product(input1,input2)
        }
        else{
          i1.flatMap(x => i2.map(y => (x,y))).to(mutable.Set)
        }


      // Check if a set contains an element
      case Check(name,value) =>
        val currentScopeLevel = findScopeLevel(name,scopeMap)
        if (currentScopeLevel.contains(name)){
          validateSetInput(Variable(name),currentScopeLevel).contains(compute(value,currentScopeLevel))
        }
        else{
          throw DSLException("Set not found")
        }

      //  Implement named and anonymous scope
      case Scope(name:String,value:Operator)=>
        val scopeName:mutable.Map[String,Any] = scopeMap.get(name) match {
          case Some(m)  => m.asInstanceOf[mutable.Map[String,Any]]
          case None =>
            val newMap:  mutable.Map[String,Any] =
              mutable.Map("parentReference" -> scopeMap)
            if (name != "") scopeMap.put(name, newMap)
            newMap
        }
        compute(value,scopeName)

      // Macro functionality
      case Macro(name, value)=>
        if (compute(value) == "ND"){
          if (macrosMap.contains(name)){
            compute(macrosMap(name),scopeMap)
          }
          else{
            throw DSLException("Macro does not exist")
          }

        }
        else{
          macrosMap.put(name,value)
          macrosMap(name)
        }
      // Handle Insert, Delete and simple value assignment operations
      case Assign(name,value) =>
        val variableName= name match {
          case Variable(input,shouldOverride) =>input
          case _ => throw DSLException("Invalid first attribute.")
        }
        val shouldOverrideData = name match{
          case Variable(input,shouldOverride) =>shouldOverride
          case _ => throw DSLException("Invalid data")
        }
        val v=compute(value,scopeMap)
        val n = compute(name,scopeMap)

        v match {
          case obj: operationWrapper =>

            obj.operationType match {
              case "Insert" =>
                obj.set match{
                  case setData:Value =>
                    n match {
                      // If a set already exists, the new elements are added to it
                      case existingSet : mutable.Set[Any] =>
                        existingSet ++= compute(setData).asInstanceOf[Set[Any]]
                        existingSet
                      // If there is no set, it is created or it overrides any existing
                      // data mapped to the given name
                      case anythingElse:Any =>
                        //val valSet = mutable.Set[Any](set.toArray:_*)
                        val currentScopeLevel = if (shouldOverrideData) {
                          scopeMap}
                        else {
                          findScopeLevel(variableName,scopeMap)
                        }
                        //val currentScopeLevel = findScopeLevel(variableName,scopeMap)
                        currentScopeLevel.put(variableName,compute(setData).asInstanceOf[Set[Any]].to(mutable.Set))
                        currentScopeLevel(variableName)
                    }
                  case _ =>
                    Assign(name, obj.set.asInstanceOf[Insert])
                }

              case "Delete" =>
                obj.set match{
                  case setData:Value =>
                    n match {
                      // If a set already exists, the elements are deleted from it
                      case existingSet : mutable.Set[Any] =>
                        existingSet --= compute(setData).asInstanceOf[Set[Any]]
                        existingSet
                      // If the set does not exist, show error message
                      case _:Any =>
                        throw DSLException("Set not found")
                    }
                  case _ =>
                    Assign(name, obj.set.asInstanceOf[Delete])
                }


            }
          case _=>
                // n == "N/A"
                n match{
                  case "N/A" =>
                    scopeMap.put(variableName, v)
                    scopeMap(variableName)
                  case _ =>
                    val currentScopeLevel = if (shouldOverrideData) {
                      scopeMap}
                    else {
                      findScopeLevel(variableName,scopeMap)
                    }
                    currentScopeLevel.put(variableName, v)
                    currentScopeLevel(variableName)
                }

        }
    }
    } else{
      op match {
        case Catch(name, exps*) =>
          // Store the thrown exception object. Reset exceptionDataMap - set flag back to false.
          scopeMap.put(name,exceptionDataMap("exceptionObjectData").asInstanceOf[mutable.Map[String,Any]])
          exceptionDataMap.put("exceptionOccurred",false)
          exceptionDataMap.put("exceptionObjectData",tempExceptionMap)

          exps.map( i =>compute(i,scopeMap))

        case _ =>

      }
    }
  }
  // Helper function to run the expression in methods and constructor
  def runLines(scopeMap: mutable.Map[String, Any], eqs: mutable.ArraySeq[Operator]): Any ={
    eqs.zipWithIndex.foreach {
      case(i, count) => if ((count+1) < eqs.length ){
        compute(i, scopeMap)
      }
    }
    // Return the result from last statement
    compute(eqs.last,scopeMap)
  }

  // Logic to handle mapping parameters to attributes
  def setParameters(methodParameters: List[String],parameterValues: List[Operator],scopeMap:mutable.Map[String,Any]): mutable.Map[String,Any] ={
    val methodTempMap: mutable.Map[String,Any] = mutable.Map()
    if (methodParameters.size != parameterValues.size) throw DSLException("Parameter and Attribute list sizes do not match.")
    (methodParameters zip parameterValues).map{ case (m, a) => methodTempMap.put(m,compute(a,scopeMap))}
    methodTempMap
  }

  // Helper function to handle new class creation
  def classCreationHelper(name:String,defType:String,scopeMap:mutable.Map[String,Any],args: Operator*): Unit= {
      // New class/Interface/Abstract Class template
      // If Interface is being defined Public and Private methods will not be used
      val newMap: mutable.Map[String, Any] = mutable.Map("PublicField" -> mutable.Map(),
        "PrivateField" -> mutable.Map(), "PublicMethod" -> mutable.Map(), "PrivateMethod" -> mutable.Map(),
        "InnerMembers" -> mutable.Map(), "AbstractMethod" -> mutable.Set[String](), "DefType" -> defType,
        "Chain" -> mutable.ListBuffer[String](), "Name" -> name)

      defType match {
        case "Concrete" | "AbstractClass" =>
          // Constructor definition for concrete class and abstract class
          val constructorArraySeq: (List[String], mutable.ArraySeq[Operator]) = Tuple2(List("Default"), mutable.ArraySeq(Value("Default")))
          newMap("Constructor") = constructorArraySeq
        case _ =>
      }
      // Insert newly created map template into the scope

      scopeMap.put(name, newMap)
      try {
        // Run all exps
        args.map(i => compute(i, newMap))
      }
      catch {
        case ex: Exception =>
          // Roll back new class/Interface/Abstract Class creation as error was encountered
          scopeMap.remove(name)
          throw DSLException(ex.getMessage)
      }

      // Checks after evaluating all exps
      defType match {
        case "Concrete" =>
          if (newMap("AbstractMethod").asInstanceOf[mutable.Set[String]].nonEmpty) {
            scopeMap.remove(name)
            throw DSLException("There must be no un implemented abstract methods in concrete class.")
          }
        case "Interface" =>
          if (newMap("PublicMethod").asInstanceOf[mutable.Map[String, Any]].nonEmpty || newMap("PrivateMethod").asInstanceOf[mutable.Map[String, Any]].nonEmpty) {
            scopeMap.remove(name)
            throw DSLException("There must be no concrete method in an interface.")
          }
        case "AbstractClass" =>
          if (newMap("AbstractMethod").asInstanceOf[mutable.Set[String]].isEmpty) {
            scopeMap.remove(name)
            throw DSLException("There must be at least 1 abstract method in abstract class.")
          }
        case "Exception" =>
          // WRITE LOGIC
        case _ =>
      }
  }

  // Logic to find the scope at which a variable is present. Returns main binding map by default.
  @tailrec
  def findScopeLevel(input:String, scopeMap:mutable.Map[String,Any]): mutable.Map[String,Any] ={
    if (scopeMap.contains(input)){
      scopeMap
    }else{
      if (scopeMap.contains("parentReference")){
        findScopeLevel(input,scopeMap("parentReference").asInstanceOf[mutable.Map[String,Any]])
      }
      else{
        mainBindingMap
      }
    }
  }

  // Logic to verify if set is being passed into set operations.
  def validateSetInput(input: Operator,scopeMap:mutable.Map[String,Any]): Set[Any] ={
    compute(input,scopeMap) match {
      case immutableSet : Set[Any] => immutableSet
      case mutableSet : mutable.Set[Any] => mutableSet.toSet
      case "N/A" => Set("N/A")
      case _ =>
        throw DSLException("Input in Set operations has to be a Set.")
    }
  }

  // Parse though multiple sets/values and combines them into a single set for insertion or deletion
  def validateInputs(scopeMap:mutable.Map[String,Any],args:Operator*) : mutable.Set[Any] = {
    val mutList = mutable.Set.empty[Any]
    args.foreach{i =>
      val g = compute(i,scopeMap)

      if(g.isInstanceOf[Set[Any]]){
        mutList ++= g.asInstanceOf[Set[Any]]
      }
      else if (g.isInstanceOf[mutable.Set[Any]])
        {
          mutList ++= g.asInstanceOf[mutable.Set[Any]]
        }
      else{
        if (g != "N/A") {mutList += g} else {mutList += i}
      }

      // if (g != "N/A") {mutList += g} else {throw DSLException("Variable(s) does not exist")}
    }
    mutList
  }

  // Main method
  @main def runCode(): Unit = {
  // Usage defined in test class
    compute(Assign(Variable("set5"), Insert(Value("2"), Value(3), Value(89))))
    compute(Assign(Variable("set6"), Insert(Value("a"), Value(89))))
    val actual = compute(Union(Variable("set5s"),Variable("set7")))
    compute(Assign(Variable("set7"), Insert(Value("a"), Value(89))))
    val op = compute(PartialEval(actual))
    compute(Assign(Variable("set5s"), Insert(Value("aq"), Value(82329))))
    val op1 = compute(PartialEval(op))
    println(actual)
    println(op1)

//    val op2 = compute(Assign(Variable("set222"), Insert(Variable("set72"), Value(3),Value(12),Variable("set72e"))))
//    compute(Assign(Variable("set72"), Value("a")))
//    val op3 = compute(PartialEval(op2))
//    println(op2)
//    println(op3)
//    compute(Assign(Variable("set72e"), Value("j")))
//    val op4 = compute(PartialEval(op2))
//    println(op4)
//
//    compute(Assign(Variable("set4"), Insert(Value("2"), Value(3))))
//
//    val actual = compute(Assign(Variable("set4"), Delete(Variable("set11"))))
//    compute(Assign(Variable("set11"), Value("2")))
//    val actual1 = compute(PartialEval(actual))
//    print(actual)
//    print(actual1)
  }
}
