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

            // Fetch DOC HEADER Data
            List<XWPFHeader> headerList = docx.getHeaderList();
            JSONObject myObj = new JSONObject();
            for (XWPFHeader xwpfHeader : headerList) {
                String str = xwpfHeader.getText();
                String[] strArr = str.split("Subject:|\\nMarks:|\\nDate:|\\n");
                myObj.put("subject",strArr[1].trim());
                myObj.put("marks",strArr[2].trim());
                myObj.put("date",strArr[3].trim());
            }

            // Fetch DOC BODY Data
            List<IBodyElement> bodyElements = docx.getBodyElements();
            JSONArray myArrObj = new JSONArray();
            for (IBodyElement bodyElement : bodyElements) {
                if (bodyElement instanceof XWPFTable) {
                    boolean hasImage = false;
                    XWPFTable table = (XWPFTable) bodyElement;
                    List<XWPFTableRow> rows = table.getRows();
                    for (XWPFTableRow row : rows) {
                        //options Object
                        String str = row.getCell(2).getText();
                        String[] strArr = str.split("\\([A-D]\\)");
                        JSONObject optObj = new JSONObject();
                        optObj.put("A",strArr[1].trim());
                        optObj.put("B",strArr[2].trim());
                        optObj.put("C",strArr[3].trim());
                        optObj.put("D",strArr[4].trim());

                        //mcq ArrayObject
                        JSONObject obj = new JSONObject();
                        obj.put("quesNo",row.getCell(0).getText());
                        obj.put("options",optObj);
                        obj.put("answer",row.getCell(3).getText());

                        List<XWPFTableCell> cells = row.getTableCells();
                        for (XWPFTableCell cell : cells) {
                            if (cell != null){
                                //System.out.println(cell.getText());
                                for (XWPFParagraph p : cell.getParagraphs()) {
                                    for (XWPFRun run : p.getRuns()) {
                                        List<XWPFPicture> pictures = run.getEmbeddedPictures();
                                        if (!pictures.isEmpty()){
                                            hasImage = true;
                                        }
                                         if (hasImage){
                                             for (XWPFPicture pic : pictures) {
                                                 JSONObject quesObj = new JSONObject();
                                                 //for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                                 byte[] pictureData = pic.getPictureData().getData();
                                                 //System.out.println(pictureData);
                                                 Path path2 = Paths.get("C:\\projects\\upload\\upload_" + row.getCell(0).getText());
                                                 Files.write(path2, pictureData);
                                                 quesObj.put("quesText",row.getCell(1).getText());
                                                 quesObj.put("quesImage", "C:\\projects\\upload\\upload_" + row.getCell(0).getText());
                                                 obj.put("question", quesObj);

                                                 //System.out.println("picture : " + pictureData+" of Row Index"+ row.getCell(0).getText());
                                             }
                                         } else {
                                             obj.put("question",row.getCell(1).getText());
                                         }
                                        //System.out.println(pictures.isEmpty()+"}]]]]]]");
                                        /*for (XWPFPicture pic : pictures) {
                                        //for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                            byte[] pictureData = pic.getPictureData().getData();
                                            System.out.println(pictureData);
                                            Path path2 = Paths.get("C:\\projects\\upload\\upload_" + row.getCell(0).getText());
                                            Files.write(path2, pictureData);
                                            //System.out.println("picture : " + pictureData+" of Row Index"+ row.getCell(0).getText());
                                        }*/
                                    }
                                }
                            }
                        }
                        myArrObj.put(obj);

                        //Fetch image from doc
                        /*List<XWPFTableCell> cells = row.getTableCells();
                        for (XWPFTableCell cell : cells) {
                            if (cell != null){
                                //System.out.println(cell.getText());
                                for (XWPFParagraph p : cell.getParagraphs()) {
                                    for (XWPFRun run : p.getRuns()) {
                                        for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                            byte[] pictureData = pic.getPictureData().getData();
                                            Path path2 = Paths.get("C:\\projects\\upload\\upload_" + row.getCell(0).getText());
                                            Files.write(path2, pictureData);
                                            //System.out.println("picture : " + pictureData+" of Row Index"+ row.getCell(0).getText());
                                        }
                                    }
                                }
                            }
                        }*/
                    }
                    myObj.put("mcq",myArrObj);
                    System.out.println(myObj);
                }
            }
        } catch (Exception e){
            throw new IOException("Document Failed to Load");
        }
    }
}
