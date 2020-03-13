package encryptdecrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) {
        String mode = "enc";
        int key = 0;
        String data = "";
        String inputFileName = "";
        String outputFileName = "";

        boolean dataDefined = false;
        boolean fileInput = false;
        boolean fileOutput = false;

        try {
            for (int i = 0; i < args.length - 1; i += 2) {
                String arg = args[i];
                String param = args[i + 1];
                if (arg.equals("-mode")) {
                    mode = param;
                } else if (arg.equals("-key")) {
                    key = Integer.parseInt(param);
                } else if (arg.equals("-data")) {
                    data = param;
                    dataDefined = true;
                } else if (arg.equals("-in")) {
                    inputFileName = param;
                    fileInput = true;
                } else if (arg.equals("-out")) {
                    outputFileName = param;
                    fileOutput = true;
                } else {
                    throw new IllegalArgumentException();
                }
            }

            if (!dataDefined) {
                if (fileInput) {
                    data = new String(Files.readAllBytes(Paths.get(inputFileName)));
                } else {
                    throw new IllegalArgumentException();
                }
            }

            if (mode.equals("enc")) {
                data = encrypt(data, key);
            } else if (mode.equals("dec")) {
                data = decrypt(data, key);
            }

            if (fileOutput) {
                Path path = Paths.get(outputFileName);
                Files.write(
                        path,
                        data.getBytes(),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } else {
                System.out.println(data);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: invalid parameters!");
        } catch (IOException e) {
            System.out.println("Error: can't read/write file!");
        }
    }

    private static String decrypt(String data, int key) {
        StringBuilder builder = new StringBuilder();
        for (char c : data.toCharArray()) {
            builder.append((char)(c - key));
        }
        return builder.toString();
    }

    private static String encrypt(String data, int key) {
        StringBuilder builder = new StringBuilder();
        for (char c : data.toCharArray()) {
            builder.append((char)(c + key));
        }
        return builder.toString();
    }
}