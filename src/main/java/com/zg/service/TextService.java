package com.zg.service;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guang.zhang on 2017/8/15.
 */
@Service
public class TextService {

    private double doCompare(String s1, String s2){

        Result result1 = NlpAnalysis.parse(s1);
        Result result2 = NlpAnalysis.parse(s2);
        Map<String,Integer> map1 = new HashMap<>();
        Map<String,Integer> map2 = new HashMap<>();
        for (Term term: result1.getTerms()) {
            Integer num1 = map1.get(term.getName());
            if( num1 == null){
                map1.put(term.getName(),1);
            }else {
                map1.put(term.getName(),num1 + 1);
            }
            map2.put(term.getName(),0);
        }
        for (Term term: result2.getTerms()) {
            Integer num = map2.get(term.getName());
            if( num == null){
                map2.put(term.getName(),1);
            }else {
                map2.put(term.getName(),num + 1);
            }
            Integer num1 = map1.get(term.getName());
            if( num1 == null){
                map1.put(term.getName(),0);
            }
        }
        Integer mul = 0;
        Integer sum1 = 0;
        Integer sum2 = 0;
        for (Map.Entry<String,Integer> entry : map1.entrySet()){
            Integer num1 = entry.getValue();
            Integer num2 = map2.get(entry.getKey());
            mul += num1 * num2;
            sum1 += num1 * num1;
            sum2 += num2 * num2;
        }
        double cos = mul/(Math.sqrt(sum1)*Math.sqrt(sum2));
        return cos;
    }

    public double compare(String s1, String s2){

        String shortS;
        String longS;
        if (s1.length() < s2.length()){
            shortS = s1;
            longS = s2;
        }else {
            shortS = s2;
            longS = s1;
        }

        double cos1 = doCompare(shortS, longS);
        double cos2 = doCompare(shortS, longS.substring(0,shortS.length()));
        double cos3 = doCompare(shortS, longS.substring(longS.length()-shortS.length(),longS.length()));
        double max = cos2 > cos1 ? cos2 : cos1;
        max = cos3 > max ? cos3 : max;
        return max;
    }

}
