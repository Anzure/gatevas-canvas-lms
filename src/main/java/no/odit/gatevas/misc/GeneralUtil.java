package no.odit.gatevas.misc;

import java.util.Random;

public class GeneralUtil {

    public static String generatePassword() {
        String[] wordList1 = {"Oste", "Velge", "Vaske", "Trene", "Telle", "Sykle", "Studere", "Stemme", "Snike", "Smile", "Skrive", "Knekke", "Danse", "Drikke", "Gul", "Lilla", "Svart", "Hvit"};
        String[] wordList2 = {"kake", "banan", "bil", "traktor", "fjell", "hytte", "eple", "taco", "pizza", "blyant", "fisk", "egg", "biff", "ball", "melk", "ost", "sko", "stol", "bord", "flaske"};

        String word1 = wordList1[new Random().nextInt(wordList1.length - 1)];
        String word2 = wordList2[new Random().nextInt(wordList2.length - 1)];
        int num = new Random().nextInt(90) + 10;

        String password = word1 + word2 + num;
        return password;
    }

}