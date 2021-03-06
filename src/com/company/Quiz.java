package com.company;

import com.company.questions.MultiSelect;
import com.company.questions.Question;
import com.company.questions.TrueFalse;

import java.lang.reflect.Array;
import java.util.*;

public class Quiz {
    private Scanner input;

    private ArrayList<Question> questions;
    private double grade;
    private String name;

    private String getName() {
        return name;
    }

    public static void demo(Scanner input) {
        boolean incomplete = true;
        do {
            System.out.println(">>>>>>>> QuizTime Demo! <<<<<<<\n");
            System.out.println("Enter a name for your quiz");
            try {
                String name = input.nextLine();
                Quiz quiz = new Quiz(name, input);
                incomplete = false;
                quiz.start(false);
            } catch (InputMismatchException e) {
                System.out.println("Invalid input for quiz name. Try again");
            }
        } while(incomplete);
    }

    public Quiz(String name, Scanner input) {
        this.questions = new ArrayList<>();
        this.name = name;
        this.input = input;

        this.setupQuiz();
    }

    public void start(boolean restart)  {
        String intro = restart ? "Retrying " : "";
        System.out.println(">>>>> " + intro + "Quiz: " + this.getName() + " <<<<<<\n");

        int index = 1;
        for (Question question : this.questions) {
            if (restart && question.isCorrect()) continue;

            System.out.print("Question " + index + "/" + this.questions.size() + ": ");
            question.ask();
            ++index;
        }

        this.finish();
    }

    public double getGrade() {
        int totalCorrect = 0;
        for (Question question : this.questions) {
           if (question.isCorrect()) ++totalCorrect;
        }

        return (totalCorrect / this.questions.size()) * 100;
    }

    public void finish() {
        System.out.println("Grade: " + this.getGrade());
        System.out.println("\n>>>>> Missed Questions <<<<<<\n");

        int index = 0;
        for (Question question : this.questions) {
            if (!question.isCorrect()) {
                System.out.println("Question " + index + ": " + question.getDescription());
                ++index;
            }
        }

        if (index == 0) {
            System.out.println("No missed questions!");
        }

        System.out.println("\n>>>>> Missed Questions <<<<<<\n");

        if (index != 0) retry();
    }

    private void retry() {
        boolean incorrect = true;
        do {
            System.out.println("Retry missed questions?\n1: Yes\n2: No");
            int choice = this.input.nextInt();
            if (choice == 1) {
                this.start(true);
                incorrect = false;
            } else {
                System.out.println("Better luck next time bro");
                incorrect = false;
            }
        } while (incorrect);
    }

    private void setupQuiz() {
        int numberOfQuestions;
        do {
            System.out.println("How many questions should be on the quiz?");
            if (this.input.hasNextInt()) {
                numberOfQuestions = this.input.nextInt();
            } else {
                this.input.next();
                numberOfQuestions = 0;
            }
        } while (numberOfQuestions == 0);

        for (int i = 0; i < numberOfQuestions; ++i) {
            this.addQuestion();
        }
    }

    private void addQuestion() {
        int choice;
        do {
            System.out.println("Which type of question do you want to build?");
            System.out.println("1: True or False\n2: Multi Choice\n3: Multi Select");

            if (this.input.hasNextInt()) {
                choice = this.input.nextInt();

                if (choice < 1 || choice > 3) {
                    System.out.println("Invalid choice");
                    choice = 0;
                }
            }  else {
                this.input.next();
                choice = 0;
            }
        } while (choice == 0);

        Question newQuestion = this.makeQuestion(choice);
        this.questions.add(newQuestion);
    }

    private Question makeQuestion(int choice) {
        // TODO: add additional switch cases here for the remaining Question subclasses
        switch (choice) {
            case 1:
                return this.buildTrueFalse();
            case 3:
                return this.buildMultiSelect();
        }

        return null;
    }

    private String promptForDescription() {
        boolean incomplete = true;
        String description = null;
        do {
            System.out.println("What is the question description?");
            try {
                input.nextLine(); // clear buffer ... ?
                description = input.nextLine();
                incomplete = false;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input for description");
            }
        } while (incomplete);

        return description;
    }

    private int[] promptForAnswer() {
        int[] answer = new int[1];
        boolean incomplete = true;
        do {
            System.out.println("What is the correct answer?");
            try {
                int correctAnswer = input.nextInt();
                answer[0] = correctAnswer;
                incomplete = false;
            } catch (InputMismatchException e) {
                input.next(); // clear buffer
                System.out.println("Invalid input for answer");
            }
        } while (incomplete);

        return answer;
    }

    private HashMap<Integer, String> promptForChoices(){
        HashMap<Integer, String> choices = new HashMap<>();
        int choiceNumber = 1;
        do{
            System.out.println("Enter the answer options or 'done' to complete");
            String choiceValue = this.input.nextLine();
            if(choiceValue.trim().toLowerCase().equals("done")){
                break;
            }
            if (choices.values().contains(choiceValue.trim())){
                System.out.println("This option already exists ");
                continue;
            }
            choices.put(choiceNumber++,choiceValue.trim());
        }while(true);
        return choices;
    }

    private int[] promptForChoiceAnswers(HashMap<Integer, String> choices, String description){
        ArrayList<Integer> answerArray = new ArrayList<>();
        System.out.println("Please tell us which are all the correct options for the question following question\n" + description);
        for(Map.Entry<Integer, String> choice: choices.entrySet()){
            System.out.println(choice.getKey()+" : "+choice.getValue());
            System.out.println("Enter 'y' if you want to add this answer as a valid answer or press/type any other key(s) to skip : ");
            if(input.hasNext()){
                String name = input.next();
                if(name.toLowerCase().equals("y")){
                    answerArray.add(choice.getKey());
                }
            }
        }
        return answerArray.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
    }

    // TODO: implement a 'getAnswers()' method that functions similarly to the one above
    // TODO: but is able to accept and store multiple answers
    // TODO: call this method in the MultiChoice and MultiSelect subclass builder methods

    private TrueFalse buildTrueFalse() {
        String description = this.promptForDescription();
        int answer[] = this.promptForAnswer();

        return new TrueFalse(description, answer, this.input);
    }

    private MultiSelect buildMultiSelect(){
        String description = this.promptForDescription();
        HashMap<Integer, String> choices = this.promptForChoices();
        int[] answer = this.promptForChoiceAnswers(choices, description);

        return new MultiSelect(description,choices,answer,this.input);
    }

    // TODO: create a 'buildMultiChoice()' method

    // TODO: create a 'buildMultiSelect()' method
}
