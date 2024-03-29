package msc.thesis.aritra.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssociationRulesParser {
    /**
     * <p/>
     * <ul> <li>C <- A1 (supp, conf)</li> <li>C <- A1 A2 (supp, conf)</li> </ul>
     *
     * @param rules      association rule file to parse
     * @param secondAnte if true, try to parse a second antecedent
     * @return list of all parsed axioms
     * @throws java.io.IOException on errors reading the association rule file
     */
    public List<ParsedRule> parse(File rules, boolean secondAnte) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(rules));
        String patternRegex;
        if (secondAnte) {
            patternRegex = "^(\\w+)\\s+<-\\s+(\\w+)\\s+(\\w+)\\s+\\((\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?),\\s+(\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)\\)$";
        } else {
            patternRegex = "^(\\w+)\\s+<-\\s+(\\w+)\\s+\\((\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?),\\s+(\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)\\)$";
        }
       //"^(\\d+)\\s+\\((\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)\\)$"
        String line;
        Pattern pattern = Pattern.compile(patternRegex);
        List<ParsedRule> axioms = new ArrayList<ParsedRule>();
        while ((line = in.readLine()) != null) {
            Matcher matcher = pattern.matcher(line.trim());
            boolean matches = matcher.matches();
            if(secondAnte) {
                if (!matches || matcher.groupCount() != 5) {
                    continue;
                }
            }
            else {
                if (!matches || matcher.groupCount() != 4) {
                    continue;
                }
            }
            String cons = matcher.group(1);
            String ante = matcher.group(2);

            int counter = 3;
            String ante2 = String.valueOf(-1);
            if (secondAnte) {
                ante2 = String.valueOf(matcher.group(counter++));
            }
            double supp = Double.parseDouble(matcher.group(counter++));
            double conf = Double.parseDouble(matcher.group(counter));
            if (secondAnte) {
                axioms.add(new ParsedRule(ante, ante2, cons, supp, conf));
            } else {
                //System.out.println(ante+" "+cons+" "+supp+" "+conf);
                axioms.add(new ParsedRule(ante, cons, supp, conf));
            }
        }
        in.close();
        return axioms;
    }

}
