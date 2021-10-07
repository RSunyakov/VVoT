package ru.kpfu.itis;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class App {
    public static void main(String[] args) {
        CloudPhoto cloudPhoto = new CloudPhoto();
        switch (args[1]) {
            case "upload":
                cloudPhoto.upload(args[3], args[5]);
                break;
            case "download":
                cloudPhoto.download(args[3], args[5]);
                break;
            case "list":
                try {
                    String s = args[3];
                    List<String> photos = cloudPhoto.list(s);
                    if (photos == null) {
                        System.out.println("Такой альбом не создан");
                        break;
                    } else {
                        if (photos.isEmpty()) System.out.println("В альбоме нет фотографий");
                        else photos.forEach(System.out::println);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Set<String> albums = cloudPhoto.list();
                    if (albums == null) {
                        System.out.println("Не создано ни одного альбома");
                        break;
                    } else {
                        albums.forEach(System.out::println);
                    }
                }
        }
    }
}
