//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: src/main/java/org/lukhnos/lucenestudy/Searcher.java
//

#ifndef _OrgLukhnosLucenestudySearcher_H_
#define _OrgLukhnosLucenestudySearcher_H_

#include "J2ObjC_header.h"
#include "java/lang/AutoCloseable.h"
#include "java/lang/Enum.h"

@class OrgApacheLuceneAnalysisAnalyzer;
@class OrgApacheLuceneIndexIndexReader;
@class OrgApacheLuceneSearchQuery;
@class OrgApacheLuceneSearchScoreDoc;
@class OrgApacheLuceneSearchSort;
@class OrgLukhnosLucenestudySearchResult;
@class OrgLukhnosLucenestudySearcher_SortByEnum;

@interface OrgLukhnosLucenestudySearcher : NSObject < JavaLangAutoCloseable > {
 @public
  OrgApacheLuceneAnalysisAnalyzer *analyzer_;
  OrgApacheLuceneIndexIndexReader *indexReader_;
}

#pragma mark Public

- (instancetype)initWithNSString:(NSString *)indexRoot;

- (void)close;

- (OrgLukhnosLucenestudySearchResult *)searchWithNSString:(NSString *)queryStr
                                                  withInt:(jint)maxCount;

- (OrgLukhnosLucenestudySearchResult *)searchWithNSString:(NSString *)queryStr
             withOrgLukhnosLucenestudySearcher_SortByEnum:(OrgLukhnosLucenestudySearcher_SortByEnum *)sortBy
                                                  withInt:(jint)maxCount;

- (OrgLukhnosLucenestudySearchResult *)searchAfterWithOrgLukhnosLucenestudySearchResult:(OrgLukhnosLucenestudySearchResult *)result
                                                                                withInt:(jint)maxCount;

#pragma mark Package-Private

- (OrgLukhnosLucenestudySearchResult *)searchAfterWithOrgApacheLuceneSearchScoreDoc:(OrgApacheLuceneSearchScoreDoc *)lastScoreDoc
                                                     withOrgApacheLuceneSearchQuery:(OrgApacheLuceneSearchQuery *)query
                                                      withOrgApacheLuceneSearchSort:(OrgApacheLuceneSearchSort *)sort
                                                                            withInt:(jint)maxCount;

@end

J2OBJC_EMPTY_STATIC_INIT(OrgLukhnosLucenestudySearcher)

J2OBJC_FIELD_SETTER(OrgLukhnosLucenestudySearcher, analyzer_, OrgApacheLuceneAnalysisAnalyzer *)
J2OBJC_FIELD_SETTER(OrgLukhnosLucenestudySearcher, indexReader_, OrgApacheLuceneIndexIndexReader *)

FOUNDATION_EXPORT void OrgLukhnosLucenestudySearcher_initWithNSString_(OrgLukhnosLucenestudySearcher *self, NSString *indexRoot);

FOUNDATION_EXPORT OrgLukhnosLucenestudySearcher *new_OrgLukhnosLucenestudySearcher_initWithNSString_(NSString *indexRoot) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(OrgLukhnosLucenestudySearcher)

typedef NS_ENUM(NSUInteger, OrgLukhnosLucenestudySearcher_SortBy) {
  OrgLukhnosLucenestudySearcher_SortBy_RELEVANCE = 0,
  OrgLukhnosLucenestudySearcher_SortBy_DOCUMENT_ORDER = 1,
  OrgLukhnosLucenestudySearcher_SortBy_TITLE = 2,
  OrgLukhnosLucenestudySearcher_SortBy_YEAR = 3,
  OrgLukhnosLucenestudySearcher_SortBy_RATING = 4,
};

@interface OrgLukhnosLucenestudySearcher_SortByEnum : JavaLangEnum < NSCopying > {
 @public
  OrgApacheLuceneSearchSort *sort_;
}

#pragma mark Package-Private

+ (IOSObjectArray *)values;
FOUNDATION_EXPORT IOSObjectArray *OrgLukhnosLucenestudySearcher_SortByEnum_values();

+ (OrgLukhnosLucenestudySearcher_SortByEnum *)valueOfWithNSString:(NSString *)name;
FOUNDATION_EXPORT OrgLukhnosLucenestudySearcher_SortByEnum *OrgLukhnosLucenestudySearcher_SortByEnum_valueOfWithNSString_(NSString *name);

- (id)copyWithZone:(NSZone *)zone;

@end

J2OBJC_STATIC_INIT(OrgLukhnosLucenestudySearcher_SortByEnum)

FOUNDATION_EXPORT OrgLukhnosLucenestudySearcher_SortByEnum *OrgLukhnosLucenestudySearcher_SortByEnum_values_[];

#define OrgLukhnosLucenestudySearcher_SortByEnum_RELEVANCE OrgLukhnosLucenestudySearcher_SortByEnum_values_[OrgLukhnosLucenestudySearcher_SortBy_RELEVANCE]
J2OBJC_ENUM_CONSTANT_GETTER(OrgLukhnosLucenestudySearcher_SortByEnum, RELEVANCE)

#define OrgLukhnosLucenestudySearcher_SortByEnum_DOCUMENT_ORDER OrgLukhnosLucenestudySearcher_SortByEnum_values_[OrgLukhnosLucenestudySearcher_SortBy_DOCUMENT_ORDER]
J2OBJC_ENUM_CONSTANT_GETTER(OrgLukhnosLucenestudySearcher_SortByEnum, DOCUMENT_ORDER)

#define OrgLukhnosLucenestudySearcher_SortByEnum_TITLE OrgLukhnosLucenestudySearcher_SortByEnum_values_[OrgLukhnosLucenestudySearcher_SortBy_TITLE]
J2OBJC_ENUM_CONSTANT_GETTER(OrgLukhnosLucenestudySearcher_SortByEnum, TITLE)

#define OrgLukhnosLucenestudySearcher_SortByEnum_YEAR OrgLukhnosLucenestudySearcher_SortByEnum_values_[OrgLukhnosLucenestudySearcher_SortBy_YEAR]
J2OBJC_ENUM_CONSTANT_GETTER(OrgLukhnosLucenestudySearcher_SortByEnum, YEAR)

#define OrgLukhnosLucenestudySearcher_SortByEnum_RATING OrgLukhnosLucenestudySearcher_SortByEnum_values_[OrgLukhnosLucenestudySearcher_SortBy_RATING]
J2OBJC_ENUM_CONSTANT_GETTER(OrgLukhnosLucenestudySearcher_SortByEnum, RATING)

J2OBJC_FIELD_SETTER(OrgLukhnosLucenestudySearcher_SortByEnum, sort_, OrgApacheLuceneSearchSort *)

J2OBJC_TYPE_LITERAL_HEADER(OrgLukhnosLucenestudySearcher_SortByEnum)

#endif // _OrgLukhnosLucenestudySearcher_H_