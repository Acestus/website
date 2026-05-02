(ns website.handler
  "Local development HTTP server."
  (:require [website.content :as content]
            [website.render :as render]
            [website.routes :as routes]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [com.sun.net.httpserver HttpExchange HttpServer]
           [java.net InetSocketAddress]
           [java.nio.charset StandardCharsets])
  (:gen-class))

(defn- send-bytes! [^HttpExchange exchange status content-type ^bytes body]
  (.set (.getResponseHeaders exchange) "Content-Type" content-type)
  (.sendResponseHeaders exchange status (alength body))
  (with-open [out (.getResponseBody exchange)]
    (.write out body 0 (alength body))))

(defn- send-html! [^HttpExchange exchange status html]
  (send-bytes! exchange status "text/html; charset=utf-8"
               (.getBytes (str html) StandardCharsets/UTF_8)))

(defn- send-text! [^HttpExchange exchange status text]
  (send-bytes! exchange status "text/plain; charset=utf-8"
               (.getBytes (str text) StandardCharsets/UTF_8)))

(defn- send-static! [^HttpExchange exchange resource-path content-type]
  (if-let [res (io/resource resource-path)]
    (send-bytes! exchange 200 content-type
                 (.getBytes (slurp res) StandardCharsets/UTF_8))
    (send-html! exchange 404 (render/not-found-page))))

(defn- send-binary! [^HttpExchange exchange resource-path content-type]
  (if-let [res (io/resource resource-path)]
    (with-open [in (io/input-stream res)]
      (send-bytes! exchange 200 content-type (.readAllBytes in)))
    (send-html! exchange 404 (render/not-found-page))))

(defn- dispatch [posts ^HttpExchange exchange]
  (let [path (some-> exchange .getRequestURI .getPath
                     (str/replace #"/$" "")
                     (as-> p (if (str/blank? p) "/" p)))]
    (cond
      (= path "/")      (send-html! exchange 200 (render/landing-page))
      (= path "/health") (send-text! exchange 200 "ok")
      (= path "/blog")   (send-html! exchange 200 (render/blog-index-page posts))
      (= path "/contact") (send-html! exchange 200 (render/contact-page))
      (= path "/resume")  (send-html! exchange 200 (render/resume-page))

      (str/starts-with? path "/blog/")
      (let [slug (subs path 6)]
        (if-let [post (get posts slug)]
          (send-html! exchange 200 (render/post-page post))
          (send-html! exchange 404 (render/not-found-page))))

      (str/starts-with? path "/static/")
      (let [rel (subs path 1)]
        (cond
          (str/ends-with? rel ".css") (send-static! exchange rel "text/css; charset=utf-8")
          (str/ends-with? rel ".js")  (send-static! exchange rel "application/javascript; charset=utf-8")
          (str/ends-with? rel ".vcf") (send-static! exchange rel "text/vcard; charset=utf-8")
          (str/ends-with? rel ".svg") (send-static! exchange rel "image/svg+xml")
          (str/ends-with? rel ".png") (send-binary! exchange rel "image/png")
          (str/ends-with? rel ".jpg") (send-binary! exchange rel "image/jpeg")
          :else (send-html! exchange 404 (render/not-found-page))))

      :else (send-html! exchange 404 (render/not-found-page)))))

(defn -main [& _args]
  (let [port  (Integer/parseInt (or (System/getenv "PORT") "7071"))
        posts (content/load-posts)
        srv   (HttpServer/create (InetSocketAddress. port) 0)]
    (.createContext srv "/"
                    (reify com.sun.net.httpserver.HttpHandler
                      (handle [_ exchange]
                        (try (dispatch posts exchange)
                             (catch Exception ex
                               (println "Request error:" (.getMessage ex))
                               (send-text! exchange 500 "error"))))))
    (.setExecutor srv nil)
    (.start srv)
    (println (str "website listening on http://localhost:" port))
    @(promise)))
