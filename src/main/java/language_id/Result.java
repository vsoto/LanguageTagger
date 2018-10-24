package main.java.language_id;

/**
 * An languageCode detection test result
 */
public class Result {

    public Result(String predLangCode, boolean isReliable, double predLangConf,
            double targetLangConf ) {

        if(predLangCode == null){
            this.predLangCode = "UNKNOWN";
            this.isReliable = false;
            this.predLangConf = 0;
        }
        else {
            this.predLangCode = predLangCode;
            this.isReliable = isReliable;
            this.predLangConf = predLangConf;
        }
        this.targetLangConf = targetLangConf;
    }

    public Result(String predLangCode, boolean isReliable, double predLangConf,
            double targetLangConf, String engine) {
	this(predLangCode, isReliable, predLangConf, targetLangConf);
	if (engine == null) {
	   engine = "UNKNOWN";
	}
	this.engine = engine;
    }

    public String predLangCode;
    public String engine;
    public boolean isReliable;
    public double predLangConf;
    public double targetLangConf;

    public String getLanguageName(){
        return com.neovisionaries.i18n.LanguageAlpha3Code.getByCode(this.predLangCode).getName();
    }

    @Override
    public String toString() {
        return "Result{" +
                "languageCode='" + this.predLangCode + '\'' +
                ", isReliable=" + this.isReliable +
                ", confidence=" + this.predLangConf +
		", engine=" + this.engine +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;

        Result result = (Result) o;
        return predLangCode.equals(result.predLangCode);
    }

    @Override
    public int hashCode() {
        return predLangCode.hashCode();
    }
}
