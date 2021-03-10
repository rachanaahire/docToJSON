package com.example.doc2mcq.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path ="/api")
public class DocUploadController {
    private final DocUploadService docUploadService;

    @Autowired
    public DocUploadController(DocUploadService docUploadService) {
        this.docUploadService = docUploadService;
    }

    @GetMapping
    public String helloWorld() {
        return "Hello";
    };

    @PostMapping("/upload")
    public void uploadLocal(@RequestParam("file") MultipartFile file) throws IOException {
        docUploadService.uploadToLocal(file);
    }
}
