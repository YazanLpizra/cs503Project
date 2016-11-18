/**
 *
 * @author Yazan
 */
public class DataBlock {

    private String data;
    private DataBlock next;

    public DataBlock(String data, DataBlock next) {
        this.data = data;
        this.next = next;
    }

    public DataBlock(String data) {
        this.data = data;
        next = null;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean equals(DataBlock block) {
        //check equality by comparing the data from this block vs the input block. We check recursively until the pointers point to null 
        //to ensure that the nodes we are comparing are from the same file. Extremely inefficient for large files
        if (block.getNext() == null || this.next == null) {
            return block.data.equals(this.data);
        } else {
            return block.data.equals(this.data) && block.getNext().equals(this.next);
        }
    }

    public DataBlock getNext() {
        return next;
    }

    public void setNext(DataBlock next) {
        this.next = next;
    }

    @Override
    public String toString() {
        if (next != null) {
            return data + next.toString();
        } else {
            return data;
        }
    }

    public String toFile(){
        return data;
    }
}
