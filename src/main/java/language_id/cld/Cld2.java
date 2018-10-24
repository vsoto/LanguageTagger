package main.java.language_id.cld;/*
 * Copyright 2014-present Deezer.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific languageCode governing permissions and
 * limitations under the License.
 */


import main.java.language_id.*;
import main.java.language_id.Result;

import java.io.IOException;

/**
 * Public interface for the CLD2 library.
 */
public class Cld2 extends LanguageClassifier {

    public Cld2() throws IOException, ClassNotFoundException {
        this.detectLanguage("test that will throw an error if fails to load cld", "en");
        String[] listOfLangs = {"Afrikaans", "Albanian", "Arabic", "Armenian", "Azerbaijani", "Basque", "Belarusian", "Bengali", "Bihari languages", "Bulgarian", "Catalan", "Cebuano", "Cherokee", "Croatian", "Czech", "Chinese", "Danish", "Dhivehi", "Dutch", "English", "Estonian", "Finnish", "French", "Galician", "Ganda", "Georgian", "German", "Modern Greek", "Gujarati", "Haitian", "Hebrew", "Hindi", "Hmong", "Hungarian", "Icelandic", "Indonesian", "Inuktitut", "Irish", "Italian", "Javanese", "Japanese", "Kannada", "Central Khmer", "Kinyarwanda", "Korean", "Lao", "Latvian", "Lithuanian", "Macedonian", "Malay", "Malayalam", "Maltese", "Marathi", "Nepali", "Norwegian", "Oriya", "Persian", "Polish", "Portuguese", "Panjabi", "Romanian", "Russian", "Scottish Gaelic", "Serbian", "Sinhala", "Slovak", "Slovene", "Somali", "Spanish", "Swahili", "Swedish", "Syriac", "Tagalog", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Vietnamese", "Welsh", "Yiddish"};
        buildListOfSupportedLanguageCodesFromLanguageNames(listOfLangs);

    }

    public static int getLanguageFromName(String name) {
        return Cld2Library.INSTANCE._ZN4CLD219GetLanguageFromNameEPKc(name);
    }

    public static String getLanguageName(int language) {
        return Cld2Library.INSTANCE._ZN4CLD212LanguageNameENS_8LanguageE(language);
    }

    public static String getLanguageCode(int language) {
        return Cld2Library.INSTANCE._ZN4CLD212LanguageCodeENS_8LanguageE(language);
    }

    public static String version() {
        return Cld2Library.INSTANCE._ZN4CLD221DetectLanguageVersionEv();
    }
    
    private static int translateLangCode2CLD2(String targetLangCode) {
        int cld2Code = 0;
        switch (targetLangCode) {
            case "eng":
                 cld2Code = Language.ENGLISH;
                 break;
            case "tgl":
                 cld2Code = Language.TAGALOG;
                 break;
            case "swa":
                 cld2Code = Language.SWAHILI;
                 break;
            case "som":
                 cld2Code = Language.SOMALI;
                 break;
            default:
                 cld2Code = Language.UNKNOWN_LANGUAGE;
                 break;
                
        }
        return cld2Code;
    }

    public static Result detect(String text, String targetLangCode) throws IOException, ClassNotFoundException {
        boolean isPlainText = true;

        CLDHints cldHints = new CLDHints(
                null,
                "",
                Encoding.UNKNOWN_ENCODING,
                translateLangCode2CLD2(targetLangCode));
                // Language.UNKNOWN_LANGUAGE);

        int flags = 0;
        int[] language3 = new int[10];
        int[] percent3 = new int[10];
        double[] normalizedScore3 = new double[10];
        int[] textBytes = new int[1];
        boolean[] isReliable = new boolean[1];
        byte[] utf8EncodedText;
        try {
            utf8EncodedText = text.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException exc) {
            return new Result(null, false, 0.0, 0.0, "cld2");

        }
        int language = Cld2Library.INSTANCE._ZN4CLD224ExtDetectLanguageSummaryEPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_Pb(
                utf8EncodedText,
                utf8EncodedText.length,
                isPlainText,
                cldHints,
                flags,
                language3,
                percent3,
                normalizedScore3,
                null, // Supposed to be a vector of ResultChunks, but it is not direct to pass vectors.
                textBytes,
                isReliable);

        LanguageCode lc = new LanguageCode(getLanguageCode(language), LanguageCode.CodeTypes.ISO_639_1);
        String predLangCode= lc.getLanguageCode();
        if (predLangCode != null && predLangCode.equals("swh")) {
            predLangCode = "swa";
        }
        double predLangConf = percent3[0] / 100.0;
        double targetLangConf = 0.0;
        if (targetLangCode.equals(predLangCode)) {
            targetLangConf = predLangConf;
        } else {
            LanguageCode lc1 = new LanguageCode(getLanguageCode(language3[1]), LanguageCode.CodeTypes.ISO_639_1);
            LanguageCode lc2 = new LanguageCode(getLanguageCode(language3[2]), LanguageCode.CodeTypes.ISO_639_1);
            String lang1 = lc1.getLanguageCode();
            String lang2 = lc2.getLanguageCode();
            
            System.err.println(language3[1] + "\t" + percent3[1] / 100.0);
            System.err.println(language3[2] + "\t" + percent3[2] / 100.0);
            
            if (targetLangCode.equals(lang1)) {
                targetLangConf = percent3[1] / 100.0;
            } else if (targetLangCode.equals(lang2)) {
                targetLangConf = percent3[2] / 100.0;
            }
        }
        return new Result(predLangCode, isReliable[0], predLangConf, targetLangConf, "cld2");

    }

    @Override
    public Result detectLanguage(String text, String targetLangCode) throws IOException, ClassNotFoundException {
        return Cld2.detect(text, targetLangCode);
    }
}
