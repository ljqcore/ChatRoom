
public class Room {
    int id; // 识别每一个聊天室的标识，在多个聊天室响应时会用上，此项目没用上
    private String perName = null; // 管理员名称
    private String tag = null; // 聊天室标签
    private String name = null; // 聊天室名称
    private String info = null; // 聊天室简介

    public Room(String name, String perName, String tag, String info){
        this.name = name;
        this.perName = perName;
        this.tag = tag;
        this.info = info;
    }

    public Room(){

    }

    public void setPerName(String perName) {
        this.perName = perName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getName(){
        return name;
    }

    public String getPerName(){
        return perName;
    }

    public String getTag(){
        return tag;
    }

    public String getInfo(){
        return info;
    }


}
