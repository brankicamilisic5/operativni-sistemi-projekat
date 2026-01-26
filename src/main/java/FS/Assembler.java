package FS;


import java.util.ArrayList;
import java.util.List;

public class Assembler {

    public List<Integer> translate(File file){

        List<Integer> code = new ArrayList<>();

        String text = file.read();
        String[] lines = text.split("\n");

        for(String line : lines){

            line = line.trim();

            if(line.equals("")) continue;

            String[] parts = line.split(" ");

            String instrukcija = parts[0].toUpperCase();
            int operand = 0;

            if(parts.length > 1)
                operand = Integer.parseInt(parts[1]);


            switch(instrukcija){

                case "LOAD":
                    code.add(1);
                    code.add(operand);
                    break;

                case "STORE":
                    code.add(2);
                    code.add(operand);
                    break;

                case "ADD":
                    code.add(3);
                    code.add(operand);
                    break;

                case "HALT":
                    code.add(0);
                    code.add(0);
                    break;
            }
        }

        return code;
    }
}
