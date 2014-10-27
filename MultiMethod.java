/**
 * @author bill 
 * Implementation of MultiMethod in Java
 */
package multimethod;

import java.util.HashMap;
import java.util.Map;

/** 
 * Simple hierarchy to use for test, loosely based on the game Stone Scissors and Paper
 */
class Shape {}
class Stone extends Shape {}
class Scissors extends Shape {}
class Paper extends Shape {}
class GrindStone extends Stone {}

/**
 * Everything lives in MultiMethod for now.  
 * The setup code with both generic *and* Class instance is rather ugly.
 */
class MultiMethod {
    /**
     * main is a test that sets up a MultiMethod then calls it.
     */
    public static void main(String[] args) {        
        // Instantiate some test objects
    	Shape stone = new Stone();
	Shape scissors = new Scissors();
	Shape paper = new Paper();
	Shape grindstone = new GrindStone();
        
        // Instantiate a MultiMethod to compare the test shapes
        MultiMethod compare = new MultiMethod();
        
        // Overload the compare methods.  Any ideas welcome to improve syntax!
        compare.method(new Method<Stone, Stone>(Stone.class, Stone.class){
            public Integer method( Stone shape1, Stone shape2 ){ return 0;} });
        compare.method(new Method<Stone, Scissors>(Stone.class, Scissors.class){
            public Integer method( Stone shape1, Scissors shape2 ){ return +1;} });
        compare.method(new Method<Stone, Paper>(Stone.class, Paper.class){
            public Integer method( Stone shape1, Paper shape2 ){ return -1;} });
        // Test setting up the method explicitly
        Multi mm4 = new Method<Scissors, Stone>(Scissors.class, Stone.class){
            public Integer method( Scissors shape1, Stone shape2 ){ return -1;}};
        // then passing in an instance of it
        compare.method(mm4);
        
        // Call compare polymorphically to check it gets the right results
        System.out.print("stone vs scissors => "      + compare.multi(stone, scissors)+"\n");
        System.out.print("scissor vs scissors => "    + compare.multi(scissors, scissors)+"\n");
        System.out.print("paper vs scissors => "      + compare.multi(paper, scissors)+"\n");
        System.out.print("grindstone vs scissors => " + compare.multi(grindstone, scissors)+"\n");
    }
        
    private Map<Signature, Multi> map = new HashMap<>();
    
    public MultiMethod() {}
    
    public MultiMethod method(Multi mm) {
        //System.out.println("Adding method for signature " + key.toString() );
        Signature key = new Signature( mm.getClass1(), mm.getClass2() );
        map.put(key, mm);
        return this;
    }
    /** 
     * This is the most important function, that loops round all the possible sigs
     * for an inexact match and finds the greatest lower bound.
     * @param arg1  A version with more arguments should be developed
     * @param arg2
     * @return      The return type should be configurable
     */
    public Integer multi(Object arg1, Object arg2) {
        Signature sig = new Signature(arg1.getClass(),arg2.getClass());
	Multi method = map.get(sig);
        Multi itMethod = method;
        Class contender1;
        Class contender2;
        if (method != null) {                // if the exact signature is matched
            return method.multi(arg1, arg2); // then use it
        } else {                             // else loop to find greatest lower bound
            
            boolean contenderIsReal = false;
            Signature contender = new Signature();                   
            for(Signature itSig : map.keySet()) {  // Loop round all the signatures in the methods map
                
                boolean isLowerBound =       // To be a contender must be a lower bound 
                        itSig.cl1.isAssignableFrom(arg1.getClass() ) // ie assignable from all args
                    &&  itSig.cl2.isAssignableFrom(arg2.getClass() ) ; 
                if (!isLowerBound) continue;
                                             // Greater arg in each slot is the thing to beat.
                contender.cl1 = (contender.cl1.isAssignableFrom(itSig.cl1)) 
                                ? itSig.cl1 : contender.cl1;  
                contender.cl2 = (contender.cl2.isAssignableFrom(itSig.cl2)) 
                                ? itSig.cl2 : contender.cl2;
                if ( contender.equals(itSig) )
                    contenderIsReal = true;   // the greatest contender exist as sig
            } 
                            
            if ( contenderIsReal ) {
                Multi greatest = map.get( contender );
                map.put(sig, greatest);          // store the best we found for this sig
                return greatest.multi( arg1, arg2 );
            }
            return -666;                        // Would throw in a real implementation
            
        } // end of inexact match
    } // end of all important multi() method
}// end of class MultiMethod

/**
 * Helper classes
*/
// First a bass class to act like a function pointer (whisper it softly among Javanauts).
abstract class Multi{
    abstract Integer multi(Object arg1, Object arg2);
    abstract Class getClass1();
    abstract Class getClass2();
}
// Then a refinement of the bass class that is generic by type.
// The client derives her method implementation class from this, overriding method().
// Unfortunately she has to provide a Class instance as well at the moment.  
// Java generics are like that.
abstract class Method<T1, T2> extends Multi {
    Class cl1;
    Class cl2;
    Method(){}
    Method(Class c1, Class c2){cl1 = c1; cl2 = c2;}
    @Override
    Integer multi(Object arg1, Object arg2) {return method((T1)arg1, (T2)arg2);}
    public abstract Integer method( T1 shape1, T2 shape2 );   
    public Class getClass1() { return cl1;}   
    public Class getClass2() { return cl2; }
}
// Also a signature to be the key type for the method map
class Signature {
    public Class cl1;
    public Class cl2;
    Signature(){cl1 = Object.class; cl2= Object.class;}
    Signature(Class c1, Class c2){cl1 = c1; cl2 = c2;}
    @Override public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Signature))
            return false;
        Signature that = (Signature)o;
        return cl1 == that.cl1 && cl2 == that.cl2;
    }
    @Override public int hashCode() {return 42;} // must do better
    @Override public String toString() {
        return String.format("( %s, %s )",cl1.toString(), cl2.toString());
    }
}

