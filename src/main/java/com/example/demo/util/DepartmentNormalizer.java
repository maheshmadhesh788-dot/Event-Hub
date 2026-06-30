package com.example.demo.util;

import java.util.HashMap;
import java.util.Map;

public class DepartmentNormalizer {
    private static final Map<String, String> CLEANED_MAPPING = new HashMap<>();

    public static String clean(String dept) {
        if (dept == null) {
            return "";
        }
        String s = dept.toLowerCase();
        s = s.replace("&", "and");
        s = s.replace(".", "");
        s = s.replace("-", " ");
        s = s.replace("department of", "");
        s = s.replace("department", "");
        s = s.replace("dept", "");
        s = s.replaceAll("[^a-z0-9\\s]", "");
        s = s.replaceAll("\\s+", " ").trim();
        return s.toUpperCase();
    }

    static {
        // Information Technology
        String it = "Information Technology";
        CLEANED_MAPPING.put(clean("IT"), it);
        CLEANED_MAPPING.put(clean("INFORMATION TECHNOLOGY"), it);
        CLEANED_MAPPING.put(clean("INFORMATION TECHNOLOGY DEPARTMENT"), it);
        CLEANED_MAPPING.put(clean("IT DEPARTMENT"), it);
        CLEANED_MAPPING.put(clean("INFOTECH"), it);

        // Computer Science
        String cs = "Computer Science";
        CLEANED_MAPPING.put(clean("CS"), cs);
        CLEANED_MAPPING.put(clean("CSE"), cs);
        CLEANED_MAPPING.put(clean("COMPUTER SCIENCE"), cs);
        CLEANED_MAPPING.put(clean("COMPUTER SCIENCE DEPARTMENT"), cs);
        CLEANED_MAPPING.put(clean("CS DEPARTMENT"), cs);

        // Electronics and Communication Engineering
        String ece = "Electronics and Communication Engineering";
        CLEANED_MAPPING.put(clean("EC"), ece);
        CLEANED_MAPPING.put(clean("ECE"), ece);
        CLEANED_MAPPING.put(clean("ELECTRONICS AND COMMUNICATION"), ece);
        CLEANED_MAPPING.put(clean("ELECTRONICS & COMMUNICATION"), ece);
        CLEANED_MAPPING.put(clean("ELECTRONICS AND COMMUNICATION ENGINEERING"), ece);
        CLEANED_MAPPING.put(clean("ELECTRONICS AND COMMUNICATION DEPARTMENT"), ece);

        // Mathematics
        String math = "Mathematics";
        CLEANED_MAPPING.put(clean("MATH"), math);
        CLEANED_MAPPING.put(clean("MATHS"), math);
        CLEANED_MAPPING.put(clean("MATHEMATICS"), math);
        CLEANED_MAPPING.put(clean("MATHEMATICS DEPARTMENT"), math);

        // B.Sc Artificial intelligence and machine learning
        String aiml = "B.Sc Artificial intelligence and machine learning";
        CLEANED_MAPPING.put(clean("AI"), aiml);
        CLEANED_MAPPING.put(clean("AIML"), aiml);
        CLEANED_MAPPING.put(clean("ARTIFICIAL INTELLIGENCE"), aiml);
        CLEANED_MAPPING.put(clean("ARTIFICIAL INTELLIGENCE & MACHINE LEARNING"), aiml);
        CLEANED_MAPPING.put(clean("ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING"), aiml);
        CLEANED_MAPPING.put(clean("B.SC ARTIFICIAL INTELLIGENCE & MACHINE LEARNING"), aiml);
        CLEANED_MAPPING.put(clean("B.SC ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING"), aiml);
        CLEANED_MAPPING.put(clean("BSC ARTIFICIAL INTELLIGENCE & MACHINE LEARNING"), aiml);
        CLEANED_MAPPING.put(clean("BSC ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING"), aiml);
        CLEANED_MAPPING.put(clean("BSC AI"), aiml);
        CLEANED_MAPPING.put(clean("BSC AIML"), aiml);
        CLEANED_MAPPING.put(clean("B.SC AI"), aiml);
        CLEANED_MAPPING.put(clean("B.SC AIML"), aiml);

        // B.Sc Artificial intelligence and data science
        String aids = "B.Sc Artificial intelligence and data science";
        CLEANED_MAPPING.put(clean("AIDS"), aids);
        CLEANED_MAPPING.put(clean("ARTIFICIAL INTELLIGENCE & DATA SCIENCE"), aids);
        CLEANED_MAPPING.put(clean("ARTIFICIAL INTELLIGENCE AND DATA SCIENCE"), aids);
        CLEANED_MAPPING.put(clean("B.SC ARTIFICIAL INTELLIGENCE & DATA SCIENCE"), aids);
        CLEANED_MAPPING.put(clean("B.SC ARTIFICIAL INTELLIGENCE AND DATA SCIENCE"), aids);
        CLEANED_MAPPING.put(clean("BSC ARTIFICIAL INTELLIGENCE & DATA SCIENCE"), aids);
        CLEANED_MAPPING.put(clean("BSC ARTIFICIAL INTELLIGENCE AND DATA SCIENCE"), aids);
        CLEANED_MAPPING.put(clean("BSC AIDS"), aids);
        CLEANED_MAPPING.put(clean("B.SC AIDS"), aids);

        // Electrical and Electronics Engineering
        String eee = "Electrical and Electronics Engineering";
        CLEANED_MAPPING.put(clean("EEE"), eee);
        CLEANED_MAPPING.put(clean("ELECTRICAL AND ELECTRONICS ENGINEERING"), eee);
        CLEANED_MAPPING.put(clean("ELECTRICAL ELECTRONICS ENGINEERING"), eee);
        CLEANED_MAPPING.put(clean("ELECTRICAL AND ELECTRONICS"), eee);

        // Bachelor of Computer Applications
        String bca = "Bachelor of Computer Applications";
        CLEANED_MAPPING.put(clean("BCA"), bca);
        CLEANED_MAPPING.put(clean("BACHELOR OF COMPUTER APPLICATIONS"), bca);
        CLEANED_MAPPING.put(clean("COMPUTER APPLICATIONS"), bca);

        // Tamil Department
        String tamil = "Tamil Department";
        CLEANED_MAPPING.put(clean("TAMIL"), tamil);
        CLEANED_MAPPING.put(clean("TAMIL DEPARTMENT"), tamil);

        // Department of Zoology
        String zoology = "Department of Zoology";
        CLEANED_MAPPING.put(clean("ZOOLOGY"), zoology);
        CLEANED_MAPPING.put(clean("DEPARTMENT OF ZOOLOGY"), zoology);
        CLEANED_MAPPING.put(clean("ZOOLOGY DEPARTMENT"), zoology);

        // Department User
        String deptUser = "Department User";
        CLEANED_MAPPING.put(clean("DEPT1"), deptUser);
        CLEANED_MAPPING.put(clean("DEPARTMENT USER"), deptUser);
        CLEANED_MAPPING.put(clean("DEPT USER"), deptUser);
    }

    public static String normalize(String dept) {
        if (dept == null) {
            return null;
        }
        String cleaned = clean(dept);
        if (CLEANED_MAPPING.containsKey(cleaned)) {
            return CLEANED_MAPPING.get(cleaned);
        }
        return dept;
    }
}
