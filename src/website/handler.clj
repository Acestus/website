(ns website.handler
  "Azure Functions entry point — delegates routing to website.routes."
  (:require [website.routes :as routes])
  (:import [com.microsoft.azure.functions
            ExecutionContext
            HttpRequestMessage
            HttpResponseMessage]
           [com.microsoft.azure.functions.annotation
            AuthorizationLevel
            FunctionName
            HttpTrigger])
  (:gen-class
   :name website.WebsiteFunction
   :methods [^{FunctionName "pages"} [pages
             [com.microsoft.azure.functions.HttpRequestMessage
              com.microsoft.azure.functions.ExecutionContext]
             com.microsoft.azure.functions.HttpResponseMessage]
             ^{FunctionName "health"} [health
             [com.microsoft.azure.functions.HttpRequestMessage
              com.microsoft.azure.functions.ExecutionContext]
             com.microsoft.azure.functions.HttpResponseMessage]]))

(defn -pages
  [^HttpRequestMessage request ^ExecutionContext _ctx]
  (routes/handle request))

(defn -health
  [^HttpRequestMessage request ^ExecutionContext _ctx]
  (-> (.createResponseBuilder request com.microsoft.azure.functions.HttpStatus/OK)
      (.header "Content-Type" "text/plain; charset=utf-8")
      (.body "ok")
      .build))
