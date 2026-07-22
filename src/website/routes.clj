(ns website.routes
  "Azure Functions HTTP request handler."
  (:require [website.content :as content]
            [website.render :as render]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [com.microsoft.azure.functions HttpRequestMessage HttpResponseMessage HttpStatus]))

(defonce ^:private posts (delay (content/load-posts)))

(defn- with-security-headers [builder]
  (-> builder
      (.header "Strict-Transport-Security" "max-age=31536000; includeSubDomains")
      (.header "X-Content-Type-Options" "nosniff")
      (.header "X-Frame-Options" "SAMEORIGIN")
      (.header "Content-Security-Policy"
               "default-src 'self'; style-src 'self'; script-src 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'")))

(defn- html-response [^HttpRequestMessage request status html]
  (-> (.createResponseBuilder request status)
      (.header "Content-Type" "text/html; charset=utf-8")
      with-security-headers
      (.body html)
      .build))

(defn- text-response [^HttpRequestMessage request status text]
  (-> (.createResponseBuilder request status)
      (.header "Content-Type" "text/plain; charset=utf-8")
      (.body text)
      .build))

(defn- static-response [^HttpRequestMessage request resource-path content-type]
  (if-let [res (io/resource resource-path)]
    (-> (.createResponseBuilder request HttpStatus/OK)
        (.header "Content-Type" content-type)
        with-security-headers
        (.body (slurp res))
        .build)
    (html-response request HttpStatus/NOT_FOUND (render/not-found-page))))

(defn- binary-response [^HttpRequestMessage request resource-path content-type]
  (if-let [res (io/resource resource-path)]
    (with-open [in (io/input-stream res)]
      (let [bytes (.readAllBytes in)]
        (-> (.createResponseBuilder request HttpStatus/OK)
            (.header "Content-Type" content-type)
            with-security-headers
            (.body bytes)
            .build)))
    (html-response request HttpStatus/NOT_FOUND (render/not-found-page))))

(defn- doc-response
  "Serve HTML as a Word-compatible .doc attachment."
  [^HttpRequestMessage request filename html]
  (-> (.createResponseBuilder request HttpStatus/OK)
      (.header "Content-Type" "application/msword; charset=utf-8")
      (.header "Content-Disposition" (str "attachment; filename=\"" filename "\""))
      with-security-headers
      (.body html)
      .build))

(defn handle
  "Route an HTTP request to a response."
  [^HttpRequestMessage request]
  (let [path (.. request getUri getPath)
        path (if (and (str/ends-with? path "/") (> (count path) 1))
               (subs path 0 (dec (count path)))
               path)]
    (cond
      (or (= path "/") (= path ""))
      (html-response request HttpStatus/OK (render/landing-page))

      (= path "/health")
      (text-response request HttpStatus/OK "ok")

      (or (= path "/blog") (str/starts-with? path "/blog/"))
      (let [redirect-url "https://blog.acestus.com"]
        (-> (.createResponseBuilder request HttpStatus/FOUND)
            (.header "Location" redirect-url)
            with-security-headers
            .build))

      (= path "/contact")
      (html-response request HttpStatus/OK (render/contact-page))

      (= path "/resume")
      (html-response request HttpStatus/OK (render/resume-page))

      (= path "/resume.doc")
      (doc-response request "william-weeks-balconi-resume.doc" (render/resume-doc))

      (= path "/resume/ai-platform-engineer")
      (html-response request HttpStatus/OK (render/resume-page :ai-platform))

      (= path "/resume/site-reliability-engineer")
      (html-response request HttpStatus/OK (render/resume-page :sre))

      (or (= path "/portfolio") (str/starts-with? path "/portfolio/"))
      (let [redirect-url "https://portfolio.acestus.com"]
        (-> (.createResponseBuilder request HttpStatus/FOUND)
            (.header "Location" (if (= path "/portfolio")
                                  redirect-url
                                  (str redirect-url (subs path 10))))
            with-security-headers
            .build))

      (str/starts-with? path "/static/")
      (let [rel (subs path 1)]
        (cond
          (str/ends-with? rel ".css") (static-response request rel "text/css; charset=utf-8")
          (str/ends-with? rel ".js")  (static-response request rel "application/javascript; charset=utf-8")
          (str/ends-with? rel ".vcf") (static-response request rel "text/vcard; charset=utf-8")
          (str/ends-with? rel ".png") (binary-response request rel "image/png")
          (str/ends-with? rel ".jpg") (binary-response request rel "image/jpeg")
          (str/ends-with? rel ".svg") (static-response request rel "image/svg+xml")
          :else (html-response request HttpStatus/NOT_FOUND (render/not-found-page))))

      :else
      (html-response request HttpStatus/NOT_FOUND (render/not-found-page)))))
