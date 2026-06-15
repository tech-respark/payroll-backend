package com.relfor.pcs.payroll.util;

public class SensitiveErrorFilter {
    private static final String[] sensitivePatterns = {
            // Database and Persistence
            "query", "sql", "mysql", "jdbc", "jpa", "hibernate", "resultset", "statement", "preparedstatement",
            "syntax error", "invalid column", "duplicate entry", "ora-", "sqlstate", "Incorrect result size",
            "javax.persistence.", "jakarta.persistence.", "Hikari", "connection pool", "c3p0", "connection refused",

            // Java Platform / JVM / Concurrency
            "java.lang", "sun.reflect", "com.sun", "jdk.internal.", "java.base", "java.util.concurrent",
            "classnotfound", "illegalaccess", "nosuchmethod", "stacktrace", "exception in thread",
            "executor", "pool", "deadlock", "module ", "jpms", "loom", "virtual thread", "jfr", "jstack", "gc overhead",

            // Spring Framework / Web
            "springframework", "spring", "spring-boot", "spring.factories", "webclient", "webflux", "reactive",
            "struts", "servlet", "javax.servlet", "jakarta.servlet.", "filter", "apache tomcat", "undertow",
            "jetty", "jboss", "weblogic", "glassfish", "netty", "nio", "handler exception", "bind exception",
            "jakarta.validation.",

            // Configuration / File System / Security
            "keystore", "truststore", "c:\\", "/etc/", "/var/", "/home/", "file not found",
            "filenotfoundexception", "access denied", "permission denied", "resource not found",
            "environment variable", "system property", "application.properties", "yaml", "vault", "kubernetes",
            "docker", "secret"
    };

    private SensitiveErrorFilter() {
    }

    public static boolean isSensitive(String error) {
        for (String pat : sensitivePatterns) {
            if (error != null && error.toLowerCase().contains(pat.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
