package server;

import java.util.ArrayList;
import java.util.List;

public class Host {

    private String name;

    private String appBase;

    private List<Wapper> wapperList;

    public Host(){
        this.wapperList = new ArrayList<>();
    }

    public List<Wapper> getWapperList() {
        return wapperList;
    }

    public void setWapperList(List<Wapper> wapperList) {
        this.wapperList = wapperList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppBase() {
        return appBase;
    }

    public void setAppBase(String appBase) {
        this.appBase = appBase;
    }
}
