package com.app.maxdocapi;

public class Routes {
    public static final String PATH_FIELD = "path";
    public static final String root = "/api";

    public static final class Documents {
        public static final String path = Routes.root + "/documents";

        public static final class ById {
            public static final String path = Documents.path + "/{id}";
        }
    }
}
