# Getting Started with Acestus

This is the first post on acestus.com — a personal blog built with Clojure, served as a GraalVM native binary on Azure Functions.

## Why Build a Blog From Scratch?

There are hundreds of static site generators. Why write one in Clojure?

Because the point isn't the blog. The point is the system. I wanted a project that exercises the same patterns I use at work — Azure Functions, Flex Consumption, Bicep, GitHub Actions — but in a space where I control every decision.

## The Architecture

The blog is three decomplected steps:

1. **Prep** — read markdown, parse to HTML, extract metadata
2. **Build** — compile Clojure, package into a native binary
3. **Serve** — read data from classpath, route requests, render pages

<div class="panel panel-info">
<strong>Note:</strong> The entire application is a single Clojure file. Posts are maps. Pages are functions from maps to strings.
</div>

### What the Server Does

At startup, the handler reads a manifest of posts and their pre-rendered HTML from the classpath. Every request is a pure function from a URL to an HTML string. No database, no external services, no mutable state.

```clojure
(defn dispatch [posts ^HttpExchange exchange]
  (let [method (.getRequestMethod exchange)
        path   (some-> exchange .getRequestURI .getPath)]
    (cond
      (and (= method "GET") (= path "/"))
      (send-html! exchange 200 (index-page posts))

      (and (= method "GET") (str/starts-with? path "/posts/"))
      (let [slug (subs path 7)]
        (if-let [post (get posts slug)]
          (send-html! exchange 200 (post-page post))
          (send-html! exchange 404 (not-found-page))))

      :else
      (send-html! exchange 404 (not-found-page)))))
```

## What's Next

More posts about infrastructure, Clojure, and the gap between how we think systems should work and how they actually do.

<div class="panel panel-warning">
<strong>Warning:</strong> This blog has no comments section. If you want to discuss something, find me on GitHub.
</div>
