// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {

    public static TypeMap typing (Declarations d) {
        TypeMap map = new TypeMap();
        for (Declaration di : d) 
            
            map.put (di.v, di.t);
        return map;
    }

    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        System.exit(1);
    }

    public static void V (Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
            }
    } 

    public static void V (Program p) {
        V (p.decpart);
        V (p.body, typing (p.decpart));
    } 

    public static Type typeOf (Expression e, TypeMap tm) {
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v)
                   , "undeclared variable: " + v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            if (b.op.ArithmeticOp( ))  
                check( 
                       (typ1 == Type.INT || typ1 == Type.FLOAT)
                       , "type error for " + b.op.val);
            else if (b.op.RelationalOp( )) 
                check( typ1 == typ2 , "type error for " + b.op.val);
            else if (b.op.BooleanOp( )) 
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                       b.op.val + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        } 
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Type type = typeOf(u.term, tm);
            V(u.term, tm);
            if (u.op.NotOp()) {
                check(type == Type.BOOL, "type error for " + u.op.val);
                return;
            } else if (u.op.NegateOp()) {
                check(type == Type.INT || type == Type.FLOAT, u.op.val + ": Non numeric operand");
                return;
            } else if (u.op.intOp()) {
                check(type == Type.FLOAT || type == Type.CHAR, u.op.val + ": Non Float operand");
                return;
            } else if (u.op.floatOp()) {
                check(type == Type.INT, u.op.val + ": Non Int operand");
                return;
            } else if (u.op.charOp()) {
                check(type == Type.INT, u.op.val + ": Improper conversion to char - Are you trying to convert an int?");
                return;
            }
        }
        // student exercise
         throw new IllegalArgumentException("should never reach here");
    }

    public static void V (Statement s, TypeMap tm) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target)
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = typeOf(a.source, tm);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                           , "mixed mode assignment to " + a.target);
                else
                    check( false
                           , "mixed mode assignment to " + a.target);
            }
            return;
        } if (s instanceof Block) {
            Block b = (Block) s;
            for (Statement st : b.members) {
                V (st, tm);
            }
            return;
        }
        if (s instanceof Conditional) {
            Conditional c = (Conditional) s;
            Type typeTest = typeOf(c.test, tm);
            if (typeTest != Type.BOOL) {
                check(false, "Test expression is not of bool type: " + c.test);
            }
            V(c.thenbranch, tm);
            V(c.elsebranch, tm);
            return;
        } 
        if (s instanceof Loop) {
            Loop l = (Loop) s;
            Type tTest = typeOf(l.test, tm);
            if (tTest != Type.BOOL) {
                check(false, "Loop test is not of type bool: " + l.test);
            }
            V(l.body,tm);
            return;
        }
        // student exercise
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();          
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        System.out.println(map.toString());   
        V(prog);
    } //main

} // class StaticTypeCheck

