package com.dgsw.bamboo.data;

public class URLS {
    private static final String baseURL = "http://ec2-13-125-167-78.ap-northeast-2.compute.amazonaws.com/api";
    public static final String signInURL = baseURL + "/cert/signin";

    public class USER {
        private static final String userBaseURL = baseURL + "/user";

        public class GET {
            public static final String countURL = userBaseURL + "/count";
            public static final String postedURL = userBaseURL + "/posted/";
        }

        public class POST {
            public static final String postURL = userBaseURL + "/post";
        }
    }

    public class ADMIN {
        private static final String adminBaseURL = baseURL + "/admin";

        public class GET {
            public static final String countURL = adminBaseURL + "/count";
            public static final String postedURL = adminBaseURL + "/posted/";
        }

        public class POST {
            public static final String allowURL = adminBaseURL + "/allow";
            public static final String denyURL = adminBaseURL + "/reject";
        }
    }
}
