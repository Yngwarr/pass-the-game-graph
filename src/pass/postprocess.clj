(ns pass.postprocess
  (:require [cheshire.core :as json]))

(defn read-json [path]
  (json/parse-string (slurp path) true))

;; FIX
;; - links.target:
;;     218960 -> 2189600
;;     218857 -> 2188578
;; - add links:
;;     {
;;   "source": "https://itch.io/jam/day-1/rate/2187658",
;;   "target": "https://itch.io/jam/day-2/rate/2188857"
;; }, {
;;   "source": "https://itch.io/jam/day-1/rate/2188145",
;;   "target": "https://itch.io/jam/day-2/rate/2189179"
;; }, {
;;   "source": "https://itch.io/jam/day-2/rate/2189696",
;;   "target": "https://itch.io/jam/day-3/rate/2191644"
;; }, {
;;   "source": "https://itch.io/jam/day-3/rate/2191734",
;;   "target": "https://itch.io/jam/day-4/rate/2193386"
;; }, {
;;   "source": "https://itch.io/jam/day-2/rate/2190136",
;;   "target": "https://itch.io/jam/day-3/rate/2191654"
;; }, {
;;   "source": "https://itch.io/jam/day-3/rate/2191529",
;;   "target": "https://itch.io/jam/day-4/rate/2193378"
;; }, {
;;   "source": "https://itch.io/jam/day-3/rate/2191745",
;;   "target": "https://itch.io/jam/day-4/rate/2193387"
;; }, {
;;   "source": "https://itch.io/jam/day-4/rate/2193375",
;;   "target": "https://itch.io/jam/day-5/rate/2194411"
;; }, {
;;   "source": "https://itch.io/jam/day-3/rate/2191409",
;;   "target": "https://itch.io/jam/day-4/rate/2192078"
;; }, {
;;   "source": "https://itch.io/jam/day-1/rate/2188275",
;;   "target": "https://itch.io/jam/day-2/rate/2190253"
;; }


(comment
  (read-json "docs/data.json"))
