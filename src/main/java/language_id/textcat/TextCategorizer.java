package main.java.language_id.textcat;

import main.java.language_id.LanguageClassifier;
import main.java.language_id.LanguageCode;
import main.java.language_id.Result;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * TextCategorizer is able to categorize texts by computing the similarity of
 * the FingerPrint of a text with a collection of the FingerPrints of the
 * categories.
 *
 */
public class TextCategorizer extends LanguageClassifier {

    private final static int UNKNOWN_LIMIT = 1;

    private final String jarConfFile = "languageData/language_fp/textcat.conf";

    private ArrayList<FingerPrint> categories = new ArrayList<FingerPrint>();

    public TextCategorizer() {
        loadCategories();
    }

    @Override
    public Result detectLanguage(String text, String targetLangCode) throws IOException, ClassNotFoundException {
        if (text.length() < UNKNOWN_LIMIT) {
            return new Result("unknown", true, 0.0, 0.0, "textcat");
        }
        FingerPrint fp = new FingerPrint();
        fp.create(text);
        fp.categorize(categories, targetLangCode);
        String predLangCode = fp.getCategory();
        if (predLangCode != null && predLangCode.equals("swh")) {
            predLangCode = "swa";
        }
        double predLangConf = fp.getConfidence();
        double targetLangConf = 0.0;
        if (targetLangCode.equals(predLangCode)) {
            targetLangConf = predLangConf;
        } else {
            targetLangConf = fp.getTargetLangConfidence();
        }
        
        return new Result(predLangCode, true, predLangConf, targetLangConf, "textcat");
    }

    /**
     * creates a new TextCategorizer with the given configuration file. the
     * configuration file maps paths to FingerPrint files to categories which
     * are used to categorize the texts passed to the TextCategorizer.
     *
     * @param confFile the path to the configuration file
     */
//	public TextCategorizer(InputStream confFile) {
//		setConfFile(confFile);
//	}
    /**
     * sets the configuration file path.
     *
     * @param confFile the path to the configuration file
     */
//	public void setConfFile(InputStream confFile) {
//		loadCategories();
//	}
    /**
     * clears the categories-collection and fills it with the FingerPrints given
     * in the configuration file.
     */
    private void loadCategories() {
        this.categories.clear();
        Logger log = Logger.getLogger(TextCategorizer.class);
        // try {
        MyProperties properties = new MyProperties();
        //if (confFile == null) {
        properties.load(TextCategorizer.class.getClassLoader()
                .getResourceAsStream(jarConfFile));
			//} else {
        //	properties.load(new FileInputStream(confFile.toString()));
        //}
        for (Entry<String, String> entry : properties.entrySet()) {
            FingerPrint fp;
            //if (confFile == null) {
            fp = new FingerPrint(TextCategorizer.class.getClassLoader()
                    .getResourceAsStream(entry.getKey()));
				//} else {
            //	fp = new FingerPrint(BabelConfig.getResourcesPath() + entry.getKey());
            //}
            fp.setCategory(entry.getValue());
            this.categories.add(fp);
            this.supportedLanguages.add(new LanguageCode(entry.getValue(), LanguageCode.CodeTypes.ISO_639_2));
        }
//		} catch (FileNotFoundException fnfe) {
//			log.error(fnfe);
//		}
    }

    /**
     * categorizes the text passed to it
     *
     * @param text text to be categorized
     * @return the category name given in the configuration file
     */
    public String categorize(String text) {
        if (text.length() < UNKNOWN_LIMIT) {
            return "unknown";
        }
        FingerPrint fp = new FingerPrint();
        fp.create(text);
        fp.categorize(categories);
        return fp.getCategory();
    }

    /**
     * categorizes only a certain amount of characters in the text. recommended
     * when categorizing large texts in order to increase performance.
     *
     * @param text text to be analysed
     * @param limit number of characters to be analysed
     * @return the category name given in the configuration file
     */
    public String categorize(String text, int limit) {
        if (limit > (text.length() - 1)) {
            return this.categorize(text);
        }
        return this.categorize(text.substring(0, limit));
    }

    /**
     * categorizes a text but returns a map containing all categories and their
     * distances to the text.
     *
     * @param text text to be categorized
     * @return HashMap with categories as keys and distances as values
     */
    public Map<String, Integer> getCategoryDistances(String text) {
        if (this.categories.isEmpty()) {
            loadCategories();
        }
        FingerPrint fp = new FingerPrint();
        fp.create(text);
        fp.categorize(categories);
        return fp.getCategoryDistances();
    }

}
