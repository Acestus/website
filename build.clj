(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'acestus/website)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/javac {:src-dirs ["src"]
            :class-dir class-dir
            :basis @basis
            :javac-opts ["-source" "17" "-target" "17"]})
  (b/compile-clj {:basis @basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/copy-dir {:src-dirs ["resources"]
               :target-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file jar-file
           :basis @basis
           :main 'website.handler}))

(defn native [_]
  (uber nil)
  (b/process {:command-args ["native-image"
                             "--no-fallback"
                             "-H:Name=bin/website"
                             "-jar"
                             jar-file]}))
