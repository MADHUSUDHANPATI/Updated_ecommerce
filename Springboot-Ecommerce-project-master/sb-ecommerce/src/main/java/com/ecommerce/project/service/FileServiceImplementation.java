package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImplementation implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        // Get the file names of current files / original files

        String originalFileName = file.getOriginalFilename();

        // Generate a unique file name to avoid same file names throughout the system

        String randomId = UUID.randomUUID().toString();

        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));   // finds position of last . and returns string from that index to end and concat to randon id
        String filePath = path + File.separator + fileName;

        //Check if the path exist , if not create the PATH

        File folder = new File(path);
        if(!folder.exists()) {
            folder.mkdir();
        }

        // upload to the server

        Files.copy(file.getInputStream(), Paths.get(filePath));

        // return the file name;
        return fileName;

    }
}
