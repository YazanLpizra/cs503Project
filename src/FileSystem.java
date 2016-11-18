
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author yazan
 */
public class FileSystem {

    static final int MAX_BLOCK_DATA_SIZE = 249;
    static final int MAX_NUM_OF_BLOCKS = 35;//each block is 256 bytes, 8.75kb
    static final int MAX_ENTRY_SIZE = 64; //bytes

    private final int MAX_FILE_SIZE = 1048576;//1MB
    private final int MAX_METADATA_LENGTH = 408;

    private String metaData;//max size=409 bytes
    private HeaderBlock header;
    private DataBlock[] blockArr;
    private String name;
    private FileWriter writer;
    int freeFCBPointer = 0, //start at 0kb
            freeHeaderPointer = 1024, //start at 1kb
            freeNodePointer = 1536; //start at 1.5kb

    public FileSystem(HeaderBlock header, String name) {
        this.header = header;
        blockArr = new DataBlock[MAX_NUM_OF_BLOCKS];
        this.name = name;
        metaData = "";//set max capacity to 480 chars
        try {
            writer = new FileWriter(this.name + ".pfs");
        } catch (IOException e) {
            System.err.println("Cannot create pfs file " + this.name + ".\n" + e);
        }
    }

    public void close() {
        try {
            writer.write(metaData + "|", freeHeaderPointer, MAX_METADATA_LENGTH + 1);
            freeHeaderPointer += MAX_METADATA_LENGTH;
            String s = Arrays.toString(header.getBitmap()).substring(0, 102);
            writer.write(s + "|", freeHeaderPointer, s.length() + 1);
            for (DataBlock dataBlock : blockArr) {
                s = dataBlock.toFile() + "," + pad(Driver.fileSystems.indexOf(this) + "", '0', 2) + "," + pad(this.indexOf(dataBlock) + "", '0', 2) + "|";
                writer.write(s, freeNodePointer, MAX_BLOCK_DATA_SIZE);
                freeNodePointer += MAX_BLOCK_DATA_SIZE;
            }
        } catch (IOException e) {
            System.err.println("Unable to write data to the file.");
        }
    }

    public String getMetaData() {
        return metaData;
    }

    public void appendToMetaData(String s) {
        metaData += s;
        metaData = metaData.substring(0, MAX_METADATA_LENGTH);
    }

    public HeaderBlock getHeader() {
        return header;
    }

    public void setHeader(HeaderBlock header) {
        this.header = header;
    }

    public String getName() {
        return name;
    }

