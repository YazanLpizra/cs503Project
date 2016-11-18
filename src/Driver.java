
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;

/**
 *
 * @author Yazan
 */
public class Driver {

    static ArrayList<FileSystem> fileSystems = new ArrayList<>();
    static int indexLastFS = 0;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("size: " + fileSystems.size());
        System.out.println("Please type help to see list of possible commands.");
        boolean loop = true;
        while (loop) {
            System.out.print("\nub_pfs>> ");
            String input = in.nextLine();
            String[] splitInput = input.split(" ");

            switch (splitInput[0]) {
                case "open":
                    if (splitInput.length == 2 && fileSystems.isEmpty()) { //filesystem to open is specified
                        /*
                        try {
                            Scanner reader = new Scanner(new FileReader(splitInput[1] + ".pfs"));
                            reader.useDelimiter("|");
                            String token = reader.next();
                            Scanner tokenReader = new Scanner(token);
                            tokenReader.useDelimiter(",");
                            while(tokenReader.hasNext()){
                                
                            }
                        } catch (FileNotFoundException e) {
                            
                         */
                        fileSystems.add(new FileSystem(new HeaderBlock(), splitInput[1]));
                        System.out.println("Created filesystem called: " + fileSystems.get(indexLastFS).getName());
                        System.out.println("Index of last fs: " + Driver.indexLastFS);
                        //}
                    } else {//filesystem is not specified
                        System.out.println("Incorrect number of arguments or a pfs already exists. Please type 'help' to see format of valid commands.");
                    }
                    break;

                case "put":
                    for (FileSystem fileSystem : fileSystems) {
                        if (!fileSystem.isFull()) {
                            if (input.split(" ").length > 1) { //file is specified
                                fileSystem.addFile(new File(input.split(" ")[1]));
                                break;
                            } else {//file is not specified
                                fileSystem.chooseFile();
                                break;
                            }
                        }
                    }
                    break;

                case "get":
                    if (splitInput.length == 2) {
                        outerloop2:
                        for (FileSystem fs : fileSystems) {
                            for (FileDataEntry e : fs.getHeader().getFcb()) {
                                if (e.getFileName().trim().equals(splitInput[1])) {
                                    int startBlock = e.getStartingBlock();
                                    DataBlock block = fs.getBlockAt(startBlock);
                                    System.out.println(block.toString());
                                }
                            }
                        }
                    } else {
                        System.out.println("Incorrect number of arguments. Please type 'help' to see format of valid commands.");
                    }
                    break;

                case "rm":
                    if (splitInput.length == 2) {
                        outerlooprm:
                        for (FileSystem fs : fileSystems) {
                            for (FileDataEntry e : fs.getHeader().getFcb()) {
                                if (e.getFileName().trim().equals(splitInput[1])) {
                                    int index = e.getStartingBlock();
                                    DataBlock block = fs.getBlockAt(index);
                                    boolean isDone = false;
                                    while (!isDone) {
                                        if (index != -1) {
                                            fs.getHeader().resetBitPosition(index);
                                            block = block.getNext();
                                            index = block == null ? -1 : fs.indexOf(block);
                                        } else {
                                            isDone = true;
                                        }
                                    }

                                    fs.getHeader().getFcb().remove(e);
                                    break outerlooprm;
                                }
                            }
                        }
                    } else {
                        System.out.println("Incorrect number of arguments. Please type 'help' to see format of valid commands.");
                    }
                    break;

                case "ls":
                    for (FileSystem fs : fileSystems) {
                        System.out.printf("File System %s:\n", fs.getName());
                        if (splitInput.length == 1) {
                            for (FileDataEntry e : fs.getHeader().getFcb()) {
                                System.out.println("\t" + e.getFileName());
                            }
                        } else if (input.contains("-l")) {
                            System.out.printf("\t%20s\t%15s\t%15s\t%15s\t%15s\t%15s\t%15s\t%15s\n",
                                    "FileName", "FileSize", "TimeCreated", "StartingPFS", "StartingBlock", "EndingPFS", "EndingBlock", "Remarks");
                            for (FileDataEntry e : fs.getHeader().getFcb()) {
                                System.out.println(e.toString());
                            }
                        } else if (input.contains("-r")) {
                            System.out.printf("\t%15s\t%15s\n", "FileName", "Remarks");
                            for (FileDataEntry e : fs.getHeader().getFcb()) {
                                System.out.printf("\t%15s\t%15s\n", e.getFileName(), e.getRemarks());
                            }
                        } else {
                            System.out.println("Unrecognized options used. Please type 'help' to see format of valid commands.");
                        }
                    }

                    break;

                case "putr":
                    if (splitInput.length >= 3) {
                        String remark = input.split("\"")[1];
                        outerLoop:
                        for (FileSystem fs : fileSystems) {
                            for (FileDataEntry entry : fs.getHeader().getFcb()) {
                                if (entry.getFileName().trim().equals(splitInput[1])) {
                                    entry.addRemarks(remark);
                                    break outerLoop;
                                }
                            }
                        }
                    } else {
                        System.out.println("Incorrect number of arguments. Please type 'help' to see format of valid commands.");
                    }
                    break;

                case "kill":
                    if (splitInput.length == 2) {
                        if (splitInput[1].equals("-all")) {
                            System.out.println("Deleting all file systems will delete everything irreversibly. Are you sure you want to continue? ([y]es/[n]o)");
                            char doKillPFS = in.next().toUpperCase().charAt(0);
                            if (doKillPFS == 'Y') {
                                fileSystems.clear();
                            }
                        } else {
                            System.out.println("Killing file systems may corrupt data and files that have parts of their data in the file system. Are you sure you want to kill " + splitInput[1] + "? ([y]es/[n]o)");
                            char doKillPFS = in.next().toUpperCase().charAt(0);
                            if (doKillPFS == 'Y') {
                                String fsName = input.split(" ")[1];
                                for (int i = 0; i < fileSystems.size(); i++) {
                                    if (fileSystems.get(i).getName().equals(fsName)) {
                                        fileSystems.remove(i);
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Incorrect number of arguments. Please type 'help' to see format of valid commands.");
                    }
                    break;

                case "view":
                    if (splitInput.length == 1) {
                        System.out.println("Printing out the bitmaps of existing file systems.");
                        for (FileSystem fs : fileSystems) {
                            System.out.printf("FileSystem: %s\n\tNumber of set bits: %d\n\t%s\n", fs.getName(), fs.getHeader().getNumSetBits(), Arrays.toString(fs.getHeader().getBitmap()));

                        }
                    } else {
                        System.out.println("Incorrect number of arguments. Please type 'help' to see format of valid commands.");
                    }
                    break;

                case "quit":
                    System.out.println("Terminating...");
                    for (FileSystem fs : fileSystems) {
                        fs.close();
                    }
                    System.exit(-1);
                    break;

                case "help":
                    System.out.printf(
                            "\t\tCOMMANDS\n"
                            + "\t\t---------\n"
                            + "- open pfsFileName\n"
                            + "\tOpen new Portable File System called 'pfsFileName'. You may only create one PFS at a time, but the PFS will expand as needed.\n"
                            + "- put\n"
                            + "\tAdd file to the file system. WARNING: file name length must be less than or equal to 20 characters. Options include:\n"
                            + "\t\t <no option>: Open file chooser popup to choose file to add to file system.\n"
                            + "\t\t /path/to/myfile.ext: Add the specified file to the file system.\n"
                            + "- get myfile\n"
                            + "\tPrint out the contents of the file to the console.\n"
                            + "- rm myfile\n"
                            + "\tRemove the file from the file system.\n"
                            + "- ls\n"
                            + "\tDisplay the file names of all files in the file system. Options include:\n"
                            + "\t\t -l: display all available information about files.\n"
                            + "\t\t -r: display only the file names and remarks.\n"
                            + "- view\n"
                            + "\tPrints out the bitmap(s) for the file system\n"
                            + "- putr myfile \"REMARKS\"\n"
                            + "\tAdd remarks to the specified file. Remark cannot contain any additional quotations. Max remark size is 22 characters.\n"
                            + "- kill\n"
                            + "\t Deletes entire file system and their contents permenantly.\n"
                            + "\t\t myFile.ext: Kill specified file system. WARNING: may corrupt files if they are saved across multiple file system. Options include:\n"
                            + "\t\t -all: Delete all file systems.\n"
                            + "- quit\n"
                            + "\tExit out of file system.\n"
                            + "\n- help\n"
                            + "\tDisplay this help screen again.\n");
                    break;

                default:
                    System.out.println("Command not found. Please type 'help' to see format of valid commands.");
            }
        }
    }

    public static String join(String[] array, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(delimiter);
        }
        return sb.toString();
    }
}
