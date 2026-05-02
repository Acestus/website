#!/usr/bin/env bb
;; Sync posts from https://world.hey.com/jump/feed.atom into resources/content/.
;; Run from repo root: bb scripts/sync-hey-world.bb
;;
;; On success exits 0. Prints "new=N" so callers can detect new posts.

(require '[clojure.data.xml :as xml]
         '[clojure.edn      :as edn]
         '[clojure.java.io  :as io]
         '[clojure.string   :as str])

(def feed-url   "https://world.hey.com/jump/feed.atom")
(def posts-dir  "resources/content/posts")
(def posts-edn  "resources/content/posts.edn")

;; ── XML helpers ──────────────────────────────────────────────────────────────

(defn- xml-text [node]
  (str/join (filter string? (:content node))))

(defn- find-child [node tag]
  (->> (:content node)
       (filter #(and (map? %) (= (name (:tag %)) tag)))
       first))

(defn- link-href [entry]
  (-> (find-child entry "link")
      (get-in [:attrs :href])))

(defn- slug-from-url [url]
  (when url (-> url (str/split #"/") last)))

;; ── HTML cleanup ─────────────────────────────────────────────────────────────

(defn- strip-tags [html]
  (-> html
      (str/replace #"<[^>]+>" " ")
      (str/replace #"&amp;"  "&")
      (str/replace #"&lt;"   "<")
      (str/replace #"&gt;"   ">")
      (str/replace #"&nbsp;" " ")
      (str/replace #"&#39;"  "'")
      (str/replace #"&quot;" "\"")
      (str/replace #"\s+"    " ")
      str/trim))

(defn- truncate [s n]
  (if (<= (count s) n)
    s
    (let [sub (subs s 0 n)
          i   (str/last-index-of sub " ")]
      (str (if i (subs sub 0 i) sub) "…"))))

(defn- summary [html]
  (truncate (strip-tags html) 200))

;; ── Feed parsing ─────────────────────────────────────────────────────────────

(defn- parse-entry [entry]
  (let [url  (link-href entry)
        html (xml-text (find-child entry "content"))]
    {:slug    (-> url (str/split #"/") last)
     :title   (xml-text (find-child entry "title"))
     :date    (xml-text (find-child entry "published"))
     :summary (summary html)
     :url     url
     :html    html}))

(defn- fetch-entries []
  (println "Fetching" feed-url "...")
  (let [feed (xml/parse-str (slurp feed-url))]
    (->> (:content feed)
         (filter #(and (map? %) (= (name (:tag %)) "entry")))
         (map parse-entry))))

;; ── Persistence ──────────────────────────────────────────────────────────────

(defn- load-existing []
  (let [f (io/file posts-edn)]
    (if (.exists f)
      (into {} (map (juxt :slug identity) (edn/read-string (slurp f))))
      {})))

(defn- write-html! [{:keys [slug html]}]
  (spit (str posts-dir "/" slug ".html") html))

(defn- write-manifest! [posts]
  (let [sorted (->> (vals posts)
                    (sort-by :date)
                    reverse
                    (mapv #(dissoc % :html :url)))]
    (spit posts-edn (pr-str sorted))))

;; ── Main ─────────────────────────────────────────────────────────────────────

(defn -main []
  (.mkdirs (io/file posts-dir))
  (let [existing (load-existing)
        entries  (fetch-entries)
        new-posts (remove #(contains? existing (:slug %)) entries)]
    (println (str (count entries) " in feed, " (count new-posts) " new"))
    (doseq [p new-posts]
      (write-html! p)
      (println "  +" (:slug p)))
    (when (seq new-posts)
      (write-manifest! (merge existing (into {} (map (juxt :slug identity) new-posts))))
      (println "  wrote" posts-edn))
    (println (str "new=" (count new-posts)))))

(-main)
