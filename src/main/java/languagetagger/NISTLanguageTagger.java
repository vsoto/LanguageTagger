package main.java.languagetagger;

import main.java.language_id.LanguageDetector;
import main.java.language_id.Result;
import main.java.Utils.Utils;

import com.aliasi.util.Strings;
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

/**
 * Created by Gideon on 2/24/15.
 */
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
        // System.out.println(dirIn);
        File[] listOfFiles = dir.listFiles();
        // System.out.println(listOfFiles);

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String filename = listOfFiles[i].getName();
                // System.out.println(filename);
                if (filename.endsWith(".txt")) {
                    tag_document_path(dirIn + "/" + filename, dirOut + "/" + filename);
                }
            }
        }
    }

    public void tag_document_path(String pathFileIn, String pathFileOut) throws Exception {
        File fileOut = new File(pathFileOut);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));

        // File contentFile = new File(pathFileIn);
        // BufferedReader br = new BufferedReader(new FileReader(contentFile));
        byte[] encoded = Files.readAllBytes(Paths.get(pathFileIn));
        String document_string = new String(encoded, StandardCharsets.UTF_8);
        String tagged_document_string = tag_document_string(document_string);

        bw.write(tagged_document_string);
        bw.close();

//        String line;
//
//        String outputBlock = "";
//        String text = "";
//        while ((line = br.readLine()) != null) {
//            if (!line.isEmpty()) {
//                text += line + "\n";
//                outputBlock += outputTaggingLine(line);
//            }
//        }
//        bw.write(outputTaggingDoc(text));
//        bw.write(outputBlock);
//        bw.write(outputEndDoc());
//
//        br.close();
//        bw.close();
    }

    public String tag_document_string(String document) {
        String[] lines = document.split("\n");

        String outputBlock = "";
        String text = "";
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];
            if (!line.isEmpty()) {
                text += line + "\n";
                outputBlock += tag_line(line);
            }
        }
        String json_document;
        json_document = outputHeaderDoc(text);
        json_document += outputBlock;
        json_document += outputEndDoc();
        return json_document;

    }

    public String tag_line(String line){
        Result res = lp.detectLanguage(line, this.languageCode);
        String output = "<s> " + processLine(line) + " </s " + (makeAttribute("engine", res.engine) + makeAttribute("languageCode", res.languageCode) + makeAttribute("score", String.valueOf(res.confidence))) + " > \n";
        return output;
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

//    private String outputTaggingLines(ArrayList<String> lines) {
//        String output = "";
//        for (String line : lines) {
//            output += outputTaggingLine(line);
//        }
//        return output;
//    }

    private String outputHeaderDoc(String doc) {
        Result res = lp.detectLanguage(doc, this.languageCode);
        String output = "<doc " + (makeAttribute("engine", res.engine) + makeAttribute("languageCode", res.languageCode) + makeAttribute("score", String.valueOf(res.confidence))) + " > \n";
        return output;
    }

    private static String outputEndDoc() {
        return "</doc>\n";
    }

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

    public String processLine(String line) {
        String[] tokens = line.split(" ");
        String newLine = "";
        for (String token : tokens) {
            token = token.toLowerCase();
            if (this.langAnchors.contains(token)) {
                newLine += "<" + token + ":" + this.languageCode + "> ";
            } else if (this.engAnchors.contains(token)) {
                newLine += "<" + token + ":eng> ";
            } else {
                newLine += token + " ";
            }
        }
        return newLine;
    }

//    public static int countWords(String s) {
//        int wordCount = 0;
//        boolean word = false;
//        int endOfLine = s.length() - 1;
//
//        for (int i = 0; i < s.length(); i++) {
//            // if the char is a letter, word = true.
//            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
//                word = true;
//                // if char isn't a letter and there have been letters before,
//                // counter goes up.
//            } else if (!Character.isLetter(s.charAt(i)) && word) {
//                wordCount++;
//                word = false;
//                // last word of String; if it doesn't end with a non letter, it
//                // wouldn't count without this.
//            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
//                wordCount++;
//            }
//        }
//        return wordCount;
//    }

    private static String makeAttribute(String att, String value) {
        return att + "=\"" + value + "\" ";
    }

    public static void main(String[] args) throws Exception {
        NISTLanguageTagger lt = new NISTLanguageTagger(args[0]);
        lt.tag_directory(args[1], args[2]);
    }

}
