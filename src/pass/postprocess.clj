(ns pass.postprocess
  (:require [cheshire.core :as json]))

(defn read-json [path]
  (json/parse-string (slurp path) true))

;; FIX
;; - links.target:
;;     218960 -> 2189600
;;     218857 -> 2188578

(comment
  (read-json "docs/data.json"))
