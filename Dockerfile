# 1단계: 빌드
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY AuthService/ .
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# 2단계: 실행
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/

# 실행할 JAR app.jar로 리네임
RUN set -eux; \
    for f in /app/*.jar; do \
      case "$f" in \
        *.jar) \
          case "$f" in \
            *-plain.jar) continue ;; \
            *) \
              mv "$f" /app/app.jar; \
              break; \
          esac \
          ;; \
      esac; \
    done

ENTRYPOINT ["java", "-jar", "app.jar"]
