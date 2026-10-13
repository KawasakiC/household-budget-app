package io.github.kawasakic.householdbudget;

import java.util.Map;

public class SatisfactionComment {

    private static final Map<String, String> COMMENTS = Map.of(
            "C001", "とても満足！納得できる買い物が多かった月です。",
            "C002", "満足できる買い物が多め。いい感じです！",
            "C003", "まずまずの満足度。少し振り返ると、次の買い物に活かせそうです。",
            "C004", "気になる買い物がいくつかあったかも。使い道を見直してみましょう。",
            "C005", "満足度が低めの月でした。何に使うと満足できるか考えてみましょう。"
    );

    public static String findById(String id) {
        return COMMENTS.get(id);
    }
    
    public static String findIdByPercent(int percent) {
        if (percent >= 90) {
            return "C001";
        } else if (percent >= 75) {
            return "C002";
        } else if (percent >= 60) {
            return "C003";
        } else if (percent >= 40) {
            return "C004";
        } else {
            return "C005";
        }
    }
}