    public boolean isFull() {
        return header.getNextFreeLocation() == -1 || header.isFull();
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataBlock getBlockAt(int index) {
        return blockArr[index];
    }

    public int indexOf(DataBlock db) {
        for (int i = 0; i < blockArr.length; i++) {
            if (blockArr[i].equals(db)) {
                return i;
            }
        }
        return -1;
    }

    public void addFile(File f) {
        //check if input file is file
        if (f.exists() && f.isFile() && f.length() < MAX_FILE_SIZE) {
            putFile(f);
        } else {
            System.out.println("File could not be added to filesystem. Possible errors include:\n\tFile with the given name does not exist\n\tSpecified file exceeds the max capacity of 1MB");
        }

    }

    public void chooseFile() {
        //code used from here: http://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
        //and here; http://stackoverflow.com/questions/5077304/how-do-i-read-file-x-bytes-at-a-time-in-java
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));//set directory to user home directory
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Simple text files (.txt)", "txt");//limit to simple txt files for now
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(new JFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            addFile(selectedFile);
        }
    }

    public void putFile(File selectedFile) {
        //make fileDataEntry obj for file
        try {
            System.out.printf("Selected file: %s length: %d\n", selectedFile.getAbsolutePath(), selectedFile.length());
            BasicFileAttributes attr = Files.readAttributes(Paths.get(selectedFile.toURI()), BasicFileAttributes.class);

            int offset = 0;
            int fileLength = (int) selectedFile.length();
            StringBuilder sb;
            RandomAccessFile reader = new RandomAccessFile(selectedFile, "r");
            //fill the file data entry
            FileDataEntry entry = new FileDataEntry();
            entry.setFileName(selectedFile.getName());
            entry.setFileSize(fileLength);
            entry.setStartingPFS((short) Driver.fileSystems.indexOf(this));
            entry.setEndingPFS((short) Driver.fileSystems.indexOf(this));
            BasicFileAttributes attribs = Files.readAttributes(Paths.get(selectedFile.toURI()), BasicFileAttributes.class);
            entry.setTimeCreated(attribs.creationTime().toString().split("T")[0]);

            int prevIndex = -1;
            while (offset < fileLength) {//while the file offset is less than the length of the file
                //read MAX_BLOCK_DATA_SIZE bytes from the file at a time, and increase the offset by the number of read chars
                int numToRead = fileLength - offset < MAX_BLOCK_DATA_SIZE ? fileLength - offset : MAX_BLOCK_DATA_SIZE;
                byte[] chars = new byte[numToRead];
                int charsRead = reader.read(chars);
                offset += charsRead;
                reader.seek(offset);

                //load read bytes into a stringbuilder
                sb = new StringBuilder();
                for (int i = 0; i < chars.length; i++) {
                    sb.append((char) chars[i]);
                }

                //initialize a new block with the read string, and mark the location of the block as used
                int index = header.getNextFreeLocation();
                if (index != -1) {//-1 would happen if there is no available location in the bitmap of the pfs
                    blockArr[index] = new DataBlock(sb.toString());
                    header.setBitPosition(index);
                    //manage the linked list part 
                    if (prevIndex != -1) {//if not head. if head, we do not have a prev
                        blockArr[prevIndex].setNext(blockArr[index]);
                    } else {
                        entry.setStartingBlock((short) index);
                    }
                    if (offset >= fileLength) {//we have read the last part of the file
                        entry.setEndingBlock((short) index);
                    }
                    prevIndex = index;
                } else {//pfs is full, create another pfs and continue writing to the new one
                    System.out.println("No free blocks available in pfs... Expanding file system");
                    System.out.printf("Number of set bits: %d\n%s", getHeader().getNumSetBits(), Arrays.toString(getHeader().getBitmap()));
                    Driver.fileSystems.add(++Driver.indexLastFS, new FileSystem(new HeaderBlock(), this.getName() + "." + Driver.indexLastFS));//create new filesystem
                    System.out.println("index of last FS: " + Driver.indexLastFS);
                    //recursively call a method to read the remainder of the file into pfs files. 
                    //as long as the file is too long, we will create a new pfs and load as much as we can into it.
                    entry = Driver.fileSystems.get(Driver.indexLastFS).readRemainder(blockArr[prevIndex], Driver.fileSystems.get(Driver.indexLastFS), reader, entry);
                    writer.write(entry.toFile().substring(0, MAX_ENTRY_SIZE - 1) + "|", freeFCBPointer, MAX_ENTRY_SIZE);
                    freeFCBPointer += MAX_ENTRY_SIZE;
                    break;
                }
            }
            header.addFile(entry);
            selectedFile.delete();
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to open selected file:\n" + ex);
        } catch (IOException ex) {
            System.err.println("Error trying to read or seek within the file or when trying to delete local file:\n" + ex);
        }
    }

    public FileDataEntry readRemainder(DataBlock prev, FileSystem fileSystem, RandomAccessFile reader, FileDataEntry entry) {
        try {
            int offset = (int) reader.getFilePointer();
            int fileLength = (int) reader.length();
            int prevIndex = -1;
            StringBuilder sb;
            while (offset < fileLength) {
                //read up to MAX_BLOCK_DATA_SIZE bytes from the file at a time, and update the file pointer offset 
                int numToRead = fileLength - offset < MAX_BLOCK_DATA_SIZE ? fileLength - offset : MAX_BLOCK_DATA_SIZE;
                byte[] chars = new byte[numToRead];
                int charsRead = reader.read(chars);
                offset += charsRead;
                reader.seek(offset);

                //load read bytes into a stringbuilder
                sb = new StringBuilder();
                for (int i = 0; i < chars.length; i++) {
                    sb.append((char) chars[i]);
                }

                //initialize a new block with the read string, and mark the location of the block as used
                int index = header.getNextFreeLocation();
                if (index != -1) {//-1 would happen if there is no available location in the bitmap of the pfs
                    blockArr[index] = new DataBlock(sb.toString());
                    header.setBitPosition(index);

                    if (getHeader().getNumSetBits() == 26) {
                        System.out.println("come here");
                    }

                    //manage the linked list part 
                    if (prevIndex != -1) {//if not head. if head, we do not have a prev
                        blockArr[prevIndex].setNext(blockArr[index]);
                    } else {
                        prev.setNext(blockArr[index]);
                    }
                    if (offset >= fileLength) {//we have read the last part of the file
                        entry.setEndingBlock((short) index);
                        entry.setEndingPFS((short) Driver.fileSystems.indexOf(this));
                    }
                    prevIndex = index;
                } else {//pfs is full, create another pfs and continue writing to the new one
                    System.out.println("No free blocks available in pfs... Expanding file system");
                    System.out.printf("Number of set bits: %d\n%s\n", getHeader().getNumSetBits(), Arrays.toString(getHeader().getBitmap()));
                    Driver.fileSystems.add(++Driver.indexLastFS, new FileSystem(new HeaderBlock(), this.getName() + "." + ++Driver.indexLastFS));//create new filesystem
                    System.out.println("Index of last FS: " + Driver.indexLastFS);
                    //recursively call a method to read the remainder of the file into pfs files. 
                    //as long as the file is too long, we will create a new pfs and load as much as we can into it.
                    entry = Driver.fileSystems.get(Driver.indexLastFS).readRemainder(blockArr[prevIndex], Driver.fileSystems.get(Driver.indexLastFS), reader, entry);
                    writer.write(entry.toFile().substring(0, MAX_ENTRY_SIZE - 1) + "|", freeFCBPointer, MAX_ENTRY_SIZE);
                    freeFCBPointer += MAX_ENTRY_SIZE;
                    break;
                }
            }
            header.addFile(entry);//add file data entry to the pfs header
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to open selected file.\n" + ex);
        } catch (IOException ex) {
            System.err.println("Error trying to read or seek within the file.\n" + ex);
        }
        return entry;
    }

    public String pad(String s, char letter, int len) {
        while (s.length() < len) {
            s += letter;
        }
        return s;
    }
}
