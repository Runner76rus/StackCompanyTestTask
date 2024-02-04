import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            Path source = Paths.get("/home/runner76rus/test/ç¥ª¨.txt");
            Path target = Paths.get("/home/runner76rus/test/");
            TestStack.run(source,target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}