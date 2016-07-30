//
//  main.cpp
//  SearchDatabase
//
//  Created by Zack Liston on 6/4/15.
//  Copyright (c) 2015 Zack Liston. All rights reserved.
//

#include <iostream>
#include "search_database.h"
#include <map>

int main(int argc, const char * argv[]) {
    // insert code here...
    SearchDatabase database = SearchDatabase("/Users/agilediagnosis/Desktop/database");
    
    char *errorMessage;
    bool success = database.setup(&errorMessage);
    if (success) {
        printf("succeeded\n");
    } else {
        printf("failure %s\n", errorMessage);
    }
    
    std::map<std::string, std::string> searchable;
    searchable[kZLSearchDBWeight4Key] = "hello";
    searchable[kZLSearchDBWeight3Key] = "hellohellohellohellohellohellohellohellohellohello ";
    searchable[kZLSearchDBWeight2Key] = "hello";
    searchable[kZLSearchDBWeight1Key] = "hello";
    searchable[kZLSearchDBWeight0Key] = "hello";
    std::map<std::string, std::string> meta;
   
    time_t  timev;
    time(&timev);
    printf("start time = %li\n", timev);
    for (int i=0; i<500; i++) {
        meta[kZLSearchDBTitleKey] = "The title";
        meta[kZLSearchDBUriKey] = "www.url.com";
        std::string moduleId = "module";
        std::string fileId = "fileId"+ std::to_string(i);
        std::string language = "en";
        double boost = 342.33;
        
        char *insertErrorMessage;
        
        bool indexSuccess = database.index_file(moduleId, fileId, language, boost, searchable, meta, &insertErrorMessage);
        if (!indexSuccess) {
            printf("Error indexing %s\n", insertErrorMessage);
        }
    }
    
    time_t  etimev;
    time(&etimev);
    printf("end time = %li\n", etimev);
    
    SearchResult searchResults[100];
    int numberOfResults;
    bool searchSuccess = database.search("hello", 100, 0, true, searchResults, &numberOfResults, NULL, NULL, &errorMessage);
    if (searchSuccess) {
        printf("Number of results %i\n", numberOfResults);
    } else {
        printf("Error searching %s\n", errorMessage);
    }
    
    SearchResult result = searchResults[0];
    printf("title %s", result.title.c_str());
    
    return 0;
}
