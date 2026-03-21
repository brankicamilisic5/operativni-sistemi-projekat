package FS;


import java.util.ArrayList;
import java.util.List;

public class Assembler {

    public List<Integer> translate(File file){

        List<Integer> code = new ArrayList<>();

        String text = file.read();
        if(text == null) return code;

        String[] lines = text.split("\n");

        for(String line : lines){

            line = line.trim();

            if(line.equals("") || line.startsWith("#")) continue;

            String[] parts = line.split("\\s+");

            String instrukcija = parts[0].toUpperCase();
            int operand = 0;

            try {
                if(parts.length > 1)
                    operand = Integer.parseInt(parts[1]);
            } catch(Exception e){
                System.out.println("Greska u liniji: " + line);
            }

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

                case "SYSCALL":
                    code.add(9);
                    break;

                case "HALT":
                    code.add(0);
                    break;

                default:
                    System.out.println("Nepoznata instrukcija: " + instrukcija);
            }
        }

        return code;
    }
}
