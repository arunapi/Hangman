package com.arunapi;

import java.io.*;
import java.util.Scanner;

public class HangmanSolver implements Runnable{

    final PipedOutputStream output;
    final PipedInputStream input;

    public HangmanSolver(PipedOutputStream toChallenger, PipedInputStream fromChallenger) throws IOException {
        output = toChallenger;
        input  = fromChallenger;
    }

    @Override
    public void run() {
        boolean gameOver = false;
        String guessedSoFar = "";
        String updatedWord = null;
        while(!gameOver) {
            try {


                String input = readInput();
                if(input.startsWith("The word now looks like this:")){
                    updatedWord = input.substring(input.indexOf(":"));
                    String nextChar = guessNextChar(updatedWord,guessedSoFar);
                    output.write(nextChar.getBytes());
                    output.flush();
                }
                else if("Your win.".equals(input)){
                    guessedSoFar = "";
                }
                gameOver = "You lose.".equals(input);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //Output guess to out stream

        }

    }

    private String guessNextChar(String updatedWord, String guessedSoFar) {
        //find the word size.
        //how many characters solved.
        //how many left
        char[] alphabets = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        if(updatedWord!=null) {
            char[] guessedCorrect = updatedWord.replace("-", "").toCharArray();
            for (char c : guessedCorrect) {
                if (!guessedSoFar.contains(Character.toString(c))) {
                    guessedSoFar = guessedSoFar + Character.toString(c);
                }
            }
        }
        String nextGuess = null;
        while(nextGuess==null){
            nextGuess = Character.toString(alphabets[(int)(Math.random() * (26))]);
            if(guessedSoFar.contains(nextGuess)){
                nextGuess = null;
            }
        }
        return nextGuess;
    }

    private String readInput() throws IOException {
        int size = 0;
        byte[] bytes = null;
        while(input.available()==0){
            //wait for input
        }
        if ((size = input.available()) != 0) {
            bytes = new byte[size];
            input.read(bytes, 0, bytes.length);
        }
        return new String(bytes);
    }
}