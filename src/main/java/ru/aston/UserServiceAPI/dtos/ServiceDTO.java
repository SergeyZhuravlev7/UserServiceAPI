package ru.aston.UserServiceAPI.dtos;

public class ServiceDTO {

    private String id;
    private String url;

    public ServiceDTO(String id,String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
