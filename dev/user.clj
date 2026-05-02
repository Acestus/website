(ns user
  (:require [nrepl.server :as nrepl]))

(defn start-nrepl []
  (nrepl/start-server :port 7888))
