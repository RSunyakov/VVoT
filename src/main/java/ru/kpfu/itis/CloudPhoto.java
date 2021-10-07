package ru.kpfu.itis;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CloudPhoto {
    public void upload(String path, String album) {
        try (Stream<Path> paths = Files.walk(Paths.get(path), 1)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            AmazonS3 s3 = InitConnection.getInstance();
            List<Path> serverFilesPaths = getOnlyImages(files);
            for (int i = 0; i < serverFilesPaths.size(); i++) {
                s3.putObject("vvot", serverFilesPaths.get(i).toString(), new File(files.get(i).toString()));
            }
            putInAlbum(s3, album, serverFilesPaths);
        } catch (IOException e) {
            System.err.println(e);
            System.out.println("Такого пути не существует");
        }
    }

    public List<Path> getOnlyImages(List<Path> files) {
        List<Path> validatedPaths = new ArrayList<>();
        for (Path file : files) {
            String filename = file.toString();
            String fileExtension = filename.substring(filename.lastIndexOf(".") + 1);
            if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
                validatedPaths.add(Paths.get(filename.substring(filename.lastIndexOf("\\") + 1)));
            }
        }
        return validatedPaths;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private void putInAlbum(AmazonS3 s3, String album, List<Path> serverPaths) {
        S3Object albumObject = s3.getObject("vvot", "albums.json");
        S3ObjectInputStream s3ObjectInputStream = albumObject.getObjectContent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
        try {
            String jsonText = readAll(bufferedReader);
            if (jsonText.isEmpty()) {
                JSONObject json = new JSONObject();
                json.put(album, serverPaths);
                s3.putObject("vvot", "albums.json", json.toString());
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, List<String>> jsonMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, List<String>>>() {
                });
                if (!jsonMap.containsKey(album)) {
                    ArrayList<String> pathList = new ArrayList<>();
                    for (Path serverPath : serverPaths) {
                        pathList.add(serverPath.toString());
                    }
                    jsonMap.put(album, pathList);
                    s3.putObject("vvot", "albums.json", new ObjectMapper().writeValueAsString(jsonMap));
                } else {
                    for (Path serverPath : serverPaths) {
                        jsonMap.get(album).add(serverPath.toString());
                    }
                    s3.putObject("vvot", "albums.json", new ObjectMapper().writeValueAsString(jsonMap));
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    public void download(String path, String album) {
        if (isPathValid(path)) {
            path = pathValidator(path);
            AmazonS3 s3 = InitConnection.getInstance();
            S3Object albumObject = s3.getObject("vvot", "albums.json");
            S3ObjectInputStream s3ObjectInputStream = albumObject.getObjectContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
            try {
                String jsonText = readAll(bufferedReader);
                if (jsonText.isEmpty()) {
                    System.out.println("Не создано ни одного альбома");
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, List<String>> jsonMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, List<String>>>() {
                    });
                    if (!jsonMap.containsKey(album)) {
                        System.out.println("Такого альбома нет");
                    } else {
                        List<String> pathList = jsonMap.get(album);
                        for (String s : pathList) {
                            S3Object photo = s3.getObject("vvot", s);
                            S3ObjectInputStream inputStream = photo.getObjectContent();
                            FileUtils.copyInputStreamToFile(inputStream, new File(path + photo.getKey()));
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println(e);
            }
        } else {
            System.out.println("Неверный путь");
        }
    }

    public static boolean isPathValid(String path) {
        try {
            return Files.isDirectory(Paths.get(path));
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    public String pathValidator(String path) {
        if (path.endsWith("\\")) return path;
        else return path + "\\";
    }


    public Set<String> list() {
        AmazonS3 s3 = InitConnection.getInstance();
        S3Object albumObject = s3.getObject("vvot", "albums.json");
        S3ObjectInputStream s3ObjectInputStream = albumObject.getObjectContent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
        try {
            String jsonText = readAll(bufferedReader);
            if (jsonText.isEmpty()) {
                return null;
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, List<String>> jsonMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, List<String>>>() {
                });
                return jsonMap.keySet();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> list(String album) {
        AmazonS3 s3 = InitConnection.getInstance();
        S3Object albumObject = s3.getObject("vvot", "albums.json");
        S3ObjectInputStream s3ObjectInputStream = albumObject.getObjectContent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
        try {
            String jsonText = readAll(bufferedReader);
            if (jsonText.isEmpty()) {
                return null;
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, List<String>> jsonMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, List<String>>>() {
                });
                return jsonMap.get(album);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

