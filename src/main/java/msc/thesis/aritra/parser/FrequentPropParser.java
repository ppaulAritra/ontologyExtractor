package msc.thesis.aritra.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FrequentPropParser {
    public HashSet<Integer> parse(File rules) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(rules));
        String patternRegex = "^(\\d+)\\s+\\((\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)\\)$";
        String line;
        Pattern pattern = Pattern.compile(patternRegex);
        HashSet<Integer> properties = new HashSet<>();
        while ((line = in.readLine()) != null) {
            Matcher matcher = pattern.matcher(line.trim());
            boolean matches = matcher.matches();

            int propId = Integer.parseInt(matcher.group(1));
            Double support = Double.parseDouble(matcher.group(2));
            properties.add(propId);
        }
        in.close();
        return properties;
    }
}
