package com.example.doc2mcq.app;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocUploadServiceImpl implements DocUploadService {
    @Override
    public void uploadToLocal(MultipartFile file) throws IOException {
        try{
            byte[] data = file.getBytes();
            Path path = Paths.get("C:\\projects\\upload\\upload_" + file.getOriginalFilename());
            Files.write(path, data);
            //System.out.println("C:\\projects\\upload\\upload_"+file.getOriginalFilename()+".docx");
            XWPFDocument docx = new XWPFDocument(new FileInputStream("C:\\projects\\upload\\upload_"+file.getOriginalFilename()));
            XWPFWordExtractor we =new XWPFWordExtractor(docx);
            System.out.println("THIS IS DATA======= "+we.getText());
            JSONArray result = CDL.toJSONArray("question, options, answer \n"+we.getText());
            System.out.println(result);
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
