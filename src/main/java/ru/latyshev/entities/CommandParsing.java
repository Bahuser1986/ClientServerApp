package ru.latyshev.entities;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandParsing {

    //login command parsing
    public static boolean checkLoginCommand(String command) {
        String[] loginArr = command.trim().split(" ", 2);
        if (loginArr.length != 2) {
            return false;
        }

        String loginCommand = loginArr[0];
        String[] loginParams = loginArr[1].trim().split("=");
        if (loginParams.length != 2 || loginParams[1].isEmpty()) {
            return false;
        }
        String loginKey = loginParams[0].trim();

        if (!loginCommand.equals("login")) {
            return false;
        }
        if (!loginKey.equals("-u")) {
            return false;
        }
        return true;
    }
    public static String getLoginNameFromCommand(String command) {
        String[] loginArr = command.split("=");
        return loginArr[1].trim();
    }

    public static String getTopicName(String command) {
        String topicName = command.split("=")[1].trim();
        return topicName.substring(1, topicName.length() - 1);
    }

    public static String[] getTopicAndVoteNames(String request) {
        String[] arr = request.split("=");

        String topicName = arr[1].substring(1, arr[1].lastIndexOf(" ") - 1);
        String voteName = arr[2].substring(1, arr[2].length() - 1);

        return new String[] {topicName, voteName};
    }
    public static String getFileName(String command) {
        String[] arr = command.split("\"");
        return arr[1];
    }
    public static void sendHelpMessage(PrintWriter writer) {
        List<String> helpList = Arrays.asList(
                "*******************************",
                "******* Client commands *******",
                "* login -u=username           *",
                "* create topic -n=\"topic\"     *",
                "* view                        *",
                "* view -t=\"topic\"             *",
                "* create vote -t=\"topic\"      *",
                "* view -t=\"topic\" -v=\"vote\"   *",
                "* vote -t=\"topic\" -v=\"vote\"   *",
                "* delete -t=\"topic\" -v=\"vote\" *",
                "* exit                        *",
                "******* Server commands *******",
                "* load \"filename\"             *",
                "* save \"filename\"             *",
                "* exit                        *",
                "*******************************");
        for (String response : helpList) {
            writer.println(response);
        }
    }
}
