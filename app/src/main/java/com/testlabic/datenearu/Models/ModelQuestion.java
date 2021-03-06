package com.testlabic.datenearu.Models;

public class ModelQuestion {
    String question;
    String optA;
    String optB;
    String optC;
    String optD;
    public String correctOption;
    
    public String getSelectedOption() {
        return selectedOption;
    }
    
    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
    
    String selectedOption;
    
    public ModelQuestion() {
    }
    
    public ModelQuestion(String question, String optA, String optB, String optC, String optD, String correctOption) {
        this.question = question;
        this.optA = optA;
        this.optB = optB;
        this.optC = optC;
        this.optD = optD;
        this.correctOption = correctOption;
    }
    
    public String getCorrectOption() {
        return correctOption;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public String getOptA() {
        return optA;
    }
    
    public String getOptB() {
        return optB;
    }
    
    public String getOptC() {
        return optC;
    }
    
    public String getOptD() {
        return optD;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public void setOptA(String optA) {
        this.optA = optA;
    }
    
    public void setOptB(String optB) {
        this.optB = optB;
    }
    
    public void setOptC(String optC) {
        this.optC = optC;
    }
    
    public void setOptD(String optD) {
        this.optD = optD;
    }
    
    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
