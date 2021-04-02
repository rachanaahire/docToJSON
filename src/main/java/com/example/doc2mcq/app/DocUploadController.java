package com.example.doc2mcq.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path ="/api")
public class DocUploadController {
    private final DocUploadServiceImpl docUploadService;

    @Autowired
    public DocUploadController(DocUploadServiceImpl docUploadService) {
        this.docUploadService = docUploadService;
    }

    @GetMapping
    public String helloWorld() {
        return "Hello";
    };

    @PostMapping("/upload")
    public String uploadLocal(@RequestParam("file") MultipartFile file) throws IOException {
        return docUploadService.uploadToLocal(file);
    }
}
