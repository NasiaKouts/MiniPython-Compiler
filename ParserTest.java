import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.*;

public class ParserTest
{
    public static void main(String[] args)
    {
        try
        {
            Parser parser =
                    new Parser(
                    new Lexer(
                    new PushbackReader(
                    new FileReader(args[0].toString()), 1024)));

            RootSymbolTable symbolTable =  new RootSymbolTable();
            Start ast = parser.parse();

            FirstVisitor first = new FirstVisitor(symbolTable);
            ast.apply(first);

            SecondVisitor second = new SecondVisitor(symbolTable, first.getErrors());
            ast.apply(second);

            System.out.println("Compilation finished with " + second.getErrors() + " errors.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

