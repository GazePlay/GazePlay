package com.gazeplay.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
public class FileUploadController {

    @RequestMapping(value = "/hello", method = RequestMethod.POST)
    public String hello(@RequestParam("name") String name) {
        return "Hello, " + name;
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public @ResponseBody
    String uploadFileHandler(@RequestParam("name") String name,
                             @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            try {
                createDirOnServer(name, file);

                return "You successfully uploaded file=" + name;
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name
                + " because the file was empty.";
        }
    }

    private void createDirOnServer(@RequestParam("name") String name, @RequestParam("file") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        // Creating the directory to store file
        String rootPath = System.getProperty("catalina.home");
        File dir = new File(rootPath + File.separator + "tmpFiles");
        if (!dir.exists())
            dir.mkdirs();

        // Create the file on server
        File serverFile = new File(dir.getAbsolutePath()
            + File.separator + name);
        BufferedOutputStream stream = new BufferedOutputStream(
            new FileOutputStream(serverFile));
        stream.write(bytes);
        stream.close();
    }
}
