package server;

public class Service {

    private Mapper mapper;

    public Service(Mapper mapper) {
        this.mapper = mapper;
    }

    public Service() {
        this.mapper = new Mapper();
    }

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
}
