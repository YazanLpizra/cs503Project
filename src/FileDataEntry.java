
/**
 *
 * @author yazan
 */
public class FileDataEntry {

    //max size of a filedata object is 63 bytes + 1 byte for the '|' delimiter = 64 bytes
    private final int MAX_FILE_NAME_LENGTH = 20,
            MAX_REMARKS_LENGTH = 21;

    private int fileSize; //4 bytes
    private short startingBlock, endingBlock, startingPFS, endingPFS;//2 bytes each, 8 total
    private String fileName = "", //to be max 20 chars
            remarks = "", //to be max 22 chars
            timeCreated = "";//to be exactly 10 chars

    public FileDataEntry() {
        fileName = String.format("%0" + MAX_FILE_NAME_LENGTH + "d", 0).replace('0', ' ');
        remarks = String.format("%0" + MAX_REMARKS_LENGTH + "d", 0).replace('0', ' ');
        timeCreated = String.format("%0" + 10 + "d", 0).replace('0', ' ');
        fileSize = -1;
        startingBlock = -1;
        endingBlock = -1;
        startingPFS = -1;
        endingPFS = -1;
    }

    public FileDataEntry(int fileSize, short startingBlock, short endingBlock, short startingPFS, short endingPFS, String fileName, String remarks, String timeCreated) {
        this.fileSize = fileSize;
        this.startingBlock = startingBlock;
        this.endingBlock = endingBlock;
        this.startingPFS = startingPFS;
        this.endingPFS = endingPFS;
        this.fileName = fileName.trim();
        this.remarks = remarks;
        this.timeCreated = timeCreated;
    }

    public short getEndingBlock() {
        return endingBlock;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRemarks() {
        return remarks;
    }

    public int getFileSize() {
        return fileSize;
    }

    public short getStartingBlock() {
        return startingBlock;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setEndingBlock(short endingBlock) {
        this.endingBlock = endingBlock;
    }

    public void setFileName(String fileName) {
        System.out.println("Filename: " + fileName);
        if (fileName.length() > MAX_FILE_NAME_LENGTH) {
            this.fileName = fileName.substring(0, MAX_FILE_NAME_LENGTH);
        } else {
            this.fileName = fileName;
            while (this.fileName.length() < MAX_FILE_NAME_LENGTH) {
                this.fileName += " ";
            }
            
        }
    }

    public void addRemarks(String remarks) {
        if (remarks.isEmpty()) {
            this.remarks = remarks.length() > MAX_REMARKS_LENGTH ? remarks.substring(0, MAX_REMARKS_LENGTH) : remarks;
        } else {
            this.remarks = (this.remarks.trim() + " " + remarks).length() > MAX_REMARKS_LENGTH ? (this.remarks.trim() + " " + remarks).substring(0, MAX_REMARKS_LENGTH) : (this.remarks.trim() + " " + remarks);
        }
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setStartingBlock(short startingBlock) {
        this.startingBlock = startingBlock;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public short getEndingPFS() {
        return endingPFS;
    }

    public short getStartingPFS() {
        return startingPFS;
    }

    public void setEndingPFS(short endingPFS) {
        this.endingPFS = endingPFS;
    }

    public void setStartingPFS(short startingPFS) {
        this.startingPFS = startingPFS;
    }

    @Override
    public String toString() {
        return String.format("\t%20s\t%15s\t%15s\t%15s\t%15s\t%15s\t%15s\t%15s\n",
                getFileName(), getFileSize(), getTimeCreated(), getStartingPFS(), getStartingBlock(),
                getEndingPFS(), getEndingBlock(), getRemarks()
        );
    }
    
    public String toFile(){
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                getFileName(), getFileSize(), getTimeCreated(), getStartingPFS(), getStartingBlock(),
                getEndingPFS(), getEndingBlock(), getRemarks()
        );
    }
}
