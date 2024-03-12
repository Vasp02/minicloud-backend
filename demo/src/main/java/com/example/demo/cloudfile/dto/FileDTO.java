package com.example.demo.cloudfile.dto;



public class FileDTO {
    public static enum FileType {
        FILE,
        DIRECTORY
    }

    private String name;
    private FileType type;
    private String pathFromUserRoot;
    private String base64Image;

    public FileDTO() {
    }

    public FileDTO(String name, FileType type, String base64Image,String pathFromUserRoot) {
        this.name = name;
        this.type = type;
        this.base64Image = base64Image;
        this.pathFromUserRoot = pathFromUserRoot;
    }
    public FileDTO(String name, FileType type,String pathFromUserRoot) {
        this.name = name;
        this.type = type;
        this.base64Image = "ISDIR";
        this.pathFromUserRoot = pathFromUserRoot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public String getPathFromUserRoot() {
        return pathFromUserRoot;
    }

    public void setPathFromUserRoot(String pathFromUserRoot) {
        this.pathFromUserRoot = pathFromUserRoot;
    }


    @Override
    public String toString() {
        return "FileDTO{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", base64Image='" + base64Image + '\'' +
                '}';
    }
}



