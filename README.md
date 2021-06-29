
TDD연습 (https://javacan.tistory.com/entry/TDD-exercise-1-Design-communication-using-tdd?category=454313)

create project
mvn archetype:generate -DgroupId=vmfhrmfoaj.study -DartifactId=transcoding -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

Add mvnw (maven wrapper)
mvn -N io.takari:maven:wrapper

TDD 연습 1. TranscodingService의 관련 객체 인터페이스 도출하기