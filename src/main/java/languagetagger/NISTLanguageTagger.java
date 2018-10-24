package main.java.languagetagger;

import main.java.language_id.LanguageDetector;
import main.java.language_id.Result;
import main.java.Utils.Triplet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.AbstractMap.SimpleEntry;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class NISTLanguageTagger {

    static Logger log = Logger.getLogger(NISTLanguageTagger.class);
    private final static LanguageDetector lp = new LanguageDetector();

    private final HashSet<String> langAnchors;
    private final HashSet<String> engAnchors;
    private final String languageCode;
    private final String nistCode;

    NISTLanguageTagger(String languageCode) throws Exception {
        this.languageCode = languageCode;
        this.nistCode = get_nist_code(this.languageCode);
        this.langAnchors = loadAnchors(languageCode, languageCode);
        this.engAnchors = loadAnchors(languageCode, "eng");
    }

    public static String get_nist_code(String languageCode) {
        switch (languageCode) {
            case "swa":
                return "1A";
            case "tgl":
                return "1B";
            case "som":
                return "1S";
            default:
                return "1X";
        }
    }

    private boolean create_directory(String dirPath) {
        if (Files.isDirectory(Paths.get(dirPath))) {
            return true;
        }
        File dir = new File(dirPath);
        boolean successful = dir.mkdir();
        return successful;
    }

    private void set_permissions(String dirPath) {
        //change permission to 777 for all the users
        File dir = new File(dirPath);
        dir.setExecutable(true, false);
        dir.setReadable(true, false);
        dir.setWritable(true, false);
    }

    private void write_report(String pathFileOut, ArrayList<Triplet<String, String, String>> results, boolean evalFlag) throws Exception {
        File fileOut = new File(pathFileOut);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));
        bw.write(this.nistCode + "\n");

        for (Triplet<String, String, String> result : results) {
            String docId = result.getFirst();
            String predictedLang = result.getSecond();
            String confidence = result.getThird();
            if (evalFlag) {
                if (predictedLang.equals(this.languageCode)) {
                    bw.write(docId + "\tY\t" + confidence + "\n");
                }
                else {
                    bw.write(docId + "\tN\t" + confidence + "\n");
                }
            } else {
                bw.write(docId + "\t" + predictedLang + "\t" + confidence + "\n");
            }
        }
        bw.close();
        //change permission to 777 for all the users
        fileOut.setExecutable(true, false);
        fileOut.setReadable(true, false);
        fileOut.setWritable(true, false);
    }

    private void write_output_reports(String dirOut, ArrayList<Triplet<String, String, String>> results) throws Exception {
        String reportDirPath = dirOut + "/report";
        if (!create_directory(reportDirPath)) {
            throw new Exception("Couldn't create report directory.");
        }
        set_permissions(reportDirPath);
        write_report(dirOut + "/report/l-" + this.nistCode + ".tsv", results, true);
        write_report(dirOut + "/report/results.log", results, false);
    }

    public void tag_directory(String dirIn, String dirOut) throws Exception {
        File dir = new File(dirIn);
        File[] listOfFiles = dir.listFiles();

        ArrayList<Triplet<String, String, String>> results = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String filename = listOfFiles[i].getName();
                String docId = filename.substring(0, filename.indexOf("."));
                Triple<String, Double, Double> result = verify_document_language(dirIn + "/" + filename, dirOut + "/" + filename);
                String predictedLang = result.getLeft();
                String targetLangConf = String.format("%.5f", result.getRight());
                results.add(new Triplet<>(docId, predictedLang, targetLangConf));
            }
        }
        write_output_reports(dirOut, results);
    }

    public Triple<String, Double, Double> verify_document_language(String pathFileIn, String pathFileOut) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(pathFileIn));
        String document_string = new String(encoded, StandardCharsets.UTF_8);
        
        Result docResult = tag_document_string(document_string, pathFileOut);
        String predictedLang = docResult.predLangCode;
        Double predLangConf = docResult.predLangConf;
        Double targetConf = docResult.targetLangConf;
        return new Triple<>(predictedLang, predLangConf, targetConf);
    }

    public Result tag_document_string(String document, String pathFileOut) throws Exception {
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
        Result docResult = outputHeaderDoc(jsonDoc, text);
        
        jsonDoc.addProperty("engine", docResult.engine);
        jsonDoc.addProperty("languageCode", docResult.predLangCode);
        jsonDoc.addProperty("score", docResult.predLangConf);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(jsonDoc);

        File fileOut = new File(pathFileOut);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));
        bw.write(prettyJson);
        bw.close();
        //change permission to 777 for all the users
        fileOut.setExecutable(true, false);
        fileOut.setReadable(true, false);
        fileOut.setWritable(true, false);
        
        return docResult;
    }

    public JsonObject tag_line(String line) {
        JsonObject sentenceJson = new JsonObject();
        JsonArray tokensJson = processLine(line);
        sentenceJson.add("items", tokensJson);
        Result res = lp.detectLanguage(line, this.languageCode);
        sentenceJson.addProperty("engine", res.engine);
        sentenceJson.addProperty("languageCode", res.predLangCode);
        sentenceJson.addProperty("score", res.predLangConf);
        return sentenceJson;
    }

    private HashSet<String> loadAnchors(String primaryLang, String langCode) throws Exception {
        HashSet<String> anchors = new HashSet<String>();
        String anchorsFilename = "weak_anchors/" + primaryLang + "/" + langCode + "_anchors.txt";
        InputStream is = NISTLanguageTagger.class.getClassLoader().getResourceAsStream(anchorsFilename);
        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = br.readLine()) != null) {
                anchors.add(line);
            }
        } else {
            log.info("Couldn't find anchors file " + anchorsFilename);
        }
        return anchors;
    }

    private Result outputHeaderDoc(JsonObject doc, String text) {
        Result res = lp.detectLanguage(text, this.languageCode);
        return res;
    }

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

    public static void main(String[] args) throws Exception {
        NISTLanguageTagger lt = new NISTLanguageTagger(args[0]);
        lt.tag_directory(args[1], args[2]);
    }

}
