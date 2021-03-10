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
            //String string = "name, city, age \n john, chicago, 22 \n gary, florida, 35 \n";
            JSONArray result = CDL.toJSONArray("question,options\n"+we.getText());
            System.out.println(result);

            /*System.out.println(string);
            JSONArray result2 = CDL.toJSONArray(string);
            System.out.println(result2);*/


            /*JSONObject obj = new JSONObject();
            obj.put("Subject",we.getText() );
            System.out.println(obj);*/
            /*JSONArray arr = new JSONArray();


            JSONObject obj = new JSONObject();
            obj.put("Subject", "Maths");
            obj.put("Marks", "10");
            obj.put("Date", "20-10-2021");

            System.out.println(obj);*/
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
