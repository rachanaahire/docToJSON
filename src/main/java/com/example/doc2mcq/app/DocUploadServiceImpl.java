package com.example.doc2mcq.app;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Integer.parseInt;

@Service
public class DocUploadServiceImpl implements DocUploadService {

    @Override
    public String uploadToLocal(MultipartFile file) throws IOException {
        try{
            byte[] data = file.getBytes();
            Path path = Paths.get("C:\\projects\\upload\\upload_" + file.getOriginalFilename());
            Files.write(path, data);
            XWPFDocument docx = new XWPFDocument(new FileInputStream("C:\\projects\\upload\\upload_"+file.getOriginalFilename()));

            List<XWPFPictureData> list = docx.getAllPictures();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XHTMLConverter.getInstance().convert(docx, outputStream, null);
            String s = outputStream.toString();
            s = setImg(s, list);
            //System.out.println(s);

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

            //BASE64 LOGIC
            Document doc2 = Jsoup.parse(s);
            Element table2 = doc2.select("table").get(0);
            Elements rows2 = table2.select("tr");

            // Fetch DOC BODY Data
            List<IBodyElement> bodyElements = docx.getBodyElements();
            JSONArray myArrObj = new JSONArray();
            for (IBodyElement bodyElement : bodyElements) {
                if (bodyElement instanceof XWPFTable) {
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

                        //BASE64 LOGIC
                        int i = parseInt(row.getCell(0).getText())-1;
                        Element row2 = rows2.get(i);
                        String str2 = row2.select("td:eq(1)").toString();
                        obj.put("questionCell",str2);

                        myArrObj.put(obj);
                    }
                    myObj.put("mcq",myArrObj);
                    System.out.println(myObj);
                }
            } return JSONObject.valueToString(myObj);
        } catch (Exception e){
            throw new IOException("Document Failed to Load");
        }
    }

    private String setImg(String html, List<XWPFPictureData> list) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("img");
        if (elements != null && elements.size() > 0 && list != null){
            for(Element element : elements){
                String src = element.attr("src");
                for (XWPFPictureData data: list){
                    if (src.contains(data.getFileName())){
                        String type = src.substring(src.lastIndexOf(".") + 1);
                        String base64 = "data:image/" + type + ";base64," + new String(Base64.encodeBase64(data.getData()));
                        element.attr("src", base64);
                        break;
                    }
                }
            }
        }
        return doc.toString();
    }

}
