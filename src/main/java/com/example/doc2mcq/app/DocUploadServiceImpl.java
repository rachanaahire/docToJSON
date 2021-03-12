package com.example.doc2mcq.app;

import org.apache.poi.xwpf.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DocUploadServiceImpl implements DocUploadService {

    @Override
    public void uploadToLocal(MultipartFile file) throws IOException {
        try{
            byte[] data = file.getBytes();
            Path path = Paths.get("C:\\projects\\upload\\upload_" + file.getOriginalFilename());
            Files.write(path, data);
            XWPFDocument docx = new XWPFDocument(new FileInputStream("C:\\projects\\upload\\upload_"+file.getOriginalFilename()));

            /////////HEADER WORD DOC
            List<XWPFHeader> headerList = docx.getHeaderList();
            JSONObject myObj = new JSONObject();
            for (XWPFHeader xwpfHeader : headerList) {

                String str = xwpfHeader.getText();
                String[] strArr = str.split("\\n");
                myObj.put("subject",strArr[0]);
                myObj.put("marks",strArr[1]);
                myObj.put("date",strArr[2]);
            }

            ////// BODY DOC
            List<IBodyElement> bodyElements = docx.getBodyElements();
            JSONArray myArrObj = new JSONArray();
            JSONObject demo = new JSONObject();
            for (IBodyElement bodyElement : bodyElements) {
                if (bodyElement instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) bodyElement;
                    List<XWPFTableRow> rows = table.getRows();
                    for (XWPFTableRow row : rows) {
                        demo.put("string",row.getCell(2).getText());
                        JSONObject obj = new JSONObject();
                        obj.put("quesNo",row.getCell(0).getText());
                        obj.put("question",row.getCell(1).getText());
                        obj.put("options",row.getCell(2).getText());
                        obj.put("answer",row.getCell(3).getText());
                        myArrObj.put(obj);
                    }
                    myObj.put("data",myArrObj);
                    System.out.println(myObj);
                }
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
