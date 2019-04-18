import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        Declarations d = declarations();
        Block body = statements();
        match(TokenType.RightBrace);
        
        //Block body = new Block();
        Program prog = new Program(d, body);

        return prog;  // student exercise
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations dec = new Declarations();
        while (isType()) {
            declaration(dec);
        }

        return dec;  // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        // Match type
        String var;

        Type typeT = type();

        // Loop on identifier
        var = match(TokenType.Identifier);
        while (token.type() != TokenType.Semicolon) {
            match(TokenType.Comma);
            String vs = match(TokenType.Identifier);
            Variable vV = new Variable(vs);
            Declaration decT = new Declaration(vV, typeT);
            ds.add(decT);
        }
        match(TokenType.Semicolon);
        Variable varV = new Variable(var);
        Declaration dec = new Declaration(varV,typeT);
        ds.add(dec);
        // Match identifier
        // If no semicolon repeat loop 
        // Match semicolon
        // Add declaration to arraylist
        // student exercise
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char 
        Type t = null;
            switch(token.type()) {
            
            case Char:
                match(TokenType.Char);
                t = Type.CHAR;
                break;
            case Bool:
                match(TokenType.Bool);
                t = Type.BOOL;
                break;
            case Int:
                match(TokenType.Int);
                t = Type.INT;
                break;
            case Float:
                match(TokenType.Float);
                t = Type.FLOAT;
                break;
        }
        return t;          
    }


    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        
       Statement s = null;
        // Check case for it being assign and then return that
        // TODO: case statement that checks if its an Block, Assignment IfStatement, WhleStatement, or Semicolon
        switch(token.type()) {
            case Identifier:
                s =  assignment();
                return s;
            case If:
                s = ifStatement();
                return s;
            case LeftBrace:
                match(TokenType.LeftBrace);
                s = statements();
                match(TokenType.RightBrace);
                return s;
            case While:
                s = whileStatement();
                return s;
            case Semicolon:
                s = skip();
                return s;
        }
        return skip();
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        // While loop that adds a statement to block ArrayList at each step
        while (token.type() != TokenType.RightBrace && token.type() != TokenType.Eof) {
            b.members.add(statement());
        }
        
        return b;
    }
  
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        
        String ident = match(TokenType.Identifier);

        Variable var = new Variable(ident);

        
       
        match(TokenType.Assign);
        Expression express = expression();
        Assignment assign = new Assignment(var, express);
        match(TokenType.Semicolon);

        return assign;  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Statement thenBranch = null;
        Statement elseBranch = null;
        match(TokenType.If);

        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        
        thenBranch = statement();
        if (token.type() == TokenType.Else) {
            match(TokenType.Else);
            elseBranch = statement();
            return new Conditional(e, thenBranch, elseBranch);  
        } else {
            elseBranch = skip();
        }


        return new Conditional(e, thenBranch, elseBranch);  
    }
  
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        Statement s = statement();

        return new Loop(e, s);  
    }

    private Statement skip() {return new Skip();}

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression e = conjunction();
        while (token.type() == TokenType.Or) {
            Operator op = new Operator(match(TokenType.Or));
            Expression conj = conjunction();
            e = new Binary(op, e, conj);
        }
        return e;  
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
       Expression e = equality();
        while (token.type() == TokenType.And) {
            Operator op = new Operator(match(TokenType.And));
            Expression eq2 = equality();
            e = new Binary(op, e, eq2);
        }
        return e;  
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression e = relation();
        while (isEqualityOp()) {
            Operator op = new Operator(match(token.type()));
            Expression rel = relation();
            e = new Binary(op, e, rel);
        }
        return e;  
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition] 
        Expression e = addition();
        while (isRelationalOp()) {
            Operator op = new Operator(match(token.type()));
            Expression add = addition();
            e = new Binary(op, e, add);
        }
        return e;  
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen); 
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        int integer = 0;
        Value value = null;
        if (token.type().equals(TokenType.IntLiteral)) {
            integer = Integer.parseInt(token.value());
            value = new IntValue(integer);
            token = lexer.next();
        } if (token.type().equals(TokenType.True)) {
            value = new BoolValue(true);
            token = lexer.next();
        } if (token.type().equals(TokenType.False)) {
            value = new BoolValue(false);
            token = lexer.next();
        } if (token.type().equals(TokenType.FloatLiteral)) {
            float floatNum = Float.parseFloat(token.value());
            value = new FloatValue(floatNum);
            token = lexer.next();
        } if (token.type().equals(TokenType.CharLiteral)) {
            char c = token.value().charAt(0);
            value = new CharValue(c);
            token = lexer.next();
        } else if (value == null) {
            throw new RuntimeException(); 
        }
        
        return value;
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
