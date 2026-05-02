(ns website.prep
  "Content processing — markdown → HTML + manifest.
   Run with: clj -X:prep"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str])
  (:import [org.commonmark.ext.gfm.tables TablesExtension]
           [org.commonmark.ext.heading.anchor HeadingAnchorExtension]
           [org.commonmark.node Heading Paragraph Text SoftLineBreak HardLineBreak]
           [org.commonmark.parser Parser]
           [org.commonmark.renderer.html HtmlRenderer]))

(def extensions [(TablesExtension/create) (HeadingAnchorExtension/create)])
(def parser (.build (-> (Parser/builder) (.extensions extensions))))
(def renderer (.build (-> (HtmlRenderer/builder) (.extensions extensions))))

(defn- git-creation-date
  "First commit date for a file, or nil if untracked."
  [path]
  (let [{:keys [exit out]} (shell/sh "git" "log" "--diff-filter=A"
                                     "--follow" "--format=%aI" "--" (str path))]
    (when (and (zero? exit) (not (str/blank? out)))
      (str/trim (last (str/split-lines out))))))

(defn- extract-title
  "First H1 text from a parsed document."
  [doc]
  (loop [node (.getFirstChild doc)]
    (when node
      (if (and (instance? Heading node) (= 1 (.getLevel node)))
        (let [sb (StringBuilder.)]
          (loop [child (.getFirstChild node)]
            (when child
              (cond
                (instance? Text child) (.append sb (.getLiteral child))
                (instance? SoftLineBreak child) (.append sb " ")
                (instance? HardLineBreak child) (.append sb " "))
              (recur (.getNext child))))
          (str sb))
        (recur (.getNext node))))))

(defn- extract-summary
  "Text of the first paragraph after the H1."
  [doc]
  (let [past-h1? (atom false)]
    (loop [node (.getFirstChild doc)]
      (when node
        (cond
          (and (instance? Heading node) (= 1 (.getLevel node)))
          (do (reset! past-h1? true) (recur (.getNext node)))

          (and @past-h1? (instance? Paragraph node))
          (let [sb (StringBuilder.)]
            (loop [child (.getFirstChild node)]
              (when child
                (cond
                  (instance? Text child) (.append sb (.getLiteral child))
                  (instance? SoftLineBreak child) (.append sb " ")
                  (instance? HardLineBreak child) (.append sb " "))
                (recur (.getNext child))))
            (str sb))

          :else (recur (.getNext node)))))))

(defn- extract-toc
  "Vector of {:level :text :anchor} from all headings."
  [doc]
  (let [headings (transient [])]
    (loop [node (.getFirstChild doc)]
      (when node
        (when (instance? Heading node)
          (let [sb (StringBuilder.)]
            (loop [child (.getFirstChild node)]
              (when child
                (when (instance? Text child)
                  (.append sb (.getLiteral child)))
                (recur (.getNext child))))
            (let [text (str sb)]
              (conj! headings {:level  (.getLevel node)
                               :text   text
                               :anchor (-> text str/lower-case
                                           (str/replace #"[^a-z0-9\s-]" "")
                                           str/trim
                                           (str/replace #"\s+" "-"))}))))
        (recur (.getNext node))))
    (persistent! headings)))

(defn- slug-from-path [f]
  (-> (.getName f)
      (str/replace #"\.md$" "")))

(defn- process-post [f]
  (let [md   (slurp f)
        doc  (.parse parser md)
        html (.render renderer doc)
        date (or (git-creation-date (.getPath f))
                 "1970-01-01T00:00:00+00:00")]
    {:slug    (slug-from-path f)
     :title   (or (extract-title doc) (slug-from-path f))
     :date    date
     :summary (or (extract-summary doc) "")
     :toc     (extract-toc doc)
     :html    html}))

(defn build-content!
  "Entry point — run with clj -X:prep"
  [_]
  (let [posts-dir (io/file "content/posts")
        out-dir   (io/file "resources/content/posts")
        md-files  (->> (.listFiles posts-dir)
                       (filter #(str/ends-with? (.getName %) ".md"))
                       (sort-by #(.getName %)))]
    (.mkdirs out-dir)
    (println (str "Processing " (count md-files) " post(s)..."))
    (let [posts (mapv (fn [f]
                        (let [post (process-post f)]
                          (spit (io/file out-dir (str (:slug post) ".html"))
                                (:html post))
                          (println (str "  ✓ " (:slug post) " — " (:title post)))
                          (dissoc post :html)))
                      md-files)]
      (spit (io/file "resources/content/posts.edn")
            (pr-str posts))
      (println (str "Wrote resources/content/posts.edn (" (count posts) " posts)")))))
