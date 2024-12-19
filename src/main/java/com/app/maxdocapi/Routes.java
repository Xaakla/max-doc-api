package com.app.maxdocapi;

public class Routes {
    public static final String PATH_FIELD = "path";
    public static final String root = "/api";

    public static final class Documents {
        public static final String path = Routes.root + "/documents";

        public static final class Acronym {
            public static final String path = Documents.path + "/by-acronym";
        }

        public static final class ById {
            public static final String path = Documents.path + "/{id}";

            public static final class Submit {
                public static final String path = ById.path + "/submit";
            }

            public static final class GenerateVersion {
                public static final String path = ById.path + "/generate-version";
            }
        }
    }
}
