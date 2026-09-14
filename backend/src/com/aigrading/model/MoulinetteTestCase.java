package com.aigrading.model;

public class MoulinetteTestCase {
    private int testId;
    private String testCode;
    private String title;
    private String description;
    private String testType; // PUBLIC, HIDDEN
    private String expectedOutput;
    private double weight;
    private boolean active;

    public MoulinetteTestCase() {}

    public MoulinetteTestCase(int testId, String testCode, String title, String testType, double weight) {
        this.testId = testId;
        this.testCode = testCode;
        this.title = title;
        this.testType = testType;
        this.weight = weight;
        this.active = true;
    }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
