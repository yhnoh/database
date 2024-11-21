- analyzer 분석기
- 표준분석기
- 텍스트를 단어 경계에 따라 용어로 나누므로 대부분의 사용 사례에서 언어 중립적입니다. 모든 용어를 소문자로 변환하고 문장 부호를 제거합니다. 
- https://www.mongodb.com/ko-kr/docs/atlas/atlas-search/analyzers/standard/

- 필드매핑정의
- 정적매핑정의
  - 하나의 필드안에 여러 타입을 선언할 수 있다.
  - 각 타입별로 사용할 수 있는 연산자의 제한이 존재한다.

```json
{
  "mappings": {
    "dynamic": <boolean>,
    "fields": {
      "<field-name>": [
        {
          "type": "<field-type>",
          ...
        },
        {
          "type": "<field-type>",
          ...
        },
        ...
      ],
      ...
    }
  }
}
```
  - https://www.mongodb.com/ko-kr/docs/atlas/atlas-search/define-field-mappings/


```json
{
  $search: {
    "index": "<index-name>",
    "<operator-name>"|"<collector-name>": {
      <operator-specification>|<collector-specification>
    },
    "highlight": {
      <highlight-options>
    },
    "concurrent": true | false,
    "count": {
      <count-options>
    },
    "searchAfter"|"searchBefore": "<encoded-token>",
    "scoreDetails": true| false,
    "sort": {
      <fields-to-sort>: 1 | -1
    },
    "returnStoredSource": true | false,
    "tracking": {
      <tracking-option>
    }
   }
}
```
https://www.mongodb.com/ko-kr/docs/atlas/atlas-search/aggregation-stages/search/

- 패싯
- https://www.mongodb.com/ko-kr/docs/atlas/atlas-search/tutorial/facet-tutorial/