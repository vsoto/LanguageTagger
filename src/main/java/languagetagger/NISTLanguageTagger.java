package main.java.languagetagger;

import main.java.language_id.LanguageDetector;
import main.java.language_id.Result;
import main.java.Utils.Utils;

import com.aliasi.util.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class NISTLanguageTagger {

    static Logger log = Logger.getLogger(NISTLanguageTagger.class);
    private final static LanguageDetector lp = new LanguageDetector();

    private final HashSet<String> langAnchors;
    private final HashSet<String> engAnchors;
    private final String languageCode;

    NISTLanguageTagger(String languageCode) throws Exception {
        this.languageCode = languageCode;
        this.langAnchors = loadAnchors(languageCode, languageCode);
        this.engAnchors = loadAnchors(languageCode, "eng");
    }

    public void tag_directory(String dirIn, String dirOut) throws Exception {
        File dir = new File(dirIn);
        File[] listOfFiles = dir.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String filename = listOfFiles[i].getName();
                if (filename.endsWith(".txt")) {
                    tag_document_path(dirIn + "/" + filename, dirOut + "/" + filename);
                }
            }
        }
    }

    public void tag_document_path(String pathFileIn, String pathFileOut) throws Exception {
        File fileOut = new File(pathFileOut);
        //change permission to 777 for all the users
        fileOut.setExecutable(true, false);
        fileOut.setReadable(true, false);
        fileOut.setWritable(true, false);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));

        byte[] encoded = Files.readAllBytes(Paths.get(pathFileIn));
        String document_string = new String(encoded, StandardCharsets.UTF_8);
        JsonObject tagged_document_json = tag_document_string(document_string);
        
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(tagged_document_json);
        bw.write(prettyJson);
        bw.close();
    }

    public JsonObject tag_document_string(String document) {
        String[] lines = document.split("\n");

        String text = "";
        JsonArray list = new JsonArray();
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];
            if (!line.isEmpty()) {
                text += line + "\n";
                JsonObject jsonLine = tag_line(line);
                list.add(jsonLine);
            }
        }
        JsonObject jsonDoc = new JsonObject();
        jsonDoc.add("sentences", list);
        jsonDoc = outputHeaderDoc(jsonDoc, text);
        return jsonDoc;
    }

    public JsonObject tag_line(String line) {
        JsonObject sentenceJson = new JsonObject();
        JsonArray tokensJson = processLine(line);
        sentenceJson.add("items", tokensJson);
        Result res = lp.detectLanguage(line, this.languageCode);
        sentenceJson.addProperty("engine", res.engine);
        sentenceJson.addProperty("languageCode", res.languageCode);
        sentenceJson.addProperty("score", res.confidence);
        return sentenceJson;
    }

//    private void processTranscriptionFile(String path, String saveTo) throws Exception {
//        File newLangFile = new File(saveTo);
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newLangFile), StandardCharsets.UTF_8));
//
//        File contentFile = new File(path);
//        BufferedReader br = new BufferedReader(new FileReader(contentFile));
//
//        String line;
//
//        while ((line = br.readLine()) != null) {
//            if (line.startsWith("[")) {
//                bw.write(line + "\n");
//            } else {
//                String outputBlock = outputTranscriptionLine(line);
//                bw.write(outputBlock);
//            }
//        }
//        br.close();
//        bw.close();
//    }

    private HashSet<String> loadAnchors(String primaryLang, String langCode) throws Exception {
        HashSet<String> anchors = new HashSet<String>();
        String anchorsFilename = "weak_anchors/" + primaryLang + "/" + langCode + "_anchors.txt";
        InputStream is = NISTLanguageTagger.class.getClassLoader().getResourceAsStream(anchorsFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = "";
        while ((line = br.readLine()) != null) {
            anchors.add(line);
        }
        return anchors;
    }

    private JsonObject outputHeaderDoc(JsonObject doc, String text) {
        Result res = lp.detectLanguage(text, this.languageCode);
        doc.addProperty("engine", res.engine);
        doc.addProperty("languageCode", res.languageCode);
        doc.addProperty("score", res.confidence);
        return doc;
    }
    
//    private String outputHeaderDoc(String doc) {
//        Result res = lp.detectLanguage(doc, this.languageCode);
//        String output = "<doc " + (makeAttribute("engine", res.engine) + makeAttribute("languageCode", res.languageCode) + makeAttribute("score", String.valueOf(res.confidence))) + " > \n";
//        return output;
//    }

//    private static String outputEndDoc() {
//        return "</doc>\n";
//    }

//    private String outputTranscriptionLine(String line) {
//        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
//        String output;
//        if (line.contains("inLine") || line.contains("outLine")) {
//            int idx = line.indexOf("Line");
//            String prefix = line.substring(0, idx + 4);
//            String text = line.substring(idx + 4);
//            String untagged_text = text.replaceAll("<.*>", "");
//            Result res = lp.detectLanguage(untagged_text, this.languageCode);
//            output = prefix + " <s> " + processLine(text) + " </s " + (makeAttribute("engine", res.engine) + makeAttribute("languageCode", res.languageCode) + makeAttribute("score", String.valueOf(res.confidence))) + " > \n";
//        } else if (line.matches("[0-9]*\\.?[0-9]+.*")) {
//            output = line + "\n";
//        } else {
//            String untagged_text = line.replaceAll("<.*>", "");
//            Result res = lp.detectLanguage(untagged_text, this.languageCode);
//            output = "<s> " + processLine(line) + " </s " + (makeAttribute("engine", res.engine) + makeAttribute("languageCode", res.languageCode) + makeAttribute("score", String.valueOf(res.confidence))) + " > \n";
//        }
//        return output;
//    }

    private JsonArray processLine(String line) {
        String[] tokens = line.split(" ");
        JsonArray lineJson = new JsonArray();
        for (String token : tokens) {
            token = token.toLowerCase();
            JsonObject tokenJson = new JsonObject();
            tokenJson.addProperty("token", token);
            if (this.langAnchors.contains(token)) {
                tokenJson.addProperty("anchor", this.languageCode);
            } else if (this.engAnchors.contains(token)) {
                tokenJson.addProperty("anchor", "eng");
            }
            lineJson.add(tokenJson);
        }
        return lineJson;
    }

    private static String makeAttribute(String att, String value) {
        return att + "=\"" + value + "\" ";
    }

    public static void main(String[] args) throws Exception {
        NISTLanguageTagger lt = new NISTLanguageTagger(args[0]);
        lt.tag_directory(args[1], args[2]);
    }

}
