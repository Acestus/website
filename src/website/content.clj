(ns website.content
  "Post loading and content access."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn load-posts
  "Read manifest + HTML fragments from classpath. Returns {slug -> post-map}."
  []
  (let [manifest (-> (io/resource "content/posts.edn") slurp edn/read-string)]
    (into {}
          (keep (fn [post]
                  (when-let [res (io/resource (str "content/posts/" (:slug post) ".html"))]
                    [(:slug post) (assoc post :html (slurp res))])))
          manifest)))

(defn posts-by-date
  "Sorted newest-first."
  [posts]
  (->> (vals posts)
       (sort-by :date)
       reverse))
