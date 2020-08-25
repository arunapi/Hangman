package com.arunapi;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Scanner;

public class Hangman implements Runnable{

    final PipedOutputStream output;
    final PipedInputStream input;
    boolean vsHuman = false;
    public Hangman(PipedOutputStream toSolver, PipedInputStream fromSolver) {
        output = toSolver;
        input  = fromSolver;
        vsHuman = (output==null) || (input==null);
    }

    public static void main(String[] args) throws IOException, NoSuchMethodException {

        Hangman hangman = null;
        if(args.length>0) {
            try {
                final PipedOutputStream outputHangman = new PipedOutputStream();
                final PipedOutputStream outputSolver = new PipedOutputStream();
                final PipedInputStream inputSolver  = new PipedInputStream(outputHangman);

                Class solverClass = Class.forName(args[0]);
                Runnable solver = (Runnable) solverClass
                        .getDeclaredConstructor(PipedOutputStream.class, PipedInputStream.class)
                        .newInstance(outputSolver, inputSolver);
                Thread solverInstance = new Thread(solver);
                solverInstance.start();
                System.out.println("Solver is ready!");
                final PipedInputStream inputHangman  = new PipedInputStream(outputSolver);
                hangman = new Hangman(outputHangman,inputHangman);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                System.out.println("Unable to load Solver. Solver must have two args Constructor. " +
                        "See HangmanSolver.java and use full class name. eg: com.arunapi.HangmanSolver. " +
                        "Cause of error: "+e.getMessage());
            }
        }
        else{
            System.out.println("Solver not present in arguments list.");
        }
        if(hangman==null){
            System.out.println("vs Human mode!");
            hangman = new Hangman(null,null);
        }
        Thread challenger = new Thread(hangman);
        challenger.start();
    }

    public void run(){
        System.out.println("Welcome to Hangman!");
        String secret = getSecret();
        char[] arr = secret.toCharArray();
        String updatedWord = " ";
        int guessCount =6;
        String guessChar = " ";
        int score = 0;
        while(guessCount>=0 && !secret.equals(updatedWord)){
            char[] updatedWordArr = updatedWord.toCharArray();
            updatedWord = "";
            for(int i=0;i<arr.length;i++){
                if(updatedWordArr.length>i && arr[i]==updatedWordArr[i] || arr[i]==guessChar.charAt(0)){
                    updatedWord = updatedWord+arr[i];
                }
                else{
                    updatedWord = updatedWord+"-";
                }
            }
            if(guessCount>0) {
                System.out.println("The word now looks like this:" + updatedWord);
                System.out.println("You have " + guessCount + " guesses left.");

                if(vsHuman) {
                    System.out.println("Your guess:");
                    guessChar = getInputFromUser();
                }
                else{
                    writeToSolver("The word now looks like this:" + updatedWord);
                    guessChar = getInputFromSolver();
                    System.out.println("Your guess:"+guessChar);
                }
                if (guessChar.length() > 1) {
                    System.out.println("Your entry is illegal.");
                    continue;
                }

                guessCount--;

            } else if (secret.equals(updatedWord)) {
                System.out.println("You guessed the word: "+updatedWord);
                System.out.println("You win.");
                writeToSolver("You win.");
                secret = getSecret();
                guessCount=6;
                score++;
            }
            else{
                System.out.println("The word was: "+secret);
                System.out.println("You lose.");
                writeToSolver("You lose.");
                guessCount--;
            }

        }
        System.out.println("Final Score: "+score);
    }

    public void writeToSolver(String message) {
        try {
            output.write(message.getBytes());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getInputFromUser() {
        String guessChar;
        Scanner scanner = new Scanner(System.in);
        guessChar = scanner.nextLine().toUpperCase();
        return guessChar;
    }

    public String getInputFromSolver() {
        String guessChar = readInput().toUpperCase();
        return guessChar;
    }

    public String getSecret() {
        ArrayList<String> secretsList = new ArrayList<>();
        try (
                BufferedReader words = new BufferedReader(
                    new FileReader("words.txt"))){
            String input;
            while ((input = words.readLine()) != null)
                secretsList.add(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String secret = secretsList.get((int) ((Math.random() * (secretsList.size())))).toUpperCase();
        return secret;
    }

    private String readInput(){
        int size = 0;
        byte[] bytes = null;
        try {
            while(input.available()==0){
                //wait for input
            }

            if ((size = input.available()) != 0) {
                bytes = new byte[size];
                input.read(bytes, 0, bytes.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }
}