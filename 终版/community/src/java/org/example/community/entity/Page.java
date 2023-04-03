package org.example.community.entity;

// 实现分页
public class Page {
    // 当前页码
    private int current = 1;
    // 每页数据展示的最大数
    private int limit = 10;
    // 数据总数
    private int rows;
    // 查询路径
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        // 只有当前页数大于等于1 才可以下一页(比如:跳转传递的参数是-1则就符合逻辑)
        if(current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        // 设置每页数据展示的上限条件
        if(limit >= 1 && limit <= 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        // 数据总数大于0 才可以显示
        if(rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset(){
        // 计算公式: current * limit - limit (当前页码 * 每页最大显示数据量 - 每页最大显示数据量)
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        // 计算公式: rows / limit (数据总行数 / 每页最大数据数)
        if(rows % limit == 0){
            // 如果limit恰好能被rows整数那么为整数页
            return rows / limit;
        }else{
            // 如果不能被整数,那么还需要一页将剩下的数据展示所以+1
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        // 比如我们当前页为3,那么起始页就为1 2 (这里减2主要是显示当前页左边的2页,自己可以调整)
        int from = current - 2;
        // 如果当前current为1,那么我们起始页为1,否则为from
        return from < 1 ? 1 : from;
    }

    /**
     * 获取终止页
     * @return
     */
    public int getTo(){
        // 比如我们current为80,to指的是current右边的两页: 81 82
        int to = current + 2;
        // 获取总页数
        int total = getTotal();
        // 如果current为99,我们的total最大为100,那么total为101，超过了我们total的最大范围,所以直接显示到total
        return to > total ? total : to;
    }

}
