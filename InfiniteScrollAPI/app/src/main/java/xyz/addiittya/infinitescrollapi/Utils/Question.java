package xyz.addiittya.infinitescrollapi.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by addiittya on 30/04/16.
 */

public class Question {
    private int id_question;
    private String mQuestion;
    private String mAnswer;

    public Question(int id, String question, String answer) {
        id_question = id;
        mQuestion = question;
        mAnswer = answer;
    }

    public String getQuestion() {
        return mQuestion;
    }

    private static int lastQuestionId = 0;

    public static List<Question> createQuestionsList(int numQuestions) {
        List<Question> Questions = new ArrayList<Question>();

        for (int i = 1; i <= numQuestions; i++) {
            Questions.add(new Question(++lastQuestionId,"Question ","Answer "));
        }

        return Questions;
    }
}