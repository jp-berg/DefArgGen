package berg.jp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Runner {

    public static void main(String[] args) throws Exception {
        var params = new DawCli(args);
        System.out.println(params.getVisibility());
        System.out.println(params.getOutput());
        System.out.println(params.getReturnType());
        System.out.println(params.getName());

    }
}
