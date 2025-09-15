package com.yourcompany.chatbot;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        ChatBot bot = new ChatBot();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Welcome to PC ChatBot! Type 'exit' to quit.");
        
        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();
            
            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            
            String response = bot.getResponse(userInput);
            System.out.println("\nBot: " + response);
        }
        
        scanner.close();
    }
}
