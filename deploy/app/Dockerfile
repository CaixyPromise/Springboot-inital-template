# jar包部署运行
# 使用 Java 8 基础镜像
FROM openjdk:8

# 创建工作目录
WORKDIR /app
COPY src ./src
# 将生产环境配置文件复制到容器中
COPY application-prod.yml .

# 将构建的 JAR 文件复制到容器中
COPY caixy-admin-system-0.0.1-SNAPSHOT.jar .

# 在容器启动时运行 Java 应用程序，并设置Xms、Xmx和内存不足时生成堆转储文件参数
CMD ["java",
        "-Xms512m",
        "-Xmx512m",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=/app/dumps",
        "-jar",
        "/app/caixy-admin-system-0.0.1-SNAPSHOT.jar",
        "--spring.profiles.active=prod"]
