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
  (testing "GET /blog returns 200"
    (let [resp (handle "/blog")]
      (is (= HttpStatus/OK (.getStatus resp))))))

(deftest contact-test
  (testing "GET /contact returns 200"
    (let [resp (handle "/contact")]
      (is (= HttpStatus/OK (.getStatus resp))))))

(deftest resume-test
  (testing "GET /resume returns 200"
    (let [resp (handle "/resume")]
      (is (= HttpStatus/OK (.getStatus resp))))))

(deftest not-found-test
  (testing "unknown route returns 404"
    (let [resp (handle "/does-not-exist")]
      (is (= HttpStatus/NOT_FOUND (.getStatus resp))))))

(deftest blog-post-not-found-test
  (testing "unknown blog slug returns 404"
    (let [resp (handle "/blog/no-such-slug")]
      (is (= HttpStatus/NOT_FOUND (.getStatus resp))))))

(deftest trailing-slash-test
  (testing "trailing slash on /blog/ is normalized and returns 200"
    (let [resp (handle "/blog/")]
      (is (= HttpStatus/OK (.getStatus resp))))))

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
