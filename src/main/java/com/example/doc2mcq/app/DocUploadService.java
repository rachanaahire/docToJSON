package com.example.doc2mcq.app;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface DocUploadService {
    public String uploadToLocal(MultipartFile file) throws IOException;
}
