(ns website.routes-test
  (:require [clojure.test :refer [deftest is testing]]
            [website.render :as render]
            [website.routes :as routes])
  (:import [com.microsoft.azure.functions HttpStatus]))

;; ── Minimal stubs ────────────────────────────────────────────────────────────

(defn- stub-request [path]
  (reify com.microsoft.azure.functions.HttpRequestMessage
    (getUri [_] (java.net.URI. (str "http://localhost" path)))
    (getHttpMethod [_] com.microsoft.azure.functions.HttpMethod/GET)
    (getHeaders [_] {})
    (getQueryParameters [_] {})
    (getBody [_] nil)
    (^com.microsoft.azure.functions.HttpResponseMessage$Builder
     createResponseBuilder [_ ^HttpStatus status]
     (let [st (atom status) hd (atom {}) bd (atom nil)]
       (reify com.microsoft.azure.functions.HttpResponseMessage$Builder
         (header [this k v] (swap! hd assoc k v) this)
         (body   [this v]   (reset! bd v) this)
         (^com.microsoft.azure.functions.HttpResponseMessage build [_]
          (let [s @st h @hd b @bd]
            (reify com.microsoft.azure.functions.HttpResponseMessage
              (getStatus     [_]   s)
              (getStatusCode [_]   (.value ^HttpStatus s))
              (getHeader     [_ k] (get h k))
              (getBody       [_]   b)))))))))

(defn- handle [path]
  (routes/handle (stub-request path)))

;; ── Route tests ──────────────────────────────────────────────────────────────

(deftest landing-page-test
  (testing "GET / returns 200 HTML"
    (let [resp (handle "/")]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (re-find #"text/html" (.getHeader resp "Content-Type"))))))

(deftest health-test
  (testing "GET /health returns 200 ok"
    (let [resp (handle "/health")]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (= "ok" (.getBody resp))))))

(deftest blog-index-test
  (testing "GET /blog redirects to blog.acestus.com"
    (let [resp (handle "/blog")]
      (is (= HttpStatus/FOUND (.getStatus resp)))
      (is (= "https://blog.acestus.com" (.getHeader resp "Location"))))))

(deftest contact-test
  (testing "GET /contact returns 200"
    (let [resp (handle "/contact")]
      (is (= HttpStatus/OK (.getStatus resp))))))

(deftest resume-test
  (testing "GET /resume returns 200"
    (let [resp (handle "/resume")
          body (.getBody resp)]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (re-find #"Download PDF" body))
      (is (re-find #"/resume.doc" body)))))

(deftest ai-platform-resume-test
  (testing "GET /resume/ai-platform-engineer returns AI platform resume"
    (let [resp (handle "/resume/ai-platform-engineer")
          body (.getBody resp)]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (re-find #"Senior AI Platform Engineer" body))
      (is (re-find #"Responsible agentic" body))
      (is (re-find #"Download PDF" body))
      (is (re-find #"/resume/ai-platform-engineer.doc" body)))))

(deftest ai-platform-resume-doc-test
  (testing "GET /resume/ai-platform-engineer.doc returns AI platform Word document"
    (let [resp (handle "/resume/ai-platform-engineer.doc")
          body (.getBody resp)]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (re-find #"application/msword" (.getHeader resp "Content-Type")))
      (is (re-find #"william-weeks-balconi-ai-platform-engineer-resume.doc"
                   (.getHeader resp "Content-Disposition")))
      (is (re-find #"Senior AI Platform Engineer" body)))))

(deftest sre-resume-test
  (testing "GET /resume/site-reliability-engineer returns SRE resume"
    (let [resp (handle "/resume/site-reliability-engineer")
          body (.getBody resp)]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (re-find #"Site Reliability Engineer" body))
      (is (re-find #"Incident response" body))
      (is (re-find #"Download PDF" body))
      (is (re-find #"/resume/site-reliability-engineer.doc" body)))))

(deftest sre-resume-doc-test
  (testing "GET /resume/site-reliability-engineer.doc returns SRE Word document"
    (let [resp (handle "/resume/site-reliability-engineer.doc")
          body (.getBody resp)]
      (is (= HttpStatus/OK (.getStatus resp)))
      (is (re-find #"application/msword" (.getHeader resp "Content-Type")))
      (is (re-find #"william-weeks-balconi-site-reliability-engineer-resume.doc"
                   (.getHeader resp "Content-Disposition")))
      (is (re-find #"Site Reliability Engineer" body)))))

(deftest not-found-test
  (testing "unknown route returns 404"
    (let [resp (handle "/does-not-exist")]
      (is (= HttpStatus/NOT_FOUND (.getStatus resp))))))

(deftest blog-post-not-found-test
  (testing "blog slugs redirect to blog.acestus.com"
    (let [resp (handle "/blog/no-such-slug")]
      (is (= HttpStatus/FOUND (.getStatus resp)))
      (is (= "https://blog.acestus.com" (.getHeader resp "Location"))))))

(deftest trailing-slash-test
  (testing "trailing slash on /blog/ is normalized and redirects"
    (let [resp (handle "/blog/")]
      (is (= HttpStatus/FOUND (.getStatus resp)))
      (is (= "https://blog.acestus.com" (.getHeader resp "Location"))))))

(deftest security-headers-test
  (testing "HTML responses include all security headers"
    (let [resp (handle "/")]
      (is (.getHeader resp "Strict-Transport-Security"))
      (is (.getHeader resp "X-Content-Type-Options"))
      (is (.getHeader resp "X-Frame-Options"))
      (is (.getHeader resp "Content-Security-Policy")))))

;; ── Render unit tests ─────────────────────────────────────────────────────────

(deftest render-not-found-test
  (testing "not-found-page renders a string containing 404"
    (let [html (render/not-found-page)]
      (is (string? html))
      (is (re-find #"404" html)))))

(deftest render-landing-test
  (testing "landing-page renders a string with site name"
    (let [html (render/landing-page)]
      (is (string? html))
      (is (re-find #"acestus" html)))))
