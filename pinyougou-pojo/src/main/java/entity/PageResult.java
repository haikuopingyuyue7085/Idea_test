package entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    private long total;//总记录数(多少条数据)
    private List rows;//当前页记录(当前显示哪些数据)

    //new对象时带参数方便
    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
