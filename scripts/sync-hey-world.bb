#!/usr/bin/env bb
;; Sync posts from https://world.hey.com/jump/feed.atom into the three Hugo
;; blogs (blog-cloud, blog-history, blog-christian), classifying each new
;; post by topic and routing it into the right repo as a markdown file.
;;
;; Run from repo root: bb scripts/sync-hey-world.bb
;; Requires: git write access to Acestus/blog-cloud, Acestus/blog-history,
;;           Acestus/blog-christian (via GIT_ASKPASS/token in environment,
;;           same as the website repo's own push).
;;
;; On success exits 0. Prints "new=N" so callers can detect new posts.

(require '[clojure.data.xml :as xml]
         '[clojure.edn      :as edn]
         '[clojure.java.io  :as io]
         '[clojure.java.shell :as shell]
         '[clojure.string   :as str])

(def feed-url "https://world.hey.com/jump/feed.atom")

(def state-file ".hey-world-synced.edn") ;; tracks slugs already routed, lives in website repo

(def blogs
  {:cloud     {:repo "https://github.com/Acestus/blog-cloud.git"
               :dir  "/tmp/blog-cloud-sync"
               :base "https://blog.acestus.com"
               :tags ["meta"]}
   :history   {:repo "https://github.com/Acestus/blog-history.git"
               :dir  "/tmp/blog-history-sync"
               :base "https://history.acestus.com"
               :tags ["meta"]}
   :christian {:repo "https://github.com/Acestus/blog-christian.git"
               :dir  "/tmp/blog-christian-sync"
               :base "https://dad.acestus.com"
               :tags ["meta"]}})

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

(defn- summary [text]
  (truncate text 200))

;; ── Topic classification ─────────────────────────────────────────────────────

(def keyword-weights
  {:cloud     ["azure" "cloud" "sre" "reliability" "kubernetes" "devops"
               "infrastructure" "incident" "architecture" "openclaw" "agent"
               "automation" "api" "github" "cicd" "ci/cd" "terraform" "docker"
               "engineering" "pipeline" "deploy" "ai " "artificial intelligence"
               "llm" "prompt" "workflow" "toolkit" "operator" "server" "code"]
   :history   ["history" "historian" "primary source" "secondary source"
               "philosophy" "political" "essay" "civilization" "ideas"
               "culture" "movie" "film" "star trek" "media" "journalism"
               "think tank" "rhetoric" "ancient" "empire" "war" "revolution"]
   :christian ["faith" "catholic" "jesus" "christ" "church" "marriage" "wife"
               "husband" "son" "daughter" "father" "prayer" "kids" "children"
               "family" "parenting" "god" "gospel" "mass" "saint" "sacrament"]})

(defn- score [text words]
  (let [lower (str/lower-case text)]
    (reduce + (map (fn [w]
                     (count (re-seq (re-pattern (str "(?i)" (java.util.regex.Pattern/quote w)))
                                    lower)))
                   words))))

(defn- classify [title body]
  (let [text (str title " " body)
        scores (into {} (map (fn [[k words]] [k (score text words)]) keyword-weights))
        [best-key best-score] (apply max-key val scores)]
    (if (pos? best-score)
      best-key
      :cloud))) ;; default bucket when nothing matches

;; ── Feed parsing ─────────────────────────────────────────────────────────────

(defn- parse-entry [entry]
  (let [url  (link-href entry)
        html (xml-text (find-child entry "content"))
        text (strip-tags html)
        title (xml-text (find-child entry "title"))]
    {:slug     (-> url (str/split #"/") last)
     :title    title
     :date     (xml-text (find-child entry "published"))
     :body     text
     :url      url
     :html     html
     :category (classify title text)}))

(defn- fetch-entries []
  (println "Fetching" feed-url "...")
  (let [feed (xml/parse-str (slurp feed-url))]
    (->> (:content feed)
         (filter #(and (map? %) (= (name (:tag %)) "entry")))
         (map parse-entry))))

;; ── State ────────────────────────────────────────────────────────────────────

(defn- load-synced []
  (let [f (io/file state-file)]
    (if (.exists f)
      (set (edn/read-string (slurp f)))
      #{})))

(defn- write-synced! [synced]
  (spit state-file (pr-str (vec (sort synced)))))

;; ── Git helpers ──────────────────────────────────────────────────────────────

(defn- sh! [& args]
  (let [{:keys [exit out err]} (apply shell/sh args)]
    (when-not (zero? exit)
      (println "  ! command failed:" args)
      (println out)
      (println err))
    exit))

(defn- slug->filename [slug title]
  (str slug ".md"))

(defn- frontmatter [title date category]
  (str "---\n"
       "title: " (pr-str title) "\n"
       "date: " date "\n"
       "draft: false\n"
       "tags: [\"hey-world\"]\n"
       "summary: " (pr-str (summary title)) "\n"
       "---\n\n"))

(defn- write-post-md! [dir post]
  (let [path (str dir "/content/posts/" (:slug post) ".md")]
    (spit path (str (frontmatter (:title post) (:date post) (:category post))
                    (:body post) "\n"))))

(defn- sync-category! [category posts]
  (when (seq posts)
    (let [{:keys [repo dir]} (get blogs category)]
      (println "  ->" (name category) ":" (count posts) "post(s)")
      (when-not (.exists (io/file dir))
        (sh! "git" "clone" "--depth=1" repo dir))
      (doseq [p posts]
        (write-post-md! dir p)
        (println "     +" (:slug p)))
      (sh! "git" "-C" dir "add" "-A")
      (sh! "git" "-C" dir "config" "user.name" "github-actions[bot]")
      (sh! "git" "-C" dir "config" "user.email" "github-actions[bot]@users.noreply.github.com")
      (let [msg (str "Sync Hey World posts\n\n"
                     (str/join "\n" (map #(str "  + " (:slug %)) posts)))]
        (sh! "git" "-C" dir "commit" "-m" msg))
      (sh! "git" "-C" dir "push"))))

;; ── Main ─────────────────────────────────────────────────────────────────────

(defn -main []
  (let [synced    (load-synced)
        entries   (fetch-entries)
        new-posts (remove #(contains? synced (:slug %)) entries)]
    (println (str (count entries) " in feed, " (count new-posts) " new"))
    (doseq [[category posts] (group-by :category new-posts)]
      (sync-category! category posts))
    (when (seq new-posts)
      (write-synced! (into synced (map :slug new-posts))))
    ;; Write new-posts.json for downstream steps (e.g. Mastodon/Dev.to)
    (let [esc   (fn [s] (-> (or s "")
                            (str/replace "\\" "\\\\")
                            (str/replace "\"" "\\\"")
                            (str/replace "\n" "\\n")
                            (str/replace "\r" "")
                            (str/replace "\t" " ")))
          items (map (fn [{:keys [slug title body category]}]
                       (let [base (get-in blogs [category :base])]
                         (str "{\"slug\":\"" (esc slug) "\","
                              "\"title\":\"" (esc title) "\","
                              "\"body\":\"" (esc body) "\","
                              "\"url\":\"" (esc (str base "/posts/" slug "/")) "\"}")))
                     new-posts)]
      (spit "/tmp/new-posts.json"
            (str "[" (str/join "," items) "]")))
    (println (str "new=" (count new-posts)))))

(-main)
