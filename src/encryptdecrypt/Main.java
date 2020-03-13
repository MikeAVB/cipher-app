package encryptdecrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) {
        try {
            Options options = Options.fromArgs(args);
            Context context = new Context(options.getMode(), options.getAlgorithm());
            String data;
            if (options.isDataDefined()) {
                data = options.getData();
            } else {
                data = Files.readString(Paths.get(options.getInputFileName()));
            }
            String resultData = context.operate(data, options.getKey());
            if (options.isOutputFileDefined()) {
                Files.writeString(
                        Paths.get(options.getOutputFileName()),
                        resultData,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } else {
                System.out.println(resultData);
            }
        } catch (RuntimeException | IOException e) {
            System.err.println(e);
        }
    }
}

class Options {
    private Mode mode;
    private CipherAlgorithm algorithm;
    private int key;
    private boolean isDataDefined;
    private String data;
    private boolean isInputFileDefined;
    private String inputFileName;
    private String outputFileName;
    private boolean isOutputFileDefined;

    private Options() {
        this.mode = Mode.ENCRYPTION;
        this.algorithm = new ShiftAlgorithm();
        this.key = 5;
        this.isDataDefined = false;
        this.data = "";
        this.isInputFileDefined = false;
        this.inputFileName = "";
        this.isOutputFileDefined = false;
        this.outputFileName = "";
    }

    public static Options fromArgs(String[] args) {
        if (args == null || args.length == 0) {
            throw new RuntimeException("Arguments are null or empty!");
        }

        Options options = new Options();
        for (int i = 0; i < args.length - 1; i += 2) {
            String key = args[i];
            String value = args[i + 1];
            switch (key) {
                case "-mode":
                    if (value.equalsIgnoreCase("dec")) {
                        options.setMode(Mode.DECRYPTION);
                    } else if (value.equalsIgnoreCase("enc")) {
                        options.setMode(Mode.ENCRYPTION);
                    } else {
                        throw new RuntimeException("Unknown mode: " + value);
                    }
                    break;
                case "-alg":
                    if (value.equalsIgnoreCase("unicode")) {
                        options.setAlgorithm(new UnicodeAlgorithm());
                    } else if (value.equalsIgnoreCase("shift")) {
                        options.setAlgorithm(new ShiftAlgorithm());
                    } else {
                        throw new RuntimeException("Unknown algorithm: " + value);
                    }
                    break;
                case "-key":
                    options.setKey(Integer.parseInt(value));
                    break;
                case "-data":
                    if (options.isInputFileDefined) {
                        throw new RuntimeException(
                                "Ambiguous behavior: defining data when input file was defined!"
                        );
                    }
                    options.setDataDefined(true);
                    options.setData(value);
                    break;
                case "-in":
                    if (options.isDataDefined) {
                        throw new RuntimeException(
                                "Ambiguous behavior: defining input file when data was defined!"
                        );
                    }
                    options.setInputFileDefined(true);
                    options.setInputFileName(value);
                    break;
                case "-out":
                    options.setOutputFileDefined(true);
                    options.setOutputFileName(value);
                    break;
                default:
                    throw new RuntimeException("Parsing error. Unknown key: " + key);
            }
        }
        return options;
    }

    public boolean isDataDefined() {
        return isDataDefined;
    }

    public void setDataDefined(boolean dataDefined) {
        isDataDefined = dataDefined;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public CipherAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(CipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isInputFileDefined() {
        return isInputFileDefined;
    }

    public void setInputFileDefined(boolean inputFileDefined) {
        isInputFileDefined = inputFileDefined;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public boolean isOutputFileDefined() {
        return isOutputFileDefined;
    }

    public void setOutputFileDefined(boolean outputFileDefined) {
        isOutputFileDefined = outputFileDefined;
    }
}

class Context {
    private Mode mode;
    private CipherAlgorithm algorithm;


    public Context(Mode mode, CipherAlgorithm algorithm) {
        this.mode = mode;
        this.algorithm = algorithm;
    }

    public String operate(String data, int key) {
        switch (mode) {
            case DECRYPTION:
                return algorithm.decrypt(data, key);
            case ENCRYPTION:
                return algorithm.encrypt(data, key);
            default:
                throw new RuntimeException("Invalid context mode: " + mode);
        }
    }
}

enum Mode {
    ENCRYPTION,
    DECRYPTION
}

interface CipherAlgorithm {
    String encrypt(String data, int key);
    String decrypt(String data, int key);
}

class UnicodeAlgorithm implements CipherAlgorithm {

    @Override
    public String encrypt(String data, int key) {
        StringBuilder builder = new StringBuilder();
        for (char c : data.toCharArray()) {
            builder.append((char)(c + key));
        }
        return builder.toString();
    }

    @Override
    public String decrypt(String data, int key) {
        StringBuilder builder = new StringBuilder();
        for (char c : data.toCharArray()) {
            builder.append((char)(c - key));
        }
        return builder.toString();
    }
}

class ShiftAlgorithm implements CipherAlgorithm {

    @Override
    public String encrypt(String data, int key) {
        StringBuilder result = new StringBuilder();
        char[] chars = data.toCharArray();
        for (char aChar : chars) {
            CharType type = getCharType(aChar);
            if (type == CharType.LOW_LETTER) {
                aChar = (char) ((aChar - 97 + key) % 26 + 97);
            } else if (type == CharType.UPPER_LETTER) {
                aChar = (char) ((aChar - 65 + key) % 26 + 65);
            }
            result.append(aChar);
        }
        return result.toString();
    }

    @Override
    public String decrypt(String data, int key) {
        key = 26 - (key % 26);
        return encrypt(data, key);
    }

    private CharType getCharType(char aChar) {
        if (aChar >= 97 && aChar <= 122) {
            return CharType.LOW_LETTER;
        } else if (aChar >= 65 && aChar <= 90) {
            return CharType.UPPER_LETTER;
        } else {
            return CharType.NOT_LETTER;
        }
    }

    private enum CharType {
        LOW_LETTER,
        UPPER_LETTER,
        NOT_LETTER
    }
}