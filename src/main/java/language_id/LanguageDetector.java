package main.java.language_id;

import main.java.language_id.cld.Cld2;
import main.java.language_id.lingpipe.LingPipe;
import main.java.language_id.textcat.TextCategorizer;

import main.java.Utils.Utils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by Gideon on 9/4/15.
 */
/**
 * Language detection API that manages all supported classifiers
 */
public class LanguageDetector {

    /* LingPipe classifier */
    LingPipe lp = new LingPipe("completeModel3.gm");
    /* CLD2 Classifier */
    Cld2 cld = null;

    /* TextCat Classifier */
    TextCategorizer tc = null;
    private ArrayList<String> lpLangs = new ArrayList<String>();
    Logger log = Logger.getLogger(LanguageDetector.class);


    /* Constructor */
    public LanguageDetector() {
        tc = new TextCategorizer();
        try {
            cld = new Cld2();
        } catch (IOException e) {
            log.error(e);
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (Error e) {
            log.error("Failed to load CLD. Continuing using LP language classifier. ");
            log.error(e);
        }

    }

    /**
     * Preforms language detection using majority vote approach over all
     * classifiers If CLD2 is not installed than fallback to standard language
     * detection detectLanguage()
     *
     * @param text to preform language detection on
     * @param lang what language is this text should be in? Used to pick
     * classifiers
     * @return Classification results
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Result detectMajorityVote(String text, String target_lang) throws IOException, ClassNotFoundException {

        if (cld == null) { //if we can't load CLD no point in doing majority vote
            return detectLanguage(text, target_lang);
        }
        
        ArrayList<Result> results = new ArrayList<>();
        LanguageCode target_code = new LanguageCode(target_lang, LanguageCode.CodeTypes.ISO_639_2);

        if (lp.getSupportedLanguages().contains(target_code) || (target_code.getLanguageCode().equals("swa") && lp.getSupportedLanguages().contains(new LanguageCode("swh", LanguageCode.CodeTypes.ISO_639_2)))) {
            Result pred = lp.detectLanguage(text, target_code.getLanguageCode());
            System.err.println("LP");
            System.err.println(pred);
            results.add(pred);
        }
        
        if (tc.getSupportedLanguages().contains(target_code) || (target_code.getLanguageCode().equals("swa") && tc.getSupportedLanguages().contains(new LanguageCode("swh", LanguageCode.CodeTypes.ISO_639_2)))) {
            System.err.println("TC");
            Result pred = tc.detectLanguage(text, target_code.getLanguageCode());
            System.err.println(pred);
            results.add(pred);
        }
        
        if (cld.getSupportedLanguages().contains(target_code) || (target_code.getLanguageCode().equals("swa") && cld.getSupportedLanguages().contains(new LanguageCode("swh", LanguageCode.CodeTypes.ISO_639_2)))) {
            Result pred = cld.detectLanguage(text, target_code.getLanguageCode());
            System.err.println("CLD");
            System.err.println(pred);
            results.add(pred);
        }
        

        Result res = mostCommon(results);
        if (res == null) {
            return new Result(null, false, 0, 0);
        } else {
            return res;
        }

    }

    /**
     * Preforms language detection with the best available classifier (based on
     * measured accuracy)
     *
     * @param text to preform language detection on
     * @param lang what language is this text should be in? Used to pick
     * classifiers
     * @return Classification results
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Result detectHierarchy(String text, String lang) throws IOException, ClassNotFoundException {
        if (text == null) {
            return null;
        }

        LanguageCode code = new LanguageCode(lang, LanguageCode.CodeTypes.ISO_639_2);

        if (cld != null && cld.getSupportedLanguages().contains(code) || (code.getLanguageCode().equals("swa") && cld.getSupportedLanguages().contains(new LanguageCode("swh", LanguageCode.CodeTypes.ISO_639_2)))) {
            return cld.detectLanguage(text, code.getLanguageCode());
        } else if (lp.getSupportedLanguages().contains(code) || (code.getLanguageCode().equals("swa") && lp.getSupportedLanguages().contains(new LanguageCode("swh", LanguageCode.CodeTypes.ISO_639_2)))) {
            return lp.detectLanguage(text, code.getLanguageCode());
        } else if (tc.getSupportedLanguages().contains(code) || (code.getLanguageCode().equals("swa") && tc.getSupportedLanguages().contains(new LanguageCode("swh", LanguageCode.CodeTypes.ISO_639_2)))) {
            return tc.detectLanguage(text, code.getLanguageCode());
        } else {
            log.info("Language: " + lang + " not supported!");
        }

        return null;

    }

    /**
     * Main entry point for identification. Will choose approach based on
     * configuration provided in config file
     *
     * @param text to preform language detection on
     * @param lang what language is this text should be in? Used to pick
     * classifiers
     * @return Classification results
     */
    public Result detectLanguage(String text, String lang) {
        try {
            if (cld != null) {
                return detectMajorityVote(text, lang);
            } else {
                return detectHierarchy(text, lang);
            }

        } catch (NullPointerException np) {
            log.error(np);
            return null;
        } catch (Exception e) {
            log.error("Can't run language id - > Shutting down!");
            log.error(e);
            System.exit(0);
        }
        return null;
    }
    

    private Result mostCommon(List<Result> list) {
        if (list == null || list.size() == 0) {
            return null;
        }

        if (list.size() == 1) {
            return list.get(0);
        }

        Map<Result, Integer> map_count = new HashMap<>();
        Map<String, Double> map_conf = new HashMap<>();
        Result maxScoring = null;
        double max_confidence_score = -10.0;
        for (Result t : list) {
            Integer val = map_count.get(t);
            map_count.put(t, val == null ? 1 : val + 1);
            Double conf = map_conf.get(t.predLangCode);
            map_conf.put(t.predLangCode, conf == null ? t.predLangConf : conf + t.predLangConf);
            if (t.predLangConf > max_confidence_score) {
                max_confidence_score = t.predLangConf;
                maxScoring = t;
            }
        }
        
        // If sizes are the same it means that all the values in list are unique
        if (map_count.size() == list.size()) {  
            return maxScoring;
        }

        Map.Entry<Result, Integer> max = null;
        for (Map.Entry<Result, Integer> e : map_count.entrySet()) {
            if (max == null || e.getValue() > max.getValue()) {
                max = e;
            }
        }

        Result maj_vote = max.getKey();

        double average_confidence = map_conf.get(maj_vote.predLangCode) / max.getValue();
        //int count = max.getValue(); 
        //for (Result t: list) {
        //   if (t.languageCode.equals(maj_vote.languageCode)){
        //   	average_confidence += t.confidence;
        //   }
        //}
        //average_confidence /= count;

        maj_vote.engine = "maj_vote";
        maj_vote.predLangConf = average_confidence;
        return maj_vote;
    }
}
