package com.tim.tsms.transpondsms.utils;

import android.util.Log;

import com.tim.tsms.transpondsms.model.vo.SmsExtraVo;
import com.tim.tsms.transpondsms.model.vo.SmsVo;

import java.util.*;

import static com.tim.tsms.transpondsms.utils.RuleLine.CONJUNCTION_AND;
import static com.tim.tsms.transpondsms.utils.RuleLine.CONJUNCTION_OR;

public class RuleLineUtils {
    static String TAG = "RuleLineUtils";
    static Boolean STARTLOG = false;

    public static void main(String[] args) throws Exception {
        String a = "并且 是 手机号 相等 10086\n" +
                " 或者 是 手机号 结尾 哈哈哈\n" +
                "  并且 是 短信内容 包含 asfas\n" +
                " 或者 是 手机号 结尾 aaaa\n" +
                "并且 是 手机号 相等 100861\n" +
                "并且 是 手机号 相等 100861";

        SmsVo msg = new SmsVo("10086", "哈哈哈", new Date(),new SmsExtraVo(1,"卡哇伊","红色白皮手机"));
        logg("check:" + checkRuleLines(msg, a));
    }

    public static void startLog(boolean startLog) {
        STARTLOG = startLog;
    }

    public static void logg(String msg) {
        if (STARTLOG) {
            Log.i(TAG, msg);
        }

    }

    public static boolean checkRuleLines(SmsVo msg, String RuleLines) throws Exception {

        Scanner scanner = new Scanner(RuleLines);

        int linenum = 0;
        RuleLine headRuleLine = null;

        RuleLine beforeRuleLine = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            logg(linenum + " : " + line);
            //第一行
            if (linenum == 0) {
                //第一行不允许缩进
                if (line.startsWith(" ")) {
                    throw new Exception("第一行不允许缩进");
                }
            }

            // process the line


            beforeRuleLine = RuleLineUtils.generateRuleTree(line, linenum, beforeRuleLine);
            if (linenum == 0) {
                headRuleLine = beforeRuleLine;
            }

            linenum++;
        }

        return checkRuleTree(msg, headRuleLine);

    }


    /**
     * 使用规则树判断消息是否命中规则
     * Rule节点是否命中取决于：该节点是否命中、该节点子结点（如果有的话）是否命中、该节点下节点（如果有的话）是否命中
     * 递归检查
     */

    public static boolean checkRuleTree(SmsVo msg, RuleLine currentRuleLine) throws Exception {
        //该节点是否命中
        boolean currentAll = currentRuleLine.checkMsg(msg);
        logg("current:" + currentRuleLine + " checked:" + currentAll);

        //该节点子结点（如果有的话）是否命中
        if (currentRuleLine.getChildRuleLine() != null) {
            logg(" child:" + currentRuleLine.getChildRuleLine());

            //根据情况连接结果
            switch (currentRuleLine.getChildRuleLine().conjunction) {
                case CONJUNCTION_AND:
                    currentAll = currentAll && checkRuleTree(msg, currentRuleLine.getChildRuleLine());
                    break;
                case CONJUNCTION_OR:
                    currentAll = currentAll || checkRuleTree(msg, currentRuleLine.getChildRuleLine());
                    break;
                default:
                    throw new Exception("child wrong conjunction");
            }
        }

        //该节点下节点（如果有的话）是否命中
        if (currentRuleLine.getNextRuleLine() != null) {
            logg("next:" + currentRuleLine.getNextRuleLine());
            //根据情况连接结果
            switch (currentRuleLine.getNextRuleLine().conjunction) {
                case CONJUNCTION_AND:
                    currentAll = currentAll && checkRuleTree(msg, currentRuleLine.getNextRuleLine());
                    break;
                case CONJUNCTION_OR:
                    currentAll = currentAll || checkRuleTree(msg, currentRuleLine.getNextRuleLine());
                    break;
                default:
                    throw new Exception("next wrong conjunction");
            }
        }

        return currentAll;
    }

    /**
     * 生成规则树
     * 一行代表一个规则
     */

    public static RuleLine generateRuleTree(String line, int lineNum, RuleLine parentRuleLine) throws Exception {
        String[] words = line.split(" ");

        return new RuleLine(line, lineNum, parentRuleLine);
    }

}
