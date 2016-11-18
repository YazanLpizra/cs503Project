
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Yazan
 */
public class HeaderBlock {
    //assuming max 16 files per individual pfs, the header should use up to 1.5kb

    static final int MAX_NUM_FILES = 16;
    private ArrayList<FileDataEntry> fcb;//should be max 1kb
    //using byte because it represents 1 signed byte., when set up in csv/array format, takes 2*len+1 bytes (69 bytes)
    private byte bitmap[];

    public HeaderBlock() {
        fcb = new ArrayList<>();
        bitmap = new byte[FileSystem.MAX_NUM_OF_BLOCKS];//will have 28 data blocks in the pfs. takes up 28 bytes memory
    }

    public ArrayList<FileDataEntry> getFcb() {
        return fcb;
    }

    public void setBitPosition(int index) {
        this.bitmap[index] = 1;
    }

    public void resetBitPosition(int index) {
        this.bitmap[index] = 0;
    }

    public int getBitAtPosition(int index) {
        return this.bitmap[index];
    }

    public int getNextFreeLocation() {
        for (int i = 0; i < bitmap.length; i++) {
            if (bitmap[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    public boolean addFile(FileDataEntry fd) {
        if (fcb.size() < 10) {
            fcb.add(fd);
            return true;
        }
        return false;
    }

    public boolean isFull() {
        return fcb.size() >= MAX_NUM_FILES;
    }

    public void removeFile(FileDataEntry fd) {
        fcb.remove(fd);
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public int getNumSetBits() {
        int num = 0;
        for (int i = 0; i < bitmap.length; i++) {
            if (bitmap[i] == 1) {
                num++;
            }
        }
        return num;
    }
}