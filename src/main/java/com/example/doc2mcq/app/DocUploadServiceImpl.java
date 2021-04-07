package com.example.doc2mcq.app;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlbeans.XmlCursor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.poi.xwpf.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMath;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Integer.parseInt;

@Service
public class DocUploadServiceImpl {

    public String uploadToLocal(MultipartFile file) throws IOException {
        try{
            byte[] data = file.getBytes();
            Path path = Paths.get("sampleDocs\\uploads\\upload_" + file.getOriginalFilename());
            Files.write(path, data);
            XWPFDocument docx = new XWPFDocument(new FileInputStream("sampleDocs\\uploads\\upload_"+file.getOriginalFilename()));

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

            //using a StringBuffer for appending all the content as HTML
            StringBuffer allHTML = new StringBuffer();

            //To make the HTML body from docx
            for (IBodyElement ibodyelement : docx.getBodyElements()) {
                if (ibodyelement instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable)ibodyelement;
                    allHTML.append("<table border=\"1px solid black\">");
                    for (XWPFTableRow row : table.getRows()) {
                        allHTML.append("<tr>");
                        for (XWPFTableCell cell : row.getTableCells()) {
                            allHTML.append("<td>");
                            for (IBodyElement cellBodyElement : cell.getBodyElements()) {
                                if (cellBodyElement instanceof XWPFParagraph) {
                                    XWPFParagraph paragraph = (XWPFParagraph)cellBodyElement;
                                    allHTML.append("<p>");
                                    allHTML.append(getTextImagesAndFormulas(paragraph));
                                    allHTML.append("</p>");
                                }
                                else if (cellBodyElement instanceof XWPFTable) {
                                    XWPFTable table3 = (XWPFTable)cellBodyElement;
                                    allHTML.append("<table border=\"1px solid black\">");
                                    for (XWPFTableRow row3 : table3.getRows()) {
                                        allHTML.append("<tr>");
                                        for (XWPFTableCell cell3 : row3.getTableCells()) {
                                            allHTML.append("<td>");
                                            for (XWPFParagraph paragraph3 : cell3.getParagraphs()) {
                                                allHTML.append("<p>");
                                                allHTML.append(getTextImagesAndFormulas(paragraph3));
                                                allHTML.append("</p>");
                                            }
                                            allHTML.append("</td>");
                                        }
                                        allHTML.append("</tr>");
                                    }
                                    allHTML.append("</table>");
                                } else {
                                    break;
                                }
                            }
                            allHTML.append("</td>");
                        }
                        allHTML.append("</tr>");
                    }
                    allHTML.append("</table>");
                }
            }

            //MERGE Base64 IMAGES
            List<XWPFPictureData> list = docx.getAllPictures();
            String finalHTML = allHTML.toString();
            finalHTML = setImg(finalHTML, list);

            Document doc2 = Jsoup.parse(finalHTML);
            Element table2 = doc2.select("table").get(0);
            Elements rows2 = table2.select("tr");

            // Fetch DOC BODY Data
            JSONArray myArrObj = new JSONArray();
            for (IBodyElement bodyElement : docx.getBodyElements()) {
                if (bodyElement instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) bodyElement;
                    for (XWPFTableRow row : table.getRows()) {
                        //mcq ArrayObject
                        JSONObject obj = new JSONObject();
                        obj.put("quesNo",row.getCell(0).getText());
                        obj.put("answer",row.getCell(3).getText());

                        //questionCell object
                        int i = parseInt(row.getCell(0).getText())-1;
                        Element row2 = rows2.get(i);
                        String str2 = row2.select("td:eq(1)").toString();
                        obj.put("questionCell",str2);

                        //options Object
                        int j = parseInt(row.getCell(0).getText())-1;
                        Element row3 = rows2.get(j);
                        String str3 = row3.select("td:eq(2)").toString();
                        obj.put("optionsCell",str3);

                        myArrObj.put(obj);
                    }
                    myObj.put("mcq",myArrObj);
                }
            }

            //creating a sample HTML file
            String encoding = "UTF-8";
            FileOutputStream fos = new FileOutputStream("sampleDocs\\result.html");
            OutputStreamWriter writer = new OutputStreamWriter(fos, encoding);
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"en\">");
            writer.write("<head>");
            writer.write("<meta charset=\"utf-8\"/>");

            //using MathJax for helping all browsers to interpret MathML
            writer.write("<script type=\"text/javascript\"");
            writer.write(" async src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=MML_CHTML\"");
            writer.write(">");
            writer.write("</script>");

            writer.write("</head>");
            writer.write("<body>");

            writer.write(finalHTML);

            writer.write("</body>");
            writer.write("</html>");
            writer.close();

            return JSONObject.valueToString(myObj);
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

    //method for getting MathML from oMathML
    static String getMathML(CTOMath ctomath) throws Exception {
        //MATHML CODE
        File stylesheet = new File("OMML2MML.XSL");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = tFactory.newTransformer(stylesource);
        Node node = ctomath.getDomNode();
        DOMSource source = new DOMSource(node);
        StringWriter stringwriter = new StringWriter();
        StreamResult result = new StreamResult(stringwriter);
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        transformer.transform(source, result);
        String mathML = stringwriter.toString();
        stringwriter.close();
        mathML = mathML.replaceAll("xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"", "");
        mathML = mathML.replaceAll("xmlns:mml", "xmlns");
        mathML = mathML.replaceAll("mml:", "");
        return mathML;
    }

    public static float EmuToPixels(int emu)
    {
        float val = emu != 0 ? (float) emu / 12700 : 0;
        return val;
    }


    //method for getting HTML including MathML, Images and Table from cell (XWPFParagraph)
    static String getTextImagesAndFormulas(XWPFParagraph paragraph) throws Exception {
        StringBuffer cellData = new StringBuffer();

        //using a cursor to go through the paragraph from top to down
        XmlCursor xmlcursor = paragraph.getCTP().newCursor();
        while (xmlcursor.hasNextToken()) {
            XmlCursor.TokenType tokentype = xmlcursor.toNextToken();
            if (tokentype.isStart()) {
                if (xmlcursor.getName().getPrefix().equalsIgnoreCase("w") && xmlcursor.getName().getLocalPart().equalsIgnoreCase("r")) {
                    //elements w:r are text runs within the paragraph
                    //append text data
                    cellData.append(xmlcursor.getTextValue());
                } else if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("oMath")) {
                    //append the oMath as MathML
                    cellData.append(getMathML((CTOMath)xmlcursor.getObject()));
                } else if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("pic")) {
                    //append image element
                    String filename = CTPicture.Factory.parse(xmlcursor.getDomNode().getFirstChild()).getNvPicPr().getCNvPr().getName();
                    String width = EmuToPixels((int) CTPicture.Factory.parse(xmlcursor.getDomNode().getLastChild()).getSpPr().getXfrm().getExt().getCx())+"pt";
                    String height = EmuToPixels((int) CTPicture.Factory.parse(xmlcursor.getDomNode().getLastChild()).getSpPr().getXfrm().getExt().getCy())+"pt";
                    cellData.append("<img src="+filename+" width="+width+" height="+height+" />");
                }
            } else if (tokentype.isEnd()) {
                //we have to check whether we are at the end of the paragraph
                xmlcursor.push();
                xmlcursor.toParent();
                if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("p")) {
                    break;
                }
                xmlcursor.pop();
            }
        }

        return cellData.toString();
    }

}