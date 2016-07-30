//
//  Rank.cpp
//  SearchDatabase
//
//  Created by Zack Liston on 6/9/15.
//  Copyright (c) 2015 Zack Liston. All rights reserved.
//

#include "Rank.h"
#include "math.h"

int const kZLWeight0ColumnNumber = 4;
int const kZLWeight1ColumnNumber = 5;
int const kZLWeight2ColumnNumber = 6;
int const kZLWeight3ColumnNumber = 7;
int const kZLWeight4ColumnNumber = 8;
int const kZLNumberOfWeightedColumns = 5;

#pragma mark - Private Methods

double inverseDocumentFrequency(int totalNumberOfDocuments, int numberOfDocumentsContainingSearchPhrase)
{
    if (totalNumberOfDocuments < 1) {
        return 0.0;
    }
    
    double constant = 0.5;
    double numerator = totalNumberOfDocuments-numberOfDocumentsContainingSearchPhrase+constant;
    double denominator = numberOfDocumentsContainingSearchPhrase+constant;
    double fraction = numerator/denominator;
    
    return log(fraction);
}

double normalizedTermFrequencyForField(int numberOfMatches, int lengthOfFieldInWords, int averageLengthOfFieldInWords, double bConstant)
{
    if (lengthOfFieldInWords == 0) {
        return 0.0;
    }
    if (averageLengthOfFieldInWords == 0) {
        averageLengthOfFieldInWords = 1;
    }
    
    double numerator = numberOfMatches;
    double denominator = 1+(bConstant*(((double)lengthOfFieldInWords/(double)averageLengthOfFieldInWords)-1));
    
    return numerator/denominator;
}

double normalizedTermFrequencyForDocument(double fieldWeights[], double fieldNormalizedTermFrequencies[], int numberOfFields)
{
    double result = 0.0;
    for (int i=0; i<numberOfFields; i++) {
        result += fieldWeights[i]*fieldNormalizedTermFrequencies[i];
        
    }
    
    return result;
}

double BM25F(double normalizedWeightedTermFrequencies[], double inverseDocumentFrequencies[], double saturationConstant, int numberOfTerms)
{
    double rank = 0.0;
    
    for (int i=0; i<numberOfTerms; i++) {
        double normalizedTermFrequency = normalizedWeightedTermFrequencies[i];
        double termIDF = inverseDocumentFrequencies[i];
        
        rank += (normalizedTermFrequency/(normalizedTermFrequency + saturationConstant)*termIDF);
    }
    
    return rank;
}

#pragma mark - Public Methods

double rank(unsigned int *aMatchinfo, double boost, double weights[])
{
    unsigned int PHRASE_INDEX = 0;
    unsigned int COLUMN_INDEX = 1;
    unsigned int ROW_COUNT_INDEX = 2;
    unsigned int AVERAGE_WORD_INDEX = 3;
    unsigned int WORD_COUNT_INDEX = 4;
    unsigned int PHRASE_INFO_INDEX = 5;
    
    double score = 0.0;             /* Value to return */
    
    int numberOfPhrasesInQuery = aMatchinfo[PHRASE_INDEX];
    unsigned int totalNumberOfColumns = aMatchinfo[COLUMN_INDEX];
    unsigned int totalNumberOfRows = aMatchinfo[ROW_COUNT_INDEX];
    unsigned int *columnAverageInfo = &aMatchinfo[AVERAGE_WORD_INDEX];
    unsigned int *wordCountInfo = &aMatchinfo[(totalNumberOfColumns -1) + WORD_COUNT_INDEX];
    unsigned int *phraseInfoArray = &aMatchinfo[((totalNumberOfColumns-1) * 2) + PHRASE_INFO_INDEX];
    
    unsigned int phraseInfoLength = totalNumberOfColumns*3;
    
    double termFrequencies[numberOfPhrasesInQuery];
    double termIDFs[numberOfPhrasesInQuery];
    
    for (int currentPhrase=0; currentPhrase<numberOfPhrasesInQuery; currentPhrase++) {
        unsigned int *phraseInfo = &phraseInfoArray[currentPhrase * phraseInfoLength];
        
        double termFrequenciesForFields[kZLNumberOfWeightedColumns];
        double aggregateIDF = 0.0;
        unsigned int index = 0;
        
        for(int currentColumn=kZLWeight0ColumnNumber; currentColumn<=kZLWeight4ColumnNumber; currentColumn++) {
            
            unsigned int hitCountInCurrentRow = phraseInfo[currentColumn * 3 + 0];
            //unsigned int hitCountInAllRows = phraseInfo[currentColumn * 3 + 1];
            unsigned int numberOfRowsWithHit = phraseInfo[currentColumn * 3 +2];
            
            unsigned int averageNumberOfWordsInColumn = columnAverageInfo[currentColumn];
            unsigned int wordCount = wordCountInfo[currentColumn];
            
            double IDF = inverseDocumentFrequency(totalNumberOfRows, numberOfRowsWithHit);
            double termFrequency = normalizedTermFrequencyForField(hitCountInCurrentRow, wordCount, averageNumberOfWordsInColumn, 0.4);
            
            aggregateIDF += IDF;
            termFrequenciesForFields[index] = termFrequency;
            index++;
            
        }
        
        termIDFs[currentPhrase] = aggregateIDF/(double)kZLNumberOfWeightedColumns;
        termFrequencies[currentPhrase] = normalizedTermFrequencyForDocument(weights, termFrequenciesForFields, kZLNumberOfWeightedColumns);
        
    }
    
    score = BM25F(termFrequencies, termIDFs, 1.7, numberOfPhrasesInQuery);
    
    return score;
